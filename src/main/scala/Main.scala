
import org.virtuslab.notsoquicklens._

object Main extends App {

  case class Name(name: String)
  case class Person(firstName: Name, age: Int)

  val bob = Person(Name("Bob"), 25)

  val newBob = modify(bob)(_.firstName)(_ => Name("XD"))

  println(newBob)

}
