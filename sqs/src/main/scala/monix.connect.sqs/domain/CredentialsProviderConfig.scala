package monix.connect.sqs.domain

final case class CredentialsProviderConfig(awsCredentials: AwsCredentials.Provider = AwsCredentials.Default) {
  def create(): AwsCredentialsProvider = {
    awsCredentials match {
      case AwsCredentials.Anonymous => new AnonymousCredentialsProvider()
      case AwsCredentials.Default => new DefaultCredentialsProvider()
      case AwsCredentials.Default => new DefaultCredentialsProvider()
    }
  }
}