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

import cats.effect.Resource
import monix.eval.Task
import monix.reactive.{Consumer, Observable, Observer, OverflowStrategy}
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.{Message, MessageAttributeValue, MessageSystemAttributeNameForSends, MessageSystemAttributeValue, ReceiveMessageRequest, SendMessageRequest, SqsRequest, SqsResponse}
import monix.connect.sqs.domain.{DefaultSourceSettings, SqsSourceSettings}
import monix.execution.{Ack, Callback, Cancelable}
import monix.reactive.observers.Subscriber
import monix.connect.sqs.SqsOp.Implicits._

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

private[sqs] class SqsSource(queueUrl: String, settings: SqsSourceSettings, asyncClient: Task[SqsAsyncClient])
  extends Observable[Message] {

  override def unsafeSubscribeFn(out: Subscriber[Message]): Cancelable = {

    Resource
      .make(acquire = asyncClient)(release = client => Task(client.close()))
      .use(client => runLoop(out, client))
      .runToFuture(out.scheduler)
  }
  val receiveRequest = SqsRequestBuilder.receiveRequest(queueUrl, SqsSourceSettings(maxNumberOfMessages = 1))

  def runLoop(out: Subscriber[Message], client: SqsAsyncClient): Task[Unit] = {
    for {
      ack <- ackTask(receiveRequest, out, client)
      _ <- ack match {
        case Ack.Stop => {
          println("Ack stop")
          out.onComplete()
          Task.unit
        }
        case Ack.Continue => {
          println("Ack continue")
          runLoop(out, client)
        }
      }
    } yield ()
    }.onErrorHandle{ ex =>
    println("exception found on SqsSource: " + ex.getMessage)
    out.onError(ex)
  }

  def ackTask(request: ReceiveMessageRequest, subscriber: Subscriber[Message], client: SqsAsyncClient): Task[Ack] = {
    for {
      response <- asyncClient.flatMap(client => Task.from(client.receiveMessage(request)))
      feed <- {
        Task.create[Ack] { (scheduler, cb) =>
          val f = Observer.feed(subscriber, response.messages.asScala)(scheduler)
          f.onComplete {
            case Success(ack) => {
              println(s"Success message sent: ${ack}")
              cb.onSuccess(ack)
            }
            case Failure(ex) => {
              println("Error message sent")
              cb.onError(ex)
            }
          }(scheduler)
        }
      }
    } yield feed
  }

}

object SqsSource {
  def apply(
    queueUrl: String,
    settings: SqsSourceSettings = DefaultSourceSettings)(
    implicit sqsAsyncClient: Task[SqsAsyncClient]): SqsSource = new SqsSource(queueUrl, settings, sqsAsyncClient)
}
