package monix.connect.sqs

import java.util.concurrent.CompletableFuture

import monix.eval.Task
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.{SqsRequest, SqsResponse}

private[sqs] object SqsOpFactory {
      def build[Req <: SqsRequest, Resp <: SqsResponse](
        operation: (SqsAsyncClient, Req) => CompletableFuture[Resp]): SqsOp[Req, Resp] = {
        new SqsOp[Req, Resp] {
          def execute(request: Req)(
            implicit
            client: SqsAsyncClient): Task[Resp] = {
            Task.from(operation(client, request))
          }
        }
      }
    }