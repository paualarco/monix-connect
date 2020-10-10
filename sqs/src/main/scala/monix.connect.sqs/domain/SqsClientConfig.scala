package monix.connect.sqs.domain

import software.amazon.awssdk.auth.credentials.{
  AnonymousCredentialsProvider,
  AwsCredentialsProvider,
  DefaultCredentialsProvider
}
import software.amazon.awssdk.http.async.SdkAsyncHttpClient
import software.amazon.awssdk.regions.Region





final case class SqsClientConfig(
  private val credentialsProviderConfig: CredentialsProviderConfig,
  endpoint: Option[String] = None,
  httpClient: Option[SdkAsyncHttpClient] = None,
  region: Region = Region.AWS_GLOBAL) {
  val awsCredentialsProvider: AwsCredentialsProvider = credentialsProviderConfig.create()
}
