package monix.connect.gcs.components

import java.nio.ByteBuffer

import com.google.cloud.WriteChannel
import com.google.cloud.storage.{BlobInfo, Storage}
import com.google.cloud.storage.Storage.BlobWriteOption
import monix.eval.Task
import monix.execution.cancelables.AssignableCancelable
import monix.execution.{Ack, Callback, Scheduler}
import monix.reactive.Consumer
import monix.reactive.observers.{SafeSubscriber, Subscriber}

import scala.concurrent.Future
import scala.util.control.NonFatal

private[gcs] final class GcsWriterConsumer(storage: Storage, blobInfo: BlobInfo, chunkSize: Int, options: BlobWriteOption*) extends Consumer[Array[Byte], Long] {
  override def createSubscriber(cb: Callback[Throwable, Long], s: Scheduler): (Subscriber[Array[Byte]], AssignableCancelable) = {
    val out = SafeSubscriber(new Subscriber[Array[Byte]] {
      self =>

      val writer: WriteChannel = storage.writer(blobInfo, options: _*)
      writer.setChunkSize(chunkSize)

      var writtenBytes: Long = 0L

      override implicit def scheduler: Scheduler = s

      override def onNext(elem: Array[Byte]): Future[Ack] =
        Task {
          try {
            writtenBytes += writer.write(ByteBuffer.wrap(elem))
            monix.execution.Ack.Continue
          } catch {
            case ex if NonFatal(ex) => {
              onError(ex)
              Ack.Stop
            }
          }
        }.runToFuture

      override def onError(ex: Throwable): Unit = {
        writer.close()
        cb.onError(ex)
      }

      override def onComplete(): Unit =
        writer.close()
        cb.onSuccess((writtenBytes))
    })

    (out, AssignableCancelable.dummy)
  }
}

