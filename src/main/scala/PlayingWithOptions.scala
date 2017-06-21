import cats.data.OptionT
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Right


class Repository(val db: KVStore) {
  def findById[T](id: String): Future[T] =
    OptionT(db.get[T](id)).getOrElse(throw new NotFoundException(s"Could not find value for key=$id"))
}

class KVStore {

  private val store = Map[String, Any](
    "u1" -> Cart("u1", List("i1", "i2", "i4")),
    "u2" -> Cart("u2", List("i2", "i3")),
    "i1" -> Item("i1", "Java Concurrency in Practice", 15.77),
    "i2" -> Item("i2", "The Art of Multiprocessor Programming", 28.99),
    "i3" -> Item("i3", "Meatball bubble gum", 2.99),
    "i4" -> Item("i4", "Garlic Toothpaste", 5.99)
  )

  def get[T](key: String) = Future(store("key").asInstanceOf[Option[T]])
}

class CartService(val myRepo: Repository) {

  def getCartForCheckout(userId: String): Future[ServiceResponse[CheckoutCart]] = {
    for {
      cart <- myRepo.findById[Cart](userId)
      items <- cart.itemIds.traverseU(itemId => toEither(myRepo.findById[Item](itemId)))
    } yield {
      val (found, missing) = items.partition(_.isRight)
      ServiceResponse(missing.map(e => itemNotFoundHandler(e.left.get)), Some(CheckoutCart(userId, found.map(_.right.get))))
    }
  }

  private def toEither[T](eventual: Future[T]) = eventual.map(Right(_)).recover({ case t => Left(t) })

  private def itemNotFoundHandler(ex: Throwable) = ex match {
    case ex: NotFoundException => Error("NOTFOUND", ex.getMessage)
    case ex: Throwable => ex.printStackTrace(); throw ex;
  }
}


case class Cart(userId: String, itemIds: List[String])

case class Item(itemId: String, name: String, price: BigDecimal)

case class CheckoutCart(userId: String, items: List[Item])

case class ServiceResponse[T](errors: List[Error], result: Option[T])

case class Error(code: String, message: String)

class NotFoundException(message: String) extends Exception(message)
