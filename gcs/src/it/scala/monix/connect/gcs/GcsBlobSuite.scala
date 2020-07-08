package monix.connect.gcs

import java.io.FileInputStream
import java.nio.channels.Channels

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.{Acl, Bucket, BucketInfo, Storage, StorageOptions, Option => _}
import monix.connect.gcs.GcsBucket
import monix.connect.gcs.configuration.GcsBucketInfo
import monix.execution.Scheduler.Implicits.global
import org.mockito.Mockito.{times, verify}
import org.mockito.MockitoSugar.when
import org.mockito.{ArgumentMatchersSugar, IdiomaticMockito}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import com.google.cloud.{ByteArray, NoCredentials}
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper
import monix.reactive.Observable

import scala.jdk.CollectionConverters._

class GcsBlobSuite extends AnyWordSpecLike with IdiomaticMockito with Matchers with ArgumentMatchersSugar {

// val opts = StorageOptions
//   .newBuilder()
//   .setProjectId("projectId")
//   .setCredentials(NoCredentials.getInstance())
//   .setHost("http://127.0.0.1:9000").build()

// new GcsStorage(opts.getService).createBucket("sample", "DEFAULT_REGION", None).runSyncUnsafe()
//  val gcsStorage: GcsStorage = GcsStorage.create()

//  gcsStorage.createBucket("bucketA", GcsBucketInfo.Locations.`EUROPE-WEST3`, None).runSyncUnsafe()

    val storage = LocalStorageHelper.getOptions.getService

  import com.google.cloud.storage.BlobId
  import com.google.cloud.storage.BlobInfo

  val blobPath = "/blob/path"
  val testBucketName = "test-bucket"
  val blobInfo: BlobInfo = BlobInfo.newBuilder(BlobId.of(testBucketName, blobPath)).build

  import com.google.cloud.storage.BlobInfo
  import java.nio.charset.StandardCharsets
  import java.util.stream.StreamSupport

  val blob = storage.create(blobInfo, "randomContent".getBytes(StandardCharsets.UTF_8))
  println("Listed buckets" + storage.list(testBucketName).getValues.asScala.toList.mkString)

  val gcsBlob = new GcsBlob(blob)


  println("Exist: " + ByteArray.copyFrom(blob.getContent()).toStringUtf8)

  val readChannel = storage.reader(BlobId.of(testBucketName, blobPath ))
  println("Read conent: " + ByteArray.copyFrom(Observable.fromInputStreamUnsafe(Channels.newInputStream(readChannel)).headL.runSyncUnsafe()).toStringUtf8)
  //val gcs = new GcsStorage(storage).createBucket("sample", "DEFAULT_REGION", None).runSyncUnsafe()

   //println("Bucket info: " + gcs.bucketInfo)


  // s"$GcsBucket" should {
//
 //   "implement an async exists operation" in {
 //     //given
 //     val bucketSourceOption: BucketSourceOption = mock[BucketSourceOption]
 //     when(underlying.exists(bucketSourceOption)).thenAnswer(true)
//
 //     //when
 //     val result: Boolean = bucket.exists(bucketSourceOption).runSyncUnsafe()
//
 //     //then
 //     result shouldBe true
 //     verify(underlying, times(1)).exists(bucketSourceOption)
 //   }
 //
 // }
}
