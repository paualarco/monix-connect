package monix.connect.gcs

import java.io.FileInputStream
import java.nio.file.Path

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.WriteChannel
import com.google.cloud.storage.Storage._
import com.google.cloud.storage.{BlobInfo, Storage, StorageOptions}
import monix.connect.gcs.configuration.GcsBucketInfo
import monix.connect.gcs.components.Paging
import monix.connect.gcs.configuration.GcsBucketInfo.Metadata
import monix.eval.Task
import monix.reactive.Observable

final class GcsStorage(underlying: Storage) extends Paging {

  /**
    * Creates a new [[GcsBucket]] from the give config and options.
    */
  def createBucket(name: String,
                   location: GcsBucketInfo.Locations.Location,
                   metadata: Option[Metadata],
                   options: BucketTargetOption*): GcsBucket = {
    val bucket = underlying.create(GcsBucketInfo.withMetadata(name, location, metadata), options: _*)
    GcsBucket(bucket)
  }

  /**
    * Returns the specified bucket or None if it doesn't exist.
    */
  def getBucket(name: String, options: BucketGetOption*): Task[Option[GcsBucket]] = {
    Task(underlying.get(name, options: _*)).map { optBucket =>
      Option(optBucket).map(GcsBucket.apply)
    }
  }

  /**
    * Returns an [[Observable]] of all buckets attached to this storage instance.
    */
  def listBuckets(options: BucketListOption *): Observable[GcsBucket] =
    walk(Task(underlying.list(options: _*))).map(GcsBucket.apply)


  private[gcs] def writer(blobInfo: BlobInfo, options: Storage.BlobWriteOption*): WriteChannel = underlying.writer(blobInfo, options: _*)

  private[gcs] def underlying(): Storage = underlying

}

object GcsStorage {

  private[gcs] def apply(underlying: Storage): GcsStorage = {
    new GcsStorage(underlying)
  }

  def create(): GcsStorage = {
    new GcsStorage(StorageOptions.getDefaultInstance.getService)
  }

  def create(projectId: String, credentials: Path): GcsStorage = {
     new GcsStorage(StorageOptions
      .newBuilder()
      .setProjectId(projectId)
      .setCredentials(GoogleCredentials.fromStream(new FileInputStream(credentials.toFile)))
      .build()
      .getService
    )
  }
}