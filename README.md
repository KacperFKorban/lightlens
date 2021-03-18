# lightlens

(Experimantal) implementation of lightweight lenses for Scala 3.

The goal of this project is to provide easy to use lenses functionality by adding one import statement.

# Example:
```scala
import lightlens.*

case class Name(name: String)
case class AdditionalInfo(friends: List[Name])
case class Person(firstName: Name, age: Int, id: String, siblingsNo: Int, additionalInfo: AdditionalInfo)

val bob = Person(Name("Bob"), 25, "12345", 3, AdditionalInfo(List(Name("Mark"), Name("Anna"))))

bob.focus(_.age).set(60)
// Person(Name(Bob),60,12345,3,AdditionalInfo(List(Name(Mark), Name(Anna))))
//                  ^^

bob.focus(_.additionalInfo.friends.mapped.name).modify(_.toLowerCase)
// Person(Name(Bob),25,12345,3,AdditionalInfo(List(Name(mark), Name(anna))))
//                                                      ^           ^
```

# Inspiration
- https://hackage.haskell.org/package/lens
- https://github.com/softwaremill/quicklens
- https://github.com/optics-dev/Monocle