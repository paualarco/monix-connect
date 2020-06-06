---
id: gcs
title: Google Cloud Storage
---

## GCS - Google Cloud Storage

### Introduction

_Cloud Storage_ is a durable and highly available object storage
service. _Google Cloud Storage_ is almost infinitely scalable and
guarantees consistency: when a write succeeds, the latest copy of the
object will be returned to any GET, globally.

### Set up

Add the following dependency to get started:
```scala
libraryDependencies += "io.monix" %% "monix-gcs" % "0.1.0"
```

### Getting Started

The Monix GCS connector is built on top of the
[Google Cloud Storage Client for Java](https://github.com/googleapis/java-storage).
The connector uses the *Application Default Credentials* method for
authentication to GCS. This requires that you have the
`GOOGLE_APPLICATION_CREDENTIALS` environment variable that points to a
Service Account with the required permissions in order to use the
connector.

```scala
import monix.connect.gcs.GcsStorage

val storage = GcsStorage.create()
```

Alternatively you will be able to point to connector to the credentials
file on disk in the event you don't have the
`GOOGLE_APPLICATION_CREDENTIALS` environment variable set.

```scala
import java.nio.file.Paths

import monix.connect.gcs.Storage

val projectId = "monix-connect-gcs"
val credentials = Paths.get("/path/to/credentials.json")

val storage = GcsStorage.create(projectId, credentials)
```
Once you have a GcsStorage object created you can begin to work with
GCS. You can create a new GcsBucket by using the methods on the
GcsStorage object:

```scala
import java.io.File

import io.monix.connect.gcs._
import io.monix.connect.gcs.configuration._
import io.monix.connect.gcs.configuration.BucketInfo.Locations

val storage = GcsStorage.create()

val metadata = GcsBucketInfo.Metadata(
  labels = Map(
    "project" -> "my-first-gcs-bucket"
  ),
  storageClass = Some(StorageClass.REGIONAL)
)
val bucket: Task[GcsBucket] = storage.createBucket("mybucket", Locations.`EUROPE-WEST1`, Some(metadata)).memoizeOnSuccess
```
Once you have a bucket instance you will be able to upload and download
`Blobs` from it using the methods on the Bucket. You can upload a file
directly from the filesystem or use a Consumer to upload any stream of
bytes.

```scala
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

import monix.reactive.Observable

// Upload from a File
val myFile = Paths.get("/tmp/myfile0.txt")
val metadata = GcsBlobInfo.Metadata(
  contentType = Some("text/plain")
)
val blob0: Task[GcsBlob] = {
  for {
    bucket <- bucket
    blob   <- b.uploadFromFile("my-first-file", myFile, metadata)
  } yield blob
}

// Upload from a Streaming Source
val myDataSource = Observable.fromIterable("this is some data".getBytes(StandardCharsets.UTF_8))
val blob1: Task[Unit] = {
  for {
    bucket   <- bucket
    consumer <- b.upload("my-second-file", metadata)
    _        <- data.consumeWith(consumer)
  } yield ()
}
```
Downloading from a bucket is just as straight forward:
```scala
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

import io.monix.connect.gcs._

// Upload to a File
val myFile = Paths.get("/tmp/myfile0.txt")
val blob0: Task[GcsBlob] = {
  for {
    bucket <- bucket
    _      <- b.downloadToFile("my-first-file", myFile)
  } yield println("File downloaded Successfully")
}

def printBytes(bytes: Array[Byte]): Unit = {
  println(new String(bytes, StandardCharsets.UTF_8))
}

// Get an Observable of Bytes
val blob1: Task[Unit] = {
  for {
    bucket <- bucket
    bytes  <- b.download("my-first-file").map(printBytes).completedL
  } yield println("File downloaded Successfully")
}
```

