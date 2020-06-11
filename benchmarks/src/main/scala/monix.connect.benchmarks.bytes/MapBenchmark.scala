package monix.connect.benchmarks.bytes

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations.{
  Benchmark,
  BenchmarkMode,
  Fork,
  Level,
  Measurement,
  Mode,
  OutputTimeUnit,
  Param,
  Scope,
  Setup,
  State,
  Threads,
  Warmup
}
import zio.Chunk
import akka.util.ByteString
import monix.reactive.Observable
import monix.execution.Scheduler.Implicits.global

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
@Fork(1)
@Threads(1)
class MapBenchmark {

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

  //map
  @Benchmark
  def arrayMap: Array[Int] = array.map(b => b.toString).map(_.toInt)

  @Benchmark
  def chunkMap: Chunk[Int] = chunk.map(b => b.toString).map(_.toInt)

  @Benchmark
  def bSMap: IndexedSeq[Int] = byteString.map(_.toString).map(_.toInt)

  @Benchmark
  def obMap: List[Byte] = observable.flatMap(_ => observable ++ observable).toListL.runSyncUnsafe()

}
