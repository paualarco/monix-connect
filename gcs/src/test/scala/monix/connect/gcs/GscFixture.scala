package monix.connect.gcs

import com.google.cloud.storage.{Acl, BlobId, BlobInfo, StorageClass}
import com.google.cloud.storage.Acl.{Entity, Group, Project, Role, User}
import monix.connect.gcs.configuration.GcsBlobInfo
import org.scalacheck.Gen

import scala.jdk.CollectionConverters._

trait GscFixture {

  val genAcl: Gen[Acl] = for {
    entity <- Gen.oneOf[Entity](User.ofAllUsers(), new Group("sample@email.com"), new Project(Project.ProjectRole.OWNERS, "id"))
    role <- Gen.oneOf(Role.OWNER, Role.READER, Role.WRITER)
  } yield {
    Acl.of(entity , role)
  }

  val genStorageClass: Gen[StorageClass] = Gen.oneOf(StorageClass.ARCHIVE, StorageClass.COLDLINE, StorageClass.DURABLE_REDUCED_AVAILABILITY, StorageClass.MULTI_REGIONAL, StorageClass.NEARLINE, StorageClass.REGIONAL, StorageClass.STANDARD)
  val genHex: Gen[String] = Gen.oneOf("1100", "12AC")

  val genGscBlobInfo: Gen[BlobInfo] = for {
    bucket <- Gen.alphaLowerStr
    name <- Gen.alphaLowerStr
    contentType <- Gen.option(Gen.alphaLowerStr)
    contentDisposition <- Gen.option(Gen.alphaLowerStr)
    contentLanguage <- Gen.option(Gen.alphaLowerStr)
    contentEncoding <- Gen.option(Gen.alphaLowerStr)
    cacheControl <- Gen.option(Gen.alphaLowerStr)
    crc32c <- Gen.option(Gen.alphaLowerStr.map(_.hashCode.toString))
    crc32cFromHexString <- Gen.option(genHex)
    md5 <- Gen.option(Gen.alphaLowerStr.map(_.hashCode.toString))
    md5FromHexString <- Gen.option(genHex)
    storageClass <- Gen.option(genStorageClass)
    temporaryHold <- Gen.option(Gen.oneOf(true, false))
    eventBasedHold <- Gen.option(Gen.oneOf(true, false))
    acl <- Gen.listOf(genAcl)
    metadata <- Gen.mapOfN(3, ("k", "v"))
  } yield {
    val builder = BlobInfo.newBuilder(BlobId.of(bucket, name))
    contentType.foreach(builder.setContentType)
      contentDisposition.foreach(builder.setContentDisposition)
      contentLanguage.foreach(builder.setContentLanguage)
      contentEncoding.foreach(builder.setContentEncoding)
      cacheControl.foreach(builder.setCacheControl)
      crc32c.foreach(builder.setCrc32c)
      crc32cFromHexString.foreach(builder.setCrc32cFromHexString)
      md5.foreach(builder.setMd5)
      md5FromHexString.foreach(builder.setMd5FromHexString)
      storageClass.foreach(builder.setStorageClass(_))
      temporaryHold.foreach(builder.setEventBasedHold(_))
      eventBasedHold.foreach(b => builder.setEventBasedHold(b))
      builder.setAcl(acl.asJava)
      builder.setMetadata(metadata.asJava)
      builder.build()
  }

  val genBlobInfoMetadata = for {
    contentType <- Gen.option(Gen.alphaLowerStr)
    contentDisposition <- Gen.option(Gen.alphaLowerStr)
    contentLanguage <- Gen.option(Gen.alphaLowerStr)
    contentEncoding <- Gen.option(Gen.alphaLowerStr)
    cacheControl <- Gen.option(Gen.alphaLowerStr)
    crc32c <- Gen.option(Gen.alphaLowerStr) //
    crc32cFromHexString <- Gen.option(genHex)
    md5 <- Gen.option(Gen.alphaLowerStr)
    md5FromHexString <- Gen.option(genHex)
    storageClass <- Gen.option(genStorageClass)
    temporaryHold <- Gen.option(Gen.oneOf(true, false))
    eventBasedHold <- Gen.option(Gen.oneOf(true, false))
   } yield {
    GcsBlobInfo.Metadata(
      contentType = contentType,
      contentDisposition = contentDisposition,
      contentLanguage = contentLanguage,
      contentEncoding = contentEncoding,
      cacheControl = cacheControl,
      crc32c = crc32c,
      crc32cFromHexString = crc32cFromHexString,
      md5 = md5,
      md5FromHexString = md5FromHexString,
      storageClass = storageClass,
      temporaryHold = temporaryHold,
      eventBasedHold = eventBasedHold,
    )
  }
}
