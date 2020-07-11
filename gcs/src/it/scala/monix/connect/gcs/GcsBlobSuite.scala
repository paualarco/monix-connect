package monix.connect.gcs

import java.io.File
import java.nio.file.{Files, Path}

import com.google.cloud.storage.{Blob, Option => _}
import monix.execution.Scheduler.Implicits.global
import org.mockito.{ArgumentMatchersSugar, IdiomaticMockito}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper
import monix.reactive.Observable
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import monix.eval.Task
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterAll
import org.apache.commons.io.FileUtils

class GcsBlobSuite extends AnyWordSpecLike with IdiomaticMockito with Matchers with ArgumentMatchersSugar with BeforeAndAfterAll {

  val storage = LocalStorageHelper.getOptions.getService
  val dir = new File("gcs/tmp").toPath
  val nonEmptyString: Gen[String] = Gen.nonEmptyListOf(Gen.alphaChar).map(chars => "test-" + chars.mkString.take(20))
  val genLocalPath = nonEmptyString.map(s => dir.toAbsolutePath.toString + "/" + s)
  val testBucketName = nonEmptyString.sample.get

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
      val filePath: Path = new File(genLocalPath.sample.get).toPath
      val content: Array[Byte] = nonEmptyString.sample.get.getBytes()
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

    "upload to the blob" when {

      "it is empty" in {
        //given
        val blobPath = nonEmptyString.sample.get
        val blobInfo: BlobInfo = BlobInfo.newBuilder(BlobId.of(testBucketName, blobPath)).build
        val blob: Blob = storage.create(blobInfo)
        val gcsBlob = new GcsBlob(blob)
        val content: Array[Byte] = nonEmptyString.sample.get.getBytes()

        //when
        val downloader: Observable[Array[Byte]] = gcsBlob.download()
        val contentBefore: Option[Array[Byte]] = downloader.headOptionL.runSyncUnsafe()
        Observable.pure(content).consumeWith(gcsBlob.upload()).runSyncUnsafe()

        //then
        val exists = gcsBlob.exists().runSyncUnsafe()
        val r: Array[Byte] = downloader.headL.runSyncUnsafe()
        exists shouldBe true
        contentBefore.isEmpty shouldBe true
        r shouldBe content
      }

      "it is not empty" in {
        //given
        val blobPath = nonEmptyString.sample.get
        val blobInfo: BlobInfo = BlobInfo.newBuilder(BlobId.of(testBucketName, blobPath)).build
        val content: Array[Byte] = nonEmptyString.sample.get.getBytes()
        val blob: Blob = storage.create(blobInfo, content)
        val gcsBlob = new GcsBlob(blob)

        //when
        val downloader: Observable[Array[Byte]] = gcsBlob.download()
        val contentBefore: Option[Array[Byte]] = downloader.headOptionL.runSyncUnsafe()
        Observable.now(content).consumeWith(gcsBlob.upload()).runSyncUnsafe()

        //then
        val exists = gcsBlob.exists().runSyncUnsafe()
        val r: Array[Byte] = downloader.headL.runSyncUnsafe()
        exists shouldBe true
        contentBefore.isEmpty shouldBe false
        r shouldBe content
      }

      "the source observable is empty" in {
        //given
        val blobPath = nonEmptyString.sample.get
        val blobInfo: BlobInfo = BlobInfo.newBuilder(BlobId.of(testBucketName, blobPath)).build
        val blob: Blob = storage.create(blobInfo)
        val gcsBlob = new GcsBlob(blob)

        //when
        val downloader: Observable[Array[Byte]] = gcsBlob.download()
        val contentBefore: Option[Array[Byte]] = downloader.headOptionL.runSyncUnsafe()
        Observable.pure(Array.emptyByteArray).consumeWith(gcsBlob.upload()).runSyncUnsafe()

        //then
        val r: Option[Array[Byte]] = downloader.headOptionL.runSyncUnsafe()
        contentBefore.isEmpty shouldBe true
        r.isEmpty shouldBe true
      }
    }

    "uploads to the blob from a file" in {
      //given
      val blobPath = nonEmptyString.sample.get
      val blobInfo: BlobInfo = BlobInfo.newBuilder(BlobId.of(testBucketName, blobPath)).build
      val blob: Blob = storage.create(blobInfo)
      val gcsBlob = new GcsBlob(blob)
      val sourcePath = new File(genLocalPath.sample.get).toPath
      val content: Array[Byte] = nonEmptyString.sample.get.getBytes()
      Files.write(sourcePath, content)

      //when
      val dowloader: Observable[Array[Byte]] = gcsBlob.download()
      val contentBefore: Option[Array[Byte]] = dowloader.headOptionL.runSyncUnsafe()
      val f = gcsBlob.uploadFromFile(sourcePath).runToFuture(global)

      //then
      val exists = gcsBlob.exists().runSyncUnsafe()
      val r = gcsBlob.download().headOptionL.runSyncUnsafe()
      f.value.get.isSuccess shouldBe true
      exists shouldBe true
      contentBefore.isDefined shouldBe false
      r.isDefined shouldBe true
      r.get shouldBe content
    }

    "return a failed task when uploading from a non existent file" in {
      //given
      val blobPath = nonEmptyString.sample.get
      val blobInfo: BlobInfo = BlobInfo.newBuilder(BlobId.of(testBucketName, blobPath)).build
      val blob: Blob = storage.create(blobInfo)
      val gcsBlob = new GcsBlob(blob)
      val sourcePath = new File(genLocalPath.sample.get).toPath

      //when
      val dowloader: Observable[Array[Byte]] = gcsBlob.download()
      val contentBefore: Option[Array[Byte]] = dowloader.headOptionL.runSyncUnsafe()
      val f = gcsBlob.uploadFromFile(sourcePath).runToFuture(global)

      //then
      f.value.get.isFailure shouldBe true
      val r = gcsBlob.download().headOptionL.runSyncUnsafe()
      contentBefore.isDefined shouldBe false
      r.isDefined shouldBe false
    }

  }

}
