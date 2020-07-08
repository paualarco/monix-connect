package monix.connect.gcs

import java.io.{File, FileInputStream}
import java.nio.channels.Channels
import java.nio.file.Path

import com.google.cloud.storage.{Acl, Blob, BlobId, BlobInfo, Bucket, BucketInfo, Storage, StorageOptions, Option => _}
import monix.execution.Scheduler.Implicits.global
import org.mockito.Mockito.{times, verify}
import org.mockito.MockitoSugar.when
import org.mockito.{ArgumentMatchersSugar, IdiomaticMockito}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import com.google.cloud.{ByteArray, NoCredentials}
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper
import monix.reactive.Observable
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import monix.eval.Task
import org.scalacheck.Gen

import scala.jdk.CollectionConverters._

class GcsBlobSuite extends AnyWordSpecLike with IdiomaticMockito with Matchers with ArgumentMatchersSugar {

  val storage = LocalStorageHelper.getOptions.getService

  val nonEmptyString: Gen[String] = Gen.nonEmptyListOf(Gen.alphaChar).map(chars => "test-" + chars.mkString.take(20))
  s"${GcsBlob}" should {

    "return true if exists" in {
      //given
      val blobPath = nonEmptyString.sample.get
      val testBucketName = nonEmptyString.sample.get
      val blobInfo: BlobInfo = BlobInfo.newBuilder(BlobId.of(testBucketName, blobPath)).build
      val content: Array[Byte] = nonEmptyString.sample.get.getBytes()
      val blob: Blob = storage.create(blobInfo, content)
      val gcsBlob = new GcsBlob(blob)

      //when
      val t = gcsBlob.exists()

      //then
      t.runSyncUnsafe() shouldBe true
    }

    "return delete if exists" in {
      //given
      val blobPath = nonEmptyString.sample.get
      val testBucketName = nonEmptyString.sample.get
      val blobInfo: BlobInfo = BlobInfo.newBuilder(BlobId.of(testBucketName, blobPath)).build
      val content: Array[Byte] = nonEmptyString.sample.get.getBytes()
      val blob: Blob = storage.create(blobInfo, content)
      val gcsBlob = new GcsBlob(blob)
      val existedBefore = gcsBlob.exists().runSyncUnsafe()

      //when
      val t = gcsBlob.delete()

      //then
      val deleted = t.runSyncUnsafe()
      val existsAfterDeletion = gcsBlob.exists().runSyncUnsafe()
      existedBefore shouldBe true
      deleted shouldBe true
      existsAfterDeletion shouldBe false
    }

    "download to file" in {
      //given
      val blobPath = "nonEmptyString.sample.get"
      val testBucketName = nonEmptyString.sample.get
      val blobInfo: BlobInfo = BlobInfo.newBuilder(BlobId.of(testBucketName, blobPath)).build
      val content: Array[Byte] = nonEmptyString.sample.get.getBytes()
      val blob: Blob = storage.create(blobInfo, content)

      val path: Path = new File("gcs/test.txt").toPath
      val gcsBlob = new GcsBlob(blob)

      //when
      val t: Task[Unit] = gcsBlob.downloadToFile(path)

      //then

      t.runSyncUnsafe()
    }

  }
  //println("Exist: " + ByteArray.copyFrom(blob.getContent()).toStringUtf8)

  //val readChannel = storage.reader(BlobId.of(testBucketName, blobPath ))
  //println("Read conent: " + ByteArray.copyFrom(Observable.fromInputStreamUnsafe(Channels.newInputStream(readChannel)).headL.runSyncUnsafe()).toStringUtf8)
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
