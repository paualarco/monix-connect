package monix.connect.gcs

import java.io.FileInputStream
import java.nio.file.Path

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.WriteChannel
import com.google.cloud.storage.Storage._
import com.google.cloud.storage.{Blob, BlobId, BlobInfo, Storage, StorageOptions}
import monix.connect.gcs.configuration.GcsBucketInfo
import monix.connect.gcs.components.Paging
import monix.connect.gcs.configuration.GcsBucketInfo.Metadata
import monix.eval.Task
import monix.reactive.Observable

/**
  * This class wraps the [[com.google.cloud.storage.Storage]] class,
  * providing an idiomatic scala API that only exposes side-effectful calls
  * and automatically returns the right types from the monix connect api.
  * @param underlying the google's [[Storage]] instance that this class is based on.
  */
final class GcsStorage(private[gcs] val underlying: Storage) extends Paging {
  self =>

  /** Creates a new [[GcsBucket]] from the give config and options. */
  def createBucket(name: String,
                   location: GcsBucketInfo.Locations.Location,
                   metadata: Option[Metadata],
                   options: BucketTargetOption*)
  : Task[GcsBucket] = {
    Task(underlying.create(GcsBucketInfo.withMetadata(name, location, metadata), options: _*))
      .map(GcsBucket.apply)
  }

  /** Returns the specified bucket as [[GcsStorage]] or [[None]] if it doesn't exist. */
  def getBucket(bucketName: String, options: BucketGetOption*): Task[Option[GcsBucket]] = {
    Task(underlying.get(bucketName, options: _*)).map { bucket =>
      Option(bucket).map(GcsBucket.apply)
    }
  }

  /** Returns the specified blob as [[GcsBlob]] or [[None]] if it doesn't exist. */
  def getBlob(blobId: BlobId): Task[Option[GcsBlob]] =
    Task(underlying.get(blobId)).map { blob =>
      Option(blob).map(GcsBlob.apply)
    }

  /** Returns the specified blob as [[GcsBlob]] or [[None]] if it doesn't exist. */
  def getBlob(bucketName: String, blobName: String): Task[Option[GcsBlob]] =
    self.getBlob(BlobId.of(bucketName, blobName))

  /** Returns the specified list of as [[GcsBlob]] or [[None]] if it doesn't exist. */
  def getBlobs(blobIds: List[BlobId]): Task[List[GcsBlob]] =
    Task(blobIds.map(self.getBlob(_))).flatMap { blobIds =>
      Task.sequence(blobIds).map(_.filter(_.isDefined).map(_.get))
    }

  /** Returns an [[Observable]] of all buckets attached to this storage instance. */
  def listBuckets(options: BucketListOption *): Observable[GcsBucket] =
    walk(Task(underlying.list(options: _*))).map(GcsBucket.apply)

  /** Internal API method to return [[WriteChannel]] from the underlying [[Storage]] */
  private[gcs] def writer(blobInfo: BlobInfo, options: Storage.BlobWriteOption*): WriteChannel = underlying.writer(blobInfo, options: _*)

}

/** Companion object for [[GcsStorage]] that provides different builder options. */
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