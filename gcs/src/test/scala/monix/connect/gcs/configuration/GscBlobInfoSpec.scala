package monix.connect.gcs.configuration

import com.google.cloud.ReadChannel
import com.google.cloud.storage.{BlobId, Storage, Blob => GoogleBlob, Option => _}
import monix.connect.gcs.GscFixture
import org.mockito.IdiomaticMockito
import org.scalatest.matchers.should.Matchers
import org.scalacheck.Gen
import org.scalatest.wordspec.AnyWordSpecLike

import scala.jdk.CollectionConverters._

class GscBlobInfoSpec extends AnyWordSpecLike with IdiomaticMockito with Matchers with GscFixture {

  val underlying: GoogleBlob = mock[GoogleBlob]
  val mockStorage: Storage = mock[Storage]
  val readChannel: ReadChannel = mock[ReadChannel]

  s"${GcsBlobInfo}" can {

    "be created from method `withMetadata`" in   {
      //given
      val bucketName = Gen.alphaLowerStr.sample.get
      val blobName = Gen.alphaLowerStr.sample.get
      val metadata = genBlobInfoMetadata.sample.get

      //when
      val blobInfo = GcsBlobInfo.withMetadata(bucketName, blobName, Some(metadata))

      //then
      Option(blobInfo.getContentType) shouldBe metadata.contentType
      Option(blobInfo.getContentDisposition) shouldBe metadata.contentDisposition
      Option(blobInfo.getContentLanguage) shouldBe metadata.contentLanguage
      Option(blobInfo.getContentEncoding) shouldBe metadata.contentEncoding
      Option(blobInfo.getCacheControl) shouldBe metadata.cacheControl
      //Option(blobInfo.getCrc32c) shouldBe metadata.crc32c todo revise why value is not forwarded
      Option(blobInfo.getCrc32cToHexString) shouldBe metadata.crc32cFromHexString.map(_.toLowerCase)
      //Option(blobInfo.getMd5) shouldBe metadata.md5 todo revise why value is not forwarded
      Option(blobInfo.getMd5ToHexString) shouldBe metadata.md5FromHexString.map(_.toLowerCase)
      Option(blobInfo.getStorageClass) shouldBe metadata.storageClass
      Option(blobInfo.getTemporaryHold) shouldBe metadata.temporaryHold
      Option(blobInfo.getEventBasedHold) shouldBe metadata.eventBasedHold
      blobInfo.getAcl shouldBe metadata.acl.asJava
      blobInfo.getMetadata shouldBe metadata.metadata.asJava
    }
  }

}
