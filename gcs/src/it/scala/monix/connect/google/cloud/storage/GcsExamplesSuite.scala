package monix.connect.google.cloud.storage

import java.io.File
import java.nio.file.Files

import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper
import com.google.cloud.storage.{Blob, Option => _}
import monix.connect.google.cloud.storage.configuration.GcsBucketInfo
import monix.eval.Task
import monix.reactive.Observable
import org.apache.commons.io.FileUtils
import org.mockito.{ArgumentMatchersSugar, IdiomaticMockito}
import org.scalacheck.Gen
import org.scalatest.{BeforeAndAfterAll, Ignore}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

@Ignore
class GcsExamplesSuite extends AnyWordSpecLike with IdiomaticMockito with Matchers with ArgumentMatchersSugar with BeforeAndAfterAll {

  val underlying = LocalStorageHelper.getOptions.getService
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

  s"Gcs consumer implementation" should {

    "bucket donwload" in {
      val storage = GcsStorage(underlying)
      val bucket: Task[Option[GcsBucket]] = storage.getBucket("myBucket")
      val ob: Observable[Array[Byte]] = {
        Observable.fromTask(bucket)
          .flatMap {
            case Some(blob) => blob.download("myBlob")
            case None => Observable.empty
          }
      }
    }

    "blob donwload" in {
      import monix.connect.google.cloud.storage.{GcsStorage, GcsBlob}
      import monix.eval.Task
      import monix.reactive.Observable

      val storage = GcsStorage.create()
      val getBlobT: Task[Option[GcsBlob]] = storage.getBlob("myBucket", "myBlob")

      val ob: Observable[Array[Byte]] = Observable.fromTask(getBlobT)
        .flatMap {
          case Some(blob) => blob.download()
          case None => Observable.empty
        }
    }

    "bucket donwload from file" in {
      import java.io.File

      import monix.execution.Scheduler.Implicits.global
      import monix.connect.google.cloud.storage.{GcsStorage, GcsBucket}
      import monix.eval.Task

      val storage = GcsStorage.create()
      val getBlobT: Task[Option[GcsBucket]] = storage.getBucket("myBucket")
      val file = new File("path/to/your/path.txt")
      val t: Task[Unit] = {
        for {
          maybeBlob <- getBlobT
          _ <- maybeBlob match {
            case Some(blob) => blob.downloadToFile("myBlob", file.toPath)
            case None => Task.unit
          }
        } yield ()
      }
    }

    "blob donwload from file" in {
      import monix.execution.Scheduler.Implicits.global
      import monix.connect.google.cloud.storage.{GcsStorage, GcsBlob}
      import monix.eval.Task

      val storage = GcsStorage.create()
      val getBlobT: Task[Option[GcsBlob]] = storage.getBlob("myBucket", "myBlob")
      val file = new File("path/to/your/path.txt")
      val t: Task[Unit] = {
        for {
          maybeBlob <- getBlobT
          _ <- maybeBlob match {
            case Some(blob) => blob.downloadToFile(file.toPath)
            case None => Task.unit
          }
        } yield ()
      }
    }


    "bucket upload" in {
      import java.io.File

      import monix.execution.Scheduler.Implicits.global
      import monix.connect.google.cloud.storage.{GcsStorage, GcsBucket}
      import monix.eval.Task

      val storage = GcsStorage.create()
      val createBucket: Task[GcsBucket] = storage.createBucket("myBucket", GcsBucketInfo.Locations.`US-WEST1`)
      val sourceFile = new File("path/to/your/path.txt")
      val ob: Observable[Array[Byte]] = ???
      val t: Task[Unit] = for {
        bucket <- createBucket
        unit <- bucket.uploadFromFile("myBlob", sourceFile.toPath)
      } yield ()
    }


  }

}
