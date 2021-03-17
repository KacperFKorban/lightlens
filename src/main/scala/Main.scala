
import notsoquicklens.*

object Main extends App {

  case class Name(name: String)
  case class Person(firstName: Name, age: Int, id: String, siblingsNo: Int)

  val bob = Person(Name("Bob"), 25, "2137", 3)

  println(bob.focus(_.firstName.name).modify(_.toLowerCase))

}
