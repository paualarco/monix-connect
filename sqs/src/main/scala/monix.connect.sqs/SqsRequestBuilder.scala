package monix.connect.sqs

import monix.connect.sqs.domain.{BatchMessageEntry, SqsSourceSettings}
import software.amazon.awssdk.services.sqs.model.{MessageAttributeValue, MessageSystemAttributeNameForSends, MessageSystemAttributeValue, ReceiveMessageRequest, SendMessageBatchRequest, SendMessageBatchRequestEntry, SendMessageRequest}

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

object SqsRequestBuilder {

  def receiveRequest(queueUrl: String, sqsSourceSettings: SqsSourceSettings = domain.DefaultSourceSettings) = {
    ReceiveMessageRequest.builder
      .queueUrl(queueUrl)
      .attributeNamesWithStrings(sqsSourceSettings.attributeNames.asJava)
      .messageAttributeNames(sqsSourceSettings.messageAttributeNames.asJava)
      .maxNumberOfMessages(sqsSourceSettings.maxNumberOfMessages)
      .waitTimeSeconds(sqsSourceSettings.waitTimeSeconds.toSeconds.toInt)
      .visibilityTimeout(sqsSourceSettings.visibilityTimeout.toSeconds.toInt).build()
  }

  def sendMessageRequest(queueUrl: String,
                         delay: Option[FiniteDuration] = Option.empty,
                         groupId: String,
                         attributes: Map[String, MessageAttributeValue],
                           systemAttributes: Map[MessageSystemAttributeNameForSends, MessageSystemAttributeValue])
                        (messageBody: String, deduplicationId: Option[String]) = {
    val request = SendMessageRequest.builder
      .queueUrl(queueUrl)
      .messageBody(messageBody)
      //.messageGroupId(groupId)
      //.messageSystemAttributes(systemAttributes.asJava)
      //.messageAttributes(attributes.asJava)
      //.messageDeduplicationId("da")
      //delay.map(d => request.delaySeconds(d.toSeconds.toInt))
      //deduplicationId.map(request.messageDeduplicationId(_))
    request.build()
  }

  def messageBatchEntry(messageBody: String, deduplcationId: String, groupId: String, delayInSeconds: FiniteDuration,
                        messageAttributes: Map[String, MessageAttributeValue],
                        systemAttributes: Map[MessageSystemAttributeNameForSends, MessageSystemAttributeValue]): SendMessageBatchRequestEntry = {
    val builder = SendMessageBatchRequestEntry
      .builder
      .messageBody(messageBody)
      //.delaySeconds(delayInSeconds.toSeconds.toInt)
      //.messageAttributes(messageAttributes.asJava)
      //.messageSystemAttributes(systemAttributes.asJava)
      .messageGroupId(groupId)
      .messageDeduplicationId(deduplcationId)
    builder.build()
  }

  def sendBatchMessage(queueUrl: String,
                       delay: FiniteDuration = 0.seconds,
                       groupId: String,
                       messageAttributes: Map[String, MessageAttributeValue],
                       systemAttributes: Map[MessageSystemAttributeNameForSends, MessageSystemAttributeValue]
                       )(messages: List[BatchMessageEntry]): SendMessageBatchRequest = {
    val entries: Seq[SendMessageBatchRequestEntry] =
      messages.map( batchEntry => messageBatchEntry(batchEntry.body, batchEntry.id, groupId, delay, messageAttributes, systemAttributes))
    SendMessageBatchRequest.builder
      .entries(entries: _*)
      .queueUrl(queueUrl)
      .build()
  }

}
