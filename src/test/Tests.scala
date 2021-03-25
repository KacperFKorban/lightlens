
class MySuite extends munit.FunSuite with Fixture {
  import lightlens.*

  test("Modify once nested field") {
    val obtained = eminem.modify(_.age).using(_ + 1)
    val expected = eminem.copy(
      age = eminem.age + 1
    )
    assertEquals(obtained, expected)
  }

  test("Set once nested field") {
    val obtained = eminem.modify(_.age).setTo(20)
    val expected = eminem.copy(
      age = 20
    )
    assertEquals(obtained, expected)
  }

  test("Modify twice nested field") {
    val obtained = eminem.modify(_.names.firstName).using(_.toLowerCase)
    val expected = eminem.copy(
      names = eminem.names.copy(
        firstName = eminem.names.firstName.toLowerCase
      )
    )
    assertEquals(obtained, expected)
  }

  test("Set twice nested field") {
    val obtained = eminem.modify(_.names.firstName).setTo("Bruce")
    val expected = eminem.copy(
      names = eminem.names.copy(
        firstName = "Bruce"
      )
    )
    assertEquals(obtained, expected)
  }

  test("Modify deeply nested field") {
    val obtained = forgotAboutDre.modify(_.author.address.country.name).using(_.toLowerCase)
    val expected = forgotAboutDre.copy(
      author = forgotAboutDre.author.copy(
        address = forgotAboutDre.author.address.copy(
          country = forgotAboutDre.author.address.country.copy(
            name = forgotAboutDre.author.address.country.name.toLowerCase
          )
        )
      )
    )
    assertEquals(obtained, expected)
  }

  test("Set deeply nested field") {
    val usa = "United States of America"
    val obtained = forgotAboutDre.modify(_.author.address.country.name).setTo(usa)
    val expected = forgotAboutDre.copy(
      author = forgotAboutDre.author.copy(
        address = forgotAboutDre.author.address.copy(
          country = forgotAboutDre.author.address.country.copy(
            name = usa
          )
        )
      )
    )
    assertEquals(obtained, expected)
  }

  test("Give compilation error for function focus") {
    val code = "case class Money(value: Long, currencyCode: String)\nMoney(1, \"PLN\").modify(_ => 2).setTo(100)"
    assert(
      compileErrors(code).contains("error: focus must have shape: _.field1.field2.field3")
    )
  }

  test("Give compilation error for non accessor method focus") {
    val code = "case class Money(value: Long, currencyCode: String)\nMoney(1, \"PLN\").modify(_.asInstanceOf[Long]).setTo(100)"
    assert(
      compileErrors(code).contains("error: focus must have shape: _.field1.field2.field3")
    )
  }

  test("Give compilation error for non accessor method focus mixed with field accessor") {
    val code = "case class Money(value: Long, currencyCode: String)\nMoney(1, \"PLN\").modify(_.value.toLong).setTo(100)"
    assert(
      compileErrors(code).contains("error: focus must have shape: _.field1.field2.field3")
    )
  }

  test("Be able to use each") {
    val usa = "United States of America"
    val obtained = forgotAboutDre.modify(_.features.each.address.country.name).setTo(usa)
    val expected = forgotAboutDre.copy(
      features = forgotAboutDre.features.map { x =>
        x.copy(
          address = x.address.copy(
            country = x.address.country.copy(
              name = usa
            )
          )
        )
      }
    )
    assertEquals(obtained, expected)
  }

  test("Be able to use top level each") {
    val obtained = List(eminem, drDre).modify(_.each.age).using(_ - 20)
    val expected = List(eminem, drDre).map { p =>
      p.copy(
        age = p.age - 20
      )
    }
    assertEquals(obtained, expected)
  }

  test("Use a complicated function in modify") {
    val obtained = List(eminem, drDre).modify(_.each.age).using { age =>
      def primesBefore(n: Int) = {
        def isPrime(n: Int) = 2.until(n).forall(n % _ != 0)
        1.to(n).filter(isPrime)
      }
      primesBefore(age).length
    }
    val expected = List(eminem, drDre).map { p =>
      p.copy(
        age = {
          def primesBefore(n: Int) = {
            def isPrime(n: Int) = 2.until(n).forall(n % _ != 0)
            1.to(n).filter(isPrime)
          }
          primesBefore(p.age).length
        }
      )
    }
    assertEquals(obtained, expected)
  }
}

trait Fixture {

  enum Continent:
    case Europe
    case Australia
    case NorthAmerica
    case SouthAmerica
    case Africa
    case Asia
    case Antarctica

  case class Country(name: String, continent: Continent)

  case class Address(country: Country, city: String)

  case class Names(firstName: String, lastName: String)

  case class Person(names: Names, age: Int, address: Address)

  case class Song(title: String, author: Person, features: List[Person])

  val eminem = Person(
    names = Names(
      firstName = "Marshall",
      lastName = "Mathers"
    ),
    age = 48,
    address = Address(
      country = Country(
        name = "USA",
        continent = Continent.NorthAmerica
      ),
      city = "Detroit"
    )
  )

  val drDre = Person(
    names = Names(
      firstName = "Andre",
      lastName = "Young"
    ),
    age = 56,
    address = Address(
      country = Country(
        name = "USA",
        continent = Continent.NorthAmerica
      ),
      city = "Compton"
    )
  )

  val forgotAboutDre = Song(
    title = "Forgot About Dre",
    author = eminem,
    features = List(
      drDre
    )
  )
}