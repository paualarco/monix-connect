package monix.connect.gcs

import java.io.{File, FileInputStream}
import java.nio.channels.Channels
import java.nio.file.{Files, Path, Paths}

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
import org.scalatest.BeforeAndAfterAll
import org.apache.commons.io.FileUtils

import scala.jdk.CollectionConverters._
import scala.util.Try

class GcsBlobSuite extends AnyWordSpecLike with IdiomaticMockito with Matchers with ArgumentMatchersSugar with BeforeAndAfterAll {

  val storage = LocalStorageHelper.getOptions.getService

  val nonEmptyString: Gen[String] = Gen.nonEmptyListOf(Gen.alphaChar).map(chars => "test-" + chars.mkString.take(20))

  val testBucketName = nonEmptyString.sample.get

  val dir = new File("gcs/tmp").toPath
  override def beforeAll(): Unit = {
    FileUtils.deleteDirectory(dir.toFile)
    Files.createDirectory(dir)
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    super.beforeAll()
  }

  s"${GcsBlob}" should {

    "return true if exists" in {
      //given
      val blobPath = nonEmptyString.sample.get
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

    "download a small blob in form of observable" in {
      //given
      val blobPath = nonEmptyString.sample.get
      val blobInfo: BlobInfo = BlobInfo.newBuilder(BlobId.of(testBucketName, blobPath)).build
      val content: Array[Byte] = nonEmptyString.sample.get.getBytes()
      val blob: Blob = storage.create(blobInfo, content)
      val gcsBlob = new GcsBlob(blob)

      //when
      val ob: Observable[Array[Byte]] = gcsBlob.download()
      val r: Array[Byte] = ob.headL.runSyncUnsafe()

      //then
      val exists = gcsBlob.exists().runSyncUnsafe()
      exists shouldBe true
      r shouldBe content
    }

    "download to file" in {
      //given
      val blobPath = nonEmptyString.sample.get
      val blobInfo: BlobInfo = BlobInfo.newBuilder(BlobId.of(testBucketName, blobPath)).build
      val content: Array[Byte] = nonEmptyString.sample.get.getBytes()
      val filePath: Path = new File(dir.toAbsolutePath.toString + "/" + nonEmptyString.sample.get).toPath
      val blob: Blob = storage.create(blobInfo, content)
      val gcsBlob = new GcsBlob(blob)

      //when
      val t: Task[Unit] = gcsBlob.downloadToFile(filePath)
      t.runSyncUnsafe()

      //then
      val exists = gcsBlob.exists().runSyncUnsafe()
      val r = Files.readAllBytes(filePath)
      exists shouldBe true
      r shouldBe content
    }

    "upload to the blob if it is empty" in {
      //given
      val blobPath = nonEmptyString.sample.get
      val blobInfo: BlobInfo = BlobInfo.newBuilder(BlobId.of(testBucketName, blobPath)).build
      val blob: Blob = storage.create(blobInfo)
      val gcsBlob = new GcsBlob(blob)
      val content: Array[Byte] = nonEmptyString.sample.get.getBytes()
      
      //when
      val ob: Observable[Array[Byte]] = gcsBlob.download()
      val contentBefore: Option[Array[Byte]] = ob.headOptionL.runSyncUnsafe()
      Observable.pure(content).consumeWith(gcsBlob.upload()).runSyncUnsafe()

      //then
      val exists = gcsBlob.exists().runSyncUnsafe()
      val actualContent: Array[Byte] = ob.headL.runSyncUnsafe()
      exists shouldBe true
      contentBefore.isEmpty shouldBe true
      actualContent shouldBe content
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
