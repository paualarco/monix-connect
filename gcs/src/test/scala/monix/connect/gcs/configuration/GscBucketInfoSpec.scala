package monix.connect.gcs.configuration

import com.google.cloud.ReadChannel
import com.google.cloud.storage.{Blob, Bucket, Storage, Option => _}
import monix.connect.gcs.GscFixture
import org.mockito.IdiomaticMockito
import org.scalacheck.Gen
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.jdk.CollectionConverters._

class GscBucketInfoSpec extends AnyWordSpecLike with IdiomaticMockito with Matchers with GscFixture {

  val underlying: Bucket = mock[Bucket]
  val mockStorage: Storage = mock[Storage]
  val readChannel: ReadChannel = mock[ReadChannel]

  s"${GcsBucketInfo}" can {

    "be created from method `withMetadata`" in   {
      //given
      val bucketName = Gen.alphaLowerStr.sample.get
      val location = GcsBucketInfo.Locations.`ASIA-EAST1`
      val metadata = genBucketInfoMetadata.sample.get

      //when
      val bucketInfo = GcsBucketInfo.withMetadata(bucketName, location, Some(metadata))

      //then
      Option(bucketInfo.getStorageClass) shouldBe metadata.storageClass
      Option(bucketInfo.getLogging) shouldBe metadata.logging
      Option(bucketInfo.getRetentionPeriod) shouldBe metadata.retentionPeriod.map(_.toMillis)
      Option(bucketInfo.versioningEnabled) shouldBe metadata.versioningEnabled
      Option(bucketInfo.requesterPays) shouldBe metadata.requesterPays
      Option(bucketInfo.getDefaultEventBasedHold) shouldBe metadata.defaultEventBasedHold
      bucketInfo.getAcl shouldBe metadata.acl.asJava
      bucketInfo.getDefaultAcl shouldBe metadata.defaultAcl.asJava
      bucketInfo.getCors shouldBe metadata.cors.asJava
      bucketInfo.getLifecycleRules shouldBe metadata.lifecycleRules.asJava
      Option(bucketInfo.getIamConfiguration) shouldBe metadata.iamConfiguration
      Option(bucketInfo.getDefaultKmsKeyName) shouldBe metadata.defaultKmsKeyName
      bucketInfo.getLabels shouldBe metadata.labels.asJava
      Option(bucketInfo.getIndexPage) shouldBe metadata.indexPage
      Option(bucketInfo.getNotFoundPage) shouldBe metadata.notFoundPage
    }
  }

}
