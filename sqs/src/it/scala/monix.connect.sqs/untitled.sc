

import monix.reactive.Observable
import monix.execution.Scheduler.Implicits.global
import monix.execution.Cancelable
import monix.reactive.observers.Subscriber
import monix.execution.Ack
import scala.concurrent.Future
import monix.eval.Task
import scala.concurrent.duration._
import java.time.LocalDateTime
class ObservableTest extends Observable[Int] {

  print("Hello")

   def unsafeSubscribeFn(subscriber: Subscriber[Int]): Cancelable = {
     var acc = 0
      def loop(f: Future[Ack]): Unit = {
        if(acc < 10) {
          f.onComplete{
            case _ =>  println("Sending acc with value: " + acc + " at:" + LocalDateTime.now().getSecond())
              acc = acc + 1
              val t = Task.from(subscriber.onNext(acc))

              loop(t.delayResult(1.seconds).runToFuture)
          }
          }
        else (subscriber.onComplete())
      }
     loop(subscriber.onNext(0))
     Cancelable.empty
  }

}


val a = new ObservableTest().toListL.runSyncUnsafe()