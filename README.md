# lightlens

(Experimantal) implementation of lightweight lenses for Scala 3.

The goal of this project is to provide easy to use lenses functionality by adding one import statement.

# Example:
```scala
import lightlens.*

case class Name(name: String)
case class Person(firstName: Name, age: Int, id: String, siblingsNo: Int)

val bob = Person(Name("Bob"), 25, "2137", 3)

bob.focus(_.firstName.name).modify(_.toLowerCase)
```

# Inspiration
- https://hackage.haskell.org/package/lens
- https://github.com/softwaremill/quicklens
- https://github.com/optics-dev/Monocle