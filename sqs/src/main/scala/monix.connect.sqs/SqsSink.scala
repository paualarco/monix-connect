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

import com.typesafe.scalalogging.StrictLogging
import monix.connect.sqs.SqsOp.Implicits.sendMessage
import monix.connect.sqs.domain.BatchMessageEntry
import monix.eval.Task
import monix.execution.cancelables.AssignableCancelable
import monix.execution.{Ack, Callback, Scheduler}
import monix.reactive.Consumer
import monix.reactive.observers.Subscriber
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.{MessageAttributeValue, MessageSystemAttributeNameForSends, MessageSystemAttributeValue, SendMessageBatchRequest, SendMessageRequest, SqsRequest, SqsResponse}

import scala.concurrent.Future
import scala.util.control.NonFatal

private[sqs] class SqsSink(
  queueUrl: String,
  groupId: String,
  systemAttributes: Map[MessageSystemAttributeNameForSends, MessageSystemAttributeValue],
  attributes: Map[String, MessageAttributeValue])(
  implicit
  client: SqsAsyncClient)
  extends Consumer[String, Unit] with StrictLogging {

  private[this] def sendRequest(message: String): SendMessageRequest =
    SqsRequestBuilder.sendMessageRequest(queueUrl, None, groupId, attributes, systemAttributes)(message, None)

  override def createSubscriber(
    cb: Callback[Throwable, Unit],
    s: Scheduler): (Subscriber[String], AssignableCancelable) = {
    val sub = new Subscriber[String] { self =>

      implicit val scheduler = s
      var batch: Seq[String] = List.empty[String]
      private[this] var isActive = true

      def onNext(message: String): Future[Ack] = {
       self.synchronized {
         if (isActive) {
           println("Received message: " + message)
           Task
             .from(SqsOp.create(sendRequest(message))(sendMessage, client))
             .redeem(ex => {
               println("Unexpected error in SqsSink. ", ex)
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
              cb
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


object SqsSink {
  import scala.concurrent.duration._
  def apply[In <: SqsRequest, Out <: SqsResponse](
                                                   queueUrl: String,
                                                   groupId: String,
                                                   systemAttributes: Map[MessageSystemAttributeNameForSends, MessageSystemAttributeValue] = Map.empty,
                                                   attributes: Map[String, MessageAttributeValue] = Map.empty
                                                 )(implicit client: SqsAsyncClient): Consumer[String, Unit] =
    new SqsSink(queueUrl, groupId, systemAttributes, attributes)(client)

}

