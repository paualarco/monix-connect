package monix.connect.sqs

import monix.eval.Task
import monix.reactive.{Consumer, Observable, OverflowStrategy}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model._
import monix.connect.sqs.SqsOp.Implicits._
import monix.execution.Scheduler.Implicits.global
import org.scalatest.flatspec.AnyFlatSpecLike

import scala.concurrent.duration._
import scala.util.{Failure, Try}

class SqsBatchSinkSpec extends AnyFlatSpecLike with Matchers with ScalaFutures with SqsFixture with BeforeAndAfterEach {

  implicit val defaultConfig: PatienceConfig = PatienceConfig(2.seconds, 300.milliseconds)

  val queueName: String = "sample"//genQueueName.sample.get
  val queueUrl = getQueueUrl(queueName)

  s"${SqsBatchSink}.sink()" should "sink a single `SendMessageRequest` and materialize to `Unit`" in {
    //given
    val body: String = genMessageBody.sample.get
    val request = sendMessageRequest(queueUrl = queueUrl, messageBody = body)
    SqsOp.create(request).runSyncUnsafe()

    //when
   // val t = Observable.pure(List(genMessageBody.sample.get)).consumeWith(SqsSink(queueUrl, "", 1)).runSyncUnsafe()

    //then
    //whenReady(t.runToFuture) { r =>
    //  r shouldBe a[Unit]
    //  val message = Sqs.source(queueUrl).firstL.runSyncUnsafe()
    //  body shouldBe message.body()
    //}
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
