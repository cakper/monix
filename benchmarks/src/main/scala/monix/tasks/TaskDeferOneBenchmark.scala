package monix.tasks

import java.util.concurrent.TimeUnit
import monix.execution.Scheduler.Implicits.global
import monix.tasks.{Task => MonixTask}
import org.openjdk.jmh.annotations._
import scalaz.concurrent.{Task => ScalazTask}
import scalaz.{-\/, \/-}

/*
 * Sample run:
 *
 *     sbt "benchmarks/jmh:run -i 10 -wi 10 -f 1 -t 1 monix.tasks.TaskDeferOneBenchmark"
 *
 * Which means "10 iterations" "5 warmup iterations" "1 fork" "1 thread".
 * Please note that benchmarks should be usually executed at least in
 * 10 iterations (as a rule of thumb), but more is better.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class TaskDeferOneBenchmark {
  @Benchmark
  def monix(): Long = {
    var result = 0
    MonixTask.defer(10).runAsync(new Callback[Int] {
      def onError(ex: Throwable): Unit = throw ex
      def onSuccess(value: Int): Unit =
        result = value
    })
    result
  }

  @Benchmark
  def scalaz(): Long = {
    var result = 0
    ScalazTask.delay(10).unsafePerformAsync {
      case -\/(ex) => throw ex
      case \/-(value) => result = value
    }
    result
  }
}