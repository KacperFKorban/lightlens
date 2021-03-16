# notsoquicklens

(Experimantal) implementation of lightweight lenses for Scala 3.

Example:
```scala
import org.kacperfkorban.notsoquicklens.*

case class Name(name: String)
case class Person(firstName: Name, age: Int, id: String, siblingsNo: Int)

val bob = Person(Name("Bob"), 25, "2137", 3)

modify(bob)(_.firstName.name)(_ => "XD")
```