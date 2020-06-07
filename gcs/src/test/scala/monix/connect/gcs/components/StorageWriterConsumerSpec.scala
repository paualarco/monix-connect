package monix.connect.gcs.components

import java.nio.ByteBuffer

import com.google.cloud.WriteChannel
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.mockito.IdiomaticMockito
import org.mockito.MockitoSugar.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import monix.execution.exceptions.DummyException

import scala.util.{Failure, Try}

class StorageWriterConsumerSpec extends AnyWordSpecLike with IdiomaticMockito with Matchers {

  s"${StorageWriterConsumer}" should {

    "write each passed chunk and return the total size" in {
      //given
      val writeChannel: WriteChannel = mock[WriteChannel]
      val firstChunk = "Hello".getBytes
      val secondChunk = "World".getBytes
      val thirdChunk = "!".getBytes
      val lastChunk = Array.emptyByteArray
      when(writeChannel.write(ByteBuffer.wrap(firstChunk))).thenReturn(firstChunk.length)
      when(writeChannel.write(ByteBuffer.wrap(secondChunk))).thenReturn(secondChunk.length)
      when(writeChannel.write(ByteBuffer.wrap(thirdChunk))).thenReturn(thirdChunk.length)
      when(writeChannel.write(ByteBuffer.wrap(lastChunk))).thenReturn(lastChunk.length)

      //when
      val t =
        Observable(firstChunk, secondChunk, thirdChunk, lastChunk).consumeWith(StorageWriterConsumer(writeChannel))

      //then
      val totalBytes: Long = t.runSyncUnsafe()
      val expectedWrittenBytes = (firstChunk.length + secondChunk.length + thirdChunk.length + lastChunk.length)
      totalBytes shouldBe expectedWrittenBytes
    }

    "correctly reports internal failures of any kind" in {

      //given
      val writeChannel: WriteChannel = mock[WriteChannel]
      val firstChunk = "Hello".getBytes
      val secondChunk = "World".getBytes
      val exception = DummyException("Boom!")
      when(writeChannel.write(ByteBuffer.wrap(firstChunk))).thenReturn(firstChunk.length)
      when(writeChannel.write(ByteBuffer.wrap(firstChunk))).thenThrow(DummyException("Boom!"))

      //when
      val t = Observable(firstChunk, secondChunk).consumeWith(StorageWriterConsumer(writeChannel))

      //then
      val maybeTotalBytes: Try[Long] = Try(t.runSyncUnsafe())
      maybeTotalBytes.isFailure shouldBe true
      maybeTotalBytes shouldEqual Failure(exception)
    }

    "correctly reports NullPointerException for null input elements" in {
      //given
      val writeChannel: WriteChannel = mock[WriteChannel]
      val firstChunk = "Hello".getBytes
      when(writeChannel.write(ByteBuffer.wrap(firstChunk))).thenReturn(firstChunk.length)
      when(writeChannel.write(ByteBuffer.wrap(Array.emptyByteArray))).thenReturn(0)

      //when
      val t = Observable(firstChunk, null).consumeWith(StorageWriterConsumer(writeChannel))

      //then
      val maybeTotalBytes: Try[Long] = Try(t.runSyncUnsafe())
      maybeTotalBytes.isFailure shouldBe true
    }
  }

}
