package monix.connect.gcs.components

import java.nio.ByteBuffer

import com.google.cloud.WriteChannel
import com.google.cloud.storage.{BlobInfo, Storage}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.mockito.IdiomaticMockito
import org.mockito.MockitoSugar.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import monix.execution.exceptions.DummyException

import scala.util.{Failure, Try}

class GscUploaderSpec extends AnyWordSpecLike with IdiomaticMockito with Matchers {

  val storage = mock[Storage]
  val blobInfo: BlobInfo = BlobInfo.newBuilder("myBucket", "id123").build()
  s"${GcsUploader}" should {

  //"write each passed chunk and return the total size" in {
  //   //given
  //   val writeChannel: WriteChannel = mock[WriteChannel]
  //   val firstChunk = "Hello".getBytes
  //   val secondChunk = "World".getBytes
  //   val thirdChunk = "!".getBytes
  //   val lastChunk = Array.emptyByteArray
  //   when(writeChannel.write(ByteBuffer.wrap(firstChunk))).thenReturn(firstChunk.length)
  //   when(writeChannel.write(ByteBuffer.wrap(secondChunk))).thenReturn(secondChunk.length)
  //   when(writeChannel.write(ByteBuffer.wrap(thirdChunk))).thenReturn(thirdChunk.length)
  //   when(writeChannel.write(ByteBuffer.wrap(lastChunk))).thenReturn(lastChunk.length)

  //   //when
  //   val t =
  //     Observable(firstChunk, secondChunk, thirdChunk, lastChunk)
  //       .consumeWith(GcsUploader(storage, blobInfo))

  //   //then
  //   val r = t.runSyncUnsafe()
  //   r shouldBe an[Unit]
  //   //val writtenBytes = (firstChunk.length + secondChunk.length + thirdChunk.length + lastChunk.length)
  //   //totalBytes shouldBe writtenBytes
  // }

    //"correctly reports internal failures of any kind" in {
//
    //  //given
    //  val writeChannel: WriteChannel = mock[WriteChannel]
    //  val firstChunk = "Hello".getBytes
    //  val secondChunk = "World".getBytes
    //  val exception = DummyException("Boom!")
    //  when(writeChannel.write(ByteBuffer.wrap(firstChunk))).thenReturn(firstChunk.length)
    //  when(writeChannel.write(ByteBuffer.wrap(firstChunk))).thenThrow(DummyException("Boom!"))
//
    //  //when
    //  val t = Observable(firstChunk, secondChunk).consumeWith(Storage(writeChannel))
//
    //  //then
    //  val maybeTotalBytes: Try[Long] = Try(t.runSyncUnsafe())
    //  maybeTotalBytes.isFailure shouldBe true
    //  maybeTotalBytes shouldEqual Failure(exception)
    //}
//
    //"correctly reports NullPointerException for null input elements" in {
    //  //given
    //  val writeChannel: WriteChannel = mock[WriteChannel]
    //  val firstChunk = "Hello".getBytes
    //  when(writeChannel.write(ByteBuffer.wrap(firstChunk))).thenReturn(firstChunk.length)
    //  when(writeChannel.write(ByteBuffer.wrap(Array.emptyByteArray))).thenReturn(0)
//
    //  //when
    //  val t = Observable(firstChunk, null).consumeWith(Storage(writeChannel))
//
    //  //then
    //  val maybeTotalBytes: Try[Long] = Try(t.runSyncUnsafe())
    //  maybeTotalBytes.isFailure shouldBe true
    //}
  }

}
