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
import monix.reactive.{Consumer, Observable, OverflowStrategy}
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.{Message, MessageAttributeValue, MessageSystemAttributeNameForSends, MessageSystemAttributeValue, ReceiveMessageRequest, SendMessageRequest, SqsRequest, SqsResponse}
import monix.connect.sqs.domain.{DefaultSourceSettings, SqsSourceSettings}
import monix.execution.Cancelable
import monix.reactive.observers.Subscriber

import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters._

class SqsSource(queueUrl: String, settings: SqsSourceSettings = DefaultSourceSettings) extends Observable[Message] {

  override def unsafeSubscribeFn(subscriber: Subscriber[Message]): Cancelable = {


    val request = SqsRequestBuilder.receiveRequest(queueUrl, settings)

    def runLoop()
    Task.create { (scheduler, cb) =>

    }
  }

  /**
  *
    * @param queueUrl
    * @param settings
    * @param client
    * @return
    */
  def apply(queueUrl: String,
            settings: SqsSourceSettings = DefaultSourceSettings)(
    implicit
    client: SqsAsyncClient): Observable[Message] = {
    /*for {
      receiveRequest  <- {
        Observable
          .repeatEval(SqsRequestBuilder.receiveRequest(queueUrl, settings))
          .delayOnNext(settings.pollInterval)
      }
      response <- Observable.from(client.receiveMessage(receiveRequest))
      flatten  <- Observable.from(response.messages().asScala.toList)
    } yield flatten*/
    for {
      r <- Observable.repeat[ReceiveMessageRequest] {
        println("Preparing request")
        val builder = ReceiveMessageRequest.builder
          .queueUrl(queueUrl)
          .waitTimeSeconds(1)
          //.attributeNamesWithStrings(settings.attributeNames.asJava)
          //.messageAttributeNames(settings.messageAttributeNames.asJava)
          //.maxNumberOfMessages(settings.maxNumberOfMessages)

        //settings.visibilityTimeout.foreach(builder.visibilityTimeout(_))
        //builder.waitTimeSeconds(10)
        val req = builder.build()
        println("Preparing request: " + req)
        req
      }
      l <- Observable.fromTask { Task.from(client.receiveMessage(r)).map(_.messages().asScala.toList) }
      m <- Observable.suspend(Observable.fromIterable(l))
    } yield m
  }

  /**
  *
    * @param queue
    * @param settings
    * @param client
    * @return
    */
  def consume(queue: String, settings: SqsSourceSettings = DefaultSourceSettings)
             (implicit client: SqsAsyncClient): Observable[Message] = {
    apply(queue, settings)(client)
  }

}
