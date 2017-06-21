import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by vgalcenco on 6/3/17.
  */
object ScalaFutures {


  def main(args: Array[String]) {

    //    val xx = Future(10).
    //      flatMap(a => {
    //        println(Thread.currentThread().getName)
    //        Future(10)
    //          .map(b => {
    //            println(Thread.currentThread().getName)
    //            a + b
    //          })
    //      })
    //
    //    val sum = Await.result(xx, 2 seconds)
    //
    //    println(sum)

    val mm = Future(10)
      .map(x => {println(Thread.currentThread().getName); x * 10; })
      .map(x => {println(Thread.currentThread().getName); x * 10; })
      .map(x => {println(Thread.currentThread().getName); x * 10; })


    val prod = Await.result(mm, 2 seconds)

    println(prod)
  }
}
