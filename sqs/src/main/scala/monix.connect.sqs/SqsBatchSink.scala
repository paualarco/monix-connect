/*
 * Copyright (c) 2020-2020 by The Monix Connect Project Developers.
 * See the project homepage at: https://monix.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package monix.connect.sqs

import monix.eval.Task
import monix.execution.cancelables.AssignableCancelable
import monix.execution.{Ack, Callback, Scheduler}
import monix.reactive.observers.Subscriber
import monix.reactive.Consumer
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.{MessageAttributeValue, MessageSystemAttributeNameForSends, MessageSystemAttributeValue, SendMessageBatchRequest, SqsRequest, SqsResponse}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Future
import monix.connect.sqs.SqsOp.Implicits.sendMessageBatch
import monix.connect.sqs.domain.{BatchMessageEntry, SqsClientConfig}

import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal

private[sqs] class SqsBatchSink(
  queueUrl: String,
  groupId: String,
  delay: FiniteDuration,
  systemAttributes: Map[MessageSystemAttributeNameForSends, MessageSystemAttributeValue],
  attributes: Map[String, MessageAttributeValue])(
  implicit
  client: SqsAsyncClient)
  extends Consumer[List[BatchMessageEntry], Unit] with StrictLogging {

  SqsClientConfig()
  private[this] def batchRequest(messages: List[BatchMessageEntry]): SendMessageBatchRequest =
    SqsRequestBuilder.sendBatchMessage(queueUrl, delay, groupId, attributes, systemAttributes)(messages)

  override def createSubscriber(
    cb: Callback[Throwable, Unit],
    s: Scheduler): (Subscriber[List[BatchMessageEntry]], AssignableCancelable) = {
    val sub = new Subscriber[List[BatchMessageEntry]] { self =>

      implicit val scheduler = s
      var batch: Seq[String] = List.empty[String]
      private[this] var isActive = true

      def onNext(messages: List[BatchMessageEntry]): Future[Ack] = {
       self.synchronized {
         if (isActive) {
           Task
             .from(SqsOp.create(batchRequest(messages))(sendMessageBatch, client))
             .redeem(ex => {
               logger.error("Unexpected error in SqsSink. ", ex)
               Ack.Continue
             }, _ => Ack.Continue)
             .runToFuture(scheduler)
         } else {
           Ack.Stop
         }
       }
      }

      def terminate(cb: => Unit): Unit =
        self.synchronized {
          if (isActive) {
            isActive = false
            try {
              client.close()
            } catch {
              case NonFatal(ex) =>
                logger.error("Error closing the SqsClient.", ex)
                cb
            }
          }
        }

      def onComplete(): Unit = {
        terminate(cb.onSuccess())
      }

      def onError(ex: Throwable): Unit = {
        terminate(cb.onError(ex))
      }
    }
    (sub, AssignableCancelable.multi())
  }

}

object SqsBatchSink {
import scala.concurrent.duration._
  def apply[In <: SqsRequest, Out <: SqsResponse](
    queueUrl: String,
    groupId: String,
    delay: FiniteDuration = 0.seconds,
    systemAttributes: Map[MessageSystemAttributeNameForSends, MessageSystemAttributeValue] = Map.empty,
    attributes: Map[String, MessageAttributeValue] = Map.empty
  )(implicit client: SqsAsyncClient): Consumer[List[BatchMessageEntry], Unit] =
    new SqsBatchSink(queueUrl, groupId, delay, systemAttributes, attributes)(client)

}
