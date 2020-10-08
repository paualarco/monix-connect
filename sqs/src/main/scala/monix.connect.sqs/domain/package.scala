package monix.connect.sqs

import scala.concurrent.duration.FiniteDuration

import scala.concurrent.duration._

package object domain {

  private[sqs] val DefaultSourceSettings = SqsSourceSettings()

  /**
  *
    * @param attributeNames
    * @param maxNumberOfMessages
    * @param messageAttributeNames
    * @param visibilityTimeout
    *                          https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-visibility-timeout.html
    * @param waitTimeSeconds
    * @param autoDelete
    * @param stopWhenQueueEmpty
    */
  case class SqsSourceSettings(attributeNames: List[String] = List.empty,
                               maxNumberOfMessages: Int = 1,
                               pollInterval: FiniteDuration = 0.seconds,
                               messageAttributeNames: List[String] = List.empty,
                               visibilityTimeout: FiniteDuration = 1.seconds,
                               waitTimeSeconds: FiniteDuration = 1.seconds,
                               autoDelete: Boolean = true,
                               stopWhenQueueEmpty: Boolean = false)


  case class BatchMessageEntry(body: String, id: String)
}

