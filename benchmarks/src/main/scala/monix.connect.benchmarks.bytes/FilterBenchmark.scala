package monix.connect.benchmarks.bytes

import java.util.concurrent.TimeUnit

import akka.util.ByteString
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.openjdk.jmh.annotations._
import zio.Chunk

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(1)
@Threads(1)
class FilterBenchmark {

  @Param(Array("500"))
  var size: Int = _

  var array: Array[Byte] = _
  var chunk: Chunk[Byte] = _
  var byteString: ByteString = _
  var observable: Observable[Byte] = _

  @Setup(Level.Trial)
  def setup(): Unit = {
    array = (1 to size).flatMap(_.toString.getBytes).toArray
    chunk = Chunk.fromArray(array)
    byteString = ByteString.fromArray(array)
    observable = Observable.fromIterable(array)
  }

  //filter
  @Benchmark
  def arrayFilter: Array[Byte] = array.filter(a => a.isWhole)

  @Benchmark
  def chunkFilter: Chunk[Byte] = chunk.filter(a => a.isWhole)

  @Benchmark
  def bSFilter: ByteString = byteString.filter(a => a.isWhole)

  @Benchmark
  def obFilter: List[Byte] = observable.filter(a => a.isWhole).toListL.runSyncUnsafe()

}
