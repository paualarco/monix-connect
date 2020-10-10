package monix.connect.sqs.domain

object AwsCredentials extends Enumeration {
  type Provider = Value
  val Anonymous, Chain, Default, Environment, Static = Value
}