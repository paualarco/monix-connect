package scalona.monix.connectors.grpc

import io.grpc.stub.StreamObserver
import monix.eval.Task
import monix.execution.Ack.{Continue, Stop}
import monix.execution.{Ack, Scheduler}
import scalona.monix.connectors.grpc.protobuf.test.{GrpcProtocolGrpc, JoinReply, JoinRequest}

import scala.concurrent.Future
import monix.execution.Scheduler.Implicits.global
import monix.reactive.{MulticastStrategy, Observable, Observer}
import monix.reactive.observers.Subscriber
import monix.reactive.subjects.ConcurrentSubject
import scala.collection.mutable

object GrpcServer extends App {

  class GrpcProtocolImpl extends GrpcProtocolGrpc.GrpcProtocol {
    override def join(request: JoinRequest): Future[JoinReply] = {
      Task(JoinReply("a", true)).runToFuture
    }


  }
}
