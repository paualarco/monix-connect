package monix.connect.gcs.components

import java.nio.ByteBuffer

import com.google.cloud.WriteChannel
import monix.execution.{Ack, Callback, Scheduler}
import monix.execution.cancelables.AssignableCancelable
import monix.reactive.Consumer
import monix.reactive.observers.{SafeSubscriber, Subscriber}

import scala.concurrent.Future
import scala.util.control.NonFatal

private[gcs] final class StorageWriterConsumer(channel: WriteChannel) extends Consumer[Array[Byte], Long] {
  override def createSubscriber(cb: Callback[Throwable, Long], s: Scheduler): (Subscriber[Array[Byte]], AssignableCancelable) = {
    val out = SafeSubscriber(new Subscriber[Array[Byte]] { self =>

      var writtenBytes: Long = 0L
      override implicit def scheduler: Scheduler = s

      override def onNext(elem: Array[Byte]): Future[Ack] =
        Future{
          writtenBytes += channel.write(ByteBuffer.wrap(elem))
        }
          .map(_ => Ack.Continue)
          .recover {
            case NonFatal(ex) => {
              onError(ex)
              Ack.Stop
            }
          }

      override def onError(ex: Throwable): Unit =
        cb.onError(ex)

      override def onComplete(): Unit =
        cb.onSuccess((writtenBytes))
    })

    (out, AssignableCancelable.dummy)
  }
}

object StorageWriterConsumer {
  def apply(channel: WriteChannel): StorageWriterConsumer = new StorageWriterConsumer(channel)
}