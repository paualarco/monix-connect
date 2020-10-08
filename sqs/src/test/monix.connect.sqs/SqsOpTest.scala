package monix.connect.sqs

import monix.eval.Task
import org.mockito.IdiomaticMockito
import org.mockito.MockitoSugar.when
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.{AddPermissionRequest, AddPermissionResponse, ChangeMessageVisibilityRequest, ChangeMessageVisibilityResponse, CreateQueueRequest, CreateQueueResponse, DeleteMessageRequest, DeleteMessageResponse, DeleteQueueRequest, DeleteQueueResponse, GetQueueUrlRequest, GetQueueUrlResponse, ListQueuesRequest, ListQueuesResponse, Message, ReceiveMessageRequest, ReceiveMessageResponse, SendMessageBatchRequest, SendMessageBatchRequestEntry, SendMessageBatchResponse, SendMessageBatchResultEntry, SendMessageRequest, SendMessageResponse}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import monix.execution.Scheduler.Implicits.global

import scala.compat.java8.FutureConverters._

class SqsOpTest extends AnyFlatSpec with IdiomaticMockito with Matchers {

  implicit val sqsClient = mock[SqsAsyncClient]

  "SqsOp" should "send messages " in {
    //given
    val request = SendMessageRequest.builder.messageBody("dummy body").build()
    val expectedResponse = SendMessageResponse.builder.messageId("dummyId").build()

    //when
    when(sqsClient.sendMessage(request)).thenReturn(Task(expectedResponse).runToFuture.toJava.toCompletableFuture)
    import monix.connect.sqs.SqsOp.Implicits.sendMessage
    val r = SqsOp.create(request)(sendMessage, sqsClient).runSyncUnsafe()

    //then
    r shouldBe expectedResponse
  }

  it should "send messages in batches" in {
    //given
    val batchEntryRequest = SendMessageBatchRequestEntry.builder.id("id").messageBody("dummyBody").build()
    val batchEntryResponse = SendMessageBatchResultEntry.builder.id("id").messageId("messageId").build()
    val sendBatchRequest = SendMessageBatchRequest.builder.entries(batchEntryRequest).build()
    val expectedResponse = SendMessageBatchResponse.builder.successful(batchEntryResponse).build()

    //when
    when(sqsClient.sendMessageBatch(sendBatchRequest))
      .thenReturn(Task(expectedResponse).runToFuture.toJava.toCompletableFuture)
    import monix.connect.sqs.SqsOp.Implicits.sendMessageBatch
    val r = SqsOp.create(sendBatchRequest)(sendMessageBatch, sqsClient).runSyncUnsafe()

    //then
    r shouldBe expectedResponse
  }

  it should "add permission" in {
    //given
    val addPermissionRequest = AddPermissionRequest.builder.actions("*").awsAccountIds("sample").build()
    val expectedResponse = AddPermissionResponse.builder.build()

    //when
    import monix.connect.sqs.SqsOp.Implicits.addPermission
    when(sqsClient.addPermission(addPermissionRequest))
      .thenReturn(Task(expectedResponse).runToFuture.toJava.toCompletableFuture)
    val r = SqsOp.create(addPermissionRequest)(addPermission, sqsClient).runSyncUnsafe()

    //then
    r shouldBe expectedResponse
  }

  it should "create queue" in {
    //given
    val createQueueRequest = CreateQueueRequest.builder.queueName("my-queue-name").build()
    val expectedResponse = CreateQueueResponse.builder.queueUrl("my-queue-url").build()

    //when
    import monix.connect.sqs.SqsOp.Implicits.createQueue
    when(sqsClient.createQueue(createQueueRequest))
      .thenReturn(Task(expectedResponse).runToFuture.toJava.toCompletableFuture)
    val r = SqsOp.create(createQueueRequest)(createQueue, sqsClient).runSyncUnsafe()

    //then
    r shouldBe expectedResponse
  }

  it should "delete message" in {
    //given
    val deleteMessageRequest =
      DeleteMessageRequest.builder.queueUrl("my-queue-url").receiptHandle("recipe-handle").build()
    val expectedResponse = DeleteMessageResponse.builder.build()

    //when
    import monix.connect.sqs.SqsOp.Implicits.deleteMessage
    when(sqsClient.deleteMessage(deleteMessageRequest))
      .thenReturn(Task(expectedResponse).runToFuture.toJava.toCompletableFuture)
    val r = SqsOp.create(deleteMessageRequest)(deleteMessage, sqsClient).runSyncUnsafe()

    //then
    r shouldBe expectedResponse
  }

  it should "delete queue" in {
    //given
    val deleteQueueRequest = DeleteQueueRequest.builder.queueUrl("my-queue-url").build()
    val expectedResponse = DeleteQueueResponse.builder.build()

    //when
    import monix.connect.sqs.SqsOp.Implicits.deleteQueue
    when(sqsClient.deleteQueue(deleteQueueRequest))
      .thenReturn(Task(expectedResponse).runToFuture.toJava.toCompletableFuture)
    val r = SqsOp.create(deleteQueueRequest)(deleteQueue, sqsClient).runSyncUnsafe()

    //then
    r shouldBe expectedResponse
  }

  it should "get queue url" in {
    //given
    val getQueueUrlRequest =
      GetQueueUrlRequest.builder.queueName("my-queue-name").queueOwnerAWSAccountId("owner-id").build()
    val expectedResponse = GetQueueUrlResponse.builder.queueUrl("my-queue-url").build()

    //when
    import monix.connect.sqs.SqsOp.Implicits.getQueueUrl
    when(sqsClient.getQueueUrl(getQueueUrlRequest))
      .thenReturn(Task(expectedResponse).runToFuture.toJava.toCompletableFuture)
    val r = SqsOp.create(getQueueUrlRequest)(getQueueUrl, sqsClient).runSyncUnsafe()

    //then
    r shouldBe expectedResponse
  }

  it should "list queues" in {
    //given
    val listQueuesRequest = ListQueuesRequest.builder.queueNamePrefix("prefix").build()
    val expectedResponse = ListQueuesResponse.builder.queueUrls("my-queue-url").build()

    //when
    import monix.connect.sqs.SqsOp.Implicits.listQueues
    when(sqsClient.listQueues(listQueuesRequest))
      .thenReturn(Task(expectedResponse).runToFuture.toJava.toCompletableFuture)
    val r = SqsOp.create(listQueuesRequest)(listQueues, sqsClient).runSyncUnsafe()

    //then
    r shouldBe expectedResponse
  }

  it should "receive message" in {
    //given
    val message = Message.builder.body("dummy-body").build()
    val receiveMessageRequest = ReceiveMessageRequest.builder.build()
    val expectedResponse = ReceiveMessageResponse.builder.messages(message)build()

    //when
    import monix.connect.sqs.SqsOp.Implicits.receiveMessage
    when(sqsClient.receiveMessage(receiveMessageRequest))
      .thenReturn(Task(expectedResponse).runToFuture.toJava.toCompletableFuture)
    val r = SqsOp.create(receiveMessageRequest)(receiveMessage, sqsClient).runSyncUnsafe()

    //then
    r shouldBe expectedResponse
  }

  it should "change message visibility" in {
    //given
    val changeVisibilityRequest = ChangeMessageVisibilityRequest.builder.queueUrl("my-queue").visibilityTimeout(1).build()
    val expectedResponse = ChangeMessageVisibilityResponse.builder.build()

    //when
    import monix.connect.sqs.SqsOp.Implicits.changeMessageVisibility
    when(sqsClient.changeMessageVisibility(changeVisibilityRequest))
      .thenReturn(Task(expectedResponse).runToFuture.toJava.toCompletableFuture)
    val r = SqsOp.create(changeVisibilityRequest)(changeMessageVisibility, sqsClient).runSyncUnsafe()

    //then
    r shouldBe expectedResponse
  }

}
