package monix.connect.sqs

import monix.connect.sqs.SqsOp.Implicits._
import monix.connect.sqs.domain.BatchMessageEntry
import monix.execution.Scheduler.Implicits.global
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.util.Try

class SqsOpSpec extends AnyFlatSpecLike with Matchers with ScalaFutures with SqsFixture with BeforeAndAfterEach {

  implicit val defaultConfig: PatienceConfig = PatienceConfig(2.seconds, 300.milliseconds)

  val queueName: String = "sample"//genQueueName.sample.get
  val queueUrl = getQueueUrl(queueName)

  s"${SqsOp}.create()" should "create a sendMessageRequest` and materialize to `Unit`" in {
    //given
    val body: String = genMessageBody.sample.get
    val request = sendMessageRequest(queueUrl = queueUrl, messageBody = body)

    //when
    SqsOp.create(request).runSyncUnsafe()
  }

  it should "create batch requests tasks" in  {
    //given
    val body: String = genMessageBody.sample.get
    //val batchRequest = SqsRequestBuilder.sendBatchMessage(queueUrl, 0.seconds, "group-id", Map.empty, Map.empty)(List(BatchMessageEntry(body, "1"), BatchMessageEntry(body, "2")))

    //when
    //SqsOp.create(batchRequest).runSyncUnsafe()
  }

  override def beforeEach() = {
    super.beforeEach()
    Try(SqsOp.create(createQueueRequest(queueName)).runSyncUnsafe())
  }

  override def afterEach() = {
    super.afterEach()
    SqsOp.create(deleteQueueRequest(queueUrl)).runToFuture
  }

}
