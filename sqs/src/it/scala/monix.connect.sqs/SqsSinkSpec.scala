package monix.connect.sqs

import monix.connect.sqs.SqsOp.Implicits._
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class SqsSinkSpec extends AnyFlatSpecLike with Matchers with ScalaFutures with SqsFixture with BeforeAndAfterEach {

  implicit val defaultConfig: PatienceConfig = PatienceConfig(10.seconds, 300.milliseconds)

  val queueName: String = "my-queue"//genQueueName.sample.get
  val queueUrl = getQueueUrl(queueName)

  s"${SqsSink}.sink()" should "sink a single `SendMessageRequest` and materialize to `Unit`" in {
    //given
    val body: String = genMessageBody.sample.get
    val groupId: String = "1234"//genMessageBody.sample.get

    //when
    val getRequest = SqsRequestBuilder.receiveRequest(queueUrl)
    //val f = SqsOp.create(getRequest).runToFuture
    val f = SqsSource(queueUrl)(Task.coeval(coAsyncClient)).firstL.runToFuture

    //and
    val t = Observable.fromIterable(List(genMessageBody.sample.get, genMessageBody.sample.get)).consumeWith(SqsSink(queueUrl, groupId)(coAsyncClient.value()))


    //then
    whenReady(t.runToFuture) { r =>
      r shouldBe a[Unit]
      body shouldBe Await.result(f, Duration.Inf)
      //SqsOp.create(getRequest).runSyncUnsafe()
      //body shouldBe f.value.get.get.messages()
    }
  }

  override def beforeEach() = {
    super.beforeEach()
    Try(SqsOp.create(createQueueRequest(queueName)).runSyncUnsafe()) match {
      case Success(value) => info(s"The queue $queueName was created correclty.")
      case Failure(exception) => println(s"Failed to create queue $queueName")
    }
  }

  override def afterEach() = {
    super.afterEach()
    //Try(SqsOp.create(deleteQueueRequest(queueUrl)).runSyncUnsafe())
  }

}
