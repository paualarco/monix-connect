package monix.connect.gcs.components

import java.nio.channels.Channels

import com.google.cloud.ReadChannel
import com.google.cloud.storage.{BlobId, Storage}
import monix.eval.Task
import monix.reactive.Observable

private[gcs] trait StorageDownloader {

  private def openReadChannel(storage: Storage, blobId: BlobId, chunkSize: Int): Observable[ReadChannel] = {
    Observable.resource {
      Task {
        val reader = storage.reader(blobId.getBucket, blobId.getName)
        reader.setChunkSize(chunkSize)
        reader
      }
    } { reader =>
      Task(reader.close())
    }
  }

  protected def download(storage: Storage, blobId: BlobId, chunkSize: Int): Observable[Array[Byte]] = {
    openReadChannel(storage, blobId, chunkSize).flatMap { channel =>
      Observable.fromInputStreamUnsafe(Channels.newInputStream(channel), chunkSize)
    }.takeWhile(_.nonEmpty)
  }
}