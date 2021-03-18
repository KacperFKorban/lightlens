import lightlens.*

object Main extends App {

  case class Name(name: String)
  case class AdditionalInfo(friends: List[Name], siblingsNo: Int)
  case class Person(firstName: Name, age: Int, id: String, additionalInfo: AdditionalInfo)

  val bob = Person(Name("Bob"), 25, "12345", AdditionalInfo(List(Name("Mark"), Name("Anna")), 3))

  println(bob.focus(_.age).set(60))

  println(bob.focus(_.additionalInfo.friends.mapped.name).modify(_.toLowerCase))

}
