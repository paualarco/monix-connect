package monix.connect.sqs

import monix.connect.sqs.SqsOp.Implicits._
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import software.amazon.awssdk.services.sqs.model.Message

import scala.concurrent.duration._

class SqsSourceSpec extends AnyFlatSpecLike with Matchers with ScalaFutures with SqsFixture with BeforeAndAfterEach {

  implicit val defaultConfig: PatienceConfig = PatienceConfig(2.seconds, 300.milliseconds)

  val queueName: String = genQueueName.sample.get
  val queueUrl = getQueueUrl(queueName)

  s"${SqsSource}.apply" should "consume from the specified sqs queue" in {
    //given
    val n = 5

    SqsOp.create(genSendMessageRequest(queueName).sample.get)(sendMessage, asyncClient).runSyncUnsafe()
    val sendRequests = Gen.listOfN(n, genSendMessageRequest(queueName)).sample.get
    //Task.traverse(sendRequests)(sendRequest => SqsOp.create(sendRequest)).runSyncUnsafe()

    //when
   //val t = SqsSource.consume(queueUrl).toListL

   ////then
   //whenReady(t.runToFuture) { r =>
   //  r shouldBe a[List[Message]]
   //}
  }

  override def beforeEach() = {
    super.beforeEach()
    SqsOp.create(createQueueRequest(queueName)).runSyncUnsafe()
  }

  override def afterEach() = {
    super.afterEach()
    //SqsOp.create(deleteQueueRequest(queueUrl)).runSyncUnsafe()
  }

}
