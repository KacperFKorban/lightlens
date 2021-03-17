
class MySuite extends munit.FunSuite with Fixture {
  import lightlens.*

  test("Modify once nested field") {
    val obtained = eminem.focus(_.age).modify(_ + 1)
    val expected = eminem.copy(
      age = eminem.age + 1
    )
    assertEquals(obtained, expected)
  }

  test("Set once nested field") {
    val obtained = eminem.focus(_.age).set(20)
    val expected = eminem.copy(
      age = 20
    )
    assertEquals(obtained, expected)
  }

  test("Modify twice nested field") {
    val obtained = eminem.focus(_.names.firstName).modify(_.toLowerCase)
    val expected = eminem.copy(
      names = eminem.names.copy(
        firstName = eminem.names.firstName.toLowerCase
      )
    )
    assertEquals(obtained, expected)
  }

  test("Set twice nested field") {
    val obtained = eminem.focus(_.names.firstName).set("Bruce")
    val expected = eminem.copy(
      names = eminem.names.copy(
        firstName = "Bruce"
      )
    )
    assertEquals(obtained, expected)
  }

  test("Modify deeply nested field") {
    val obtained = forgotAboutDre.focus(_.author.address.country.name).modify(_.toLowerCase)
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
    val obtained = forgotAboutDre.focus(_.author.address.country.name).set(usa)
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
    val code = "case class Money(value: Long, currencyCode: String)\nMoney(1, \"PLN\").focus(_ => 2).set(100)"
    assert(
      compileErrors(code).contains("error: focus must have shape: _.field1.field2.field3")
    )
  }

  test("Give compilation error for non accessor method focus") {
    val code = "case class Money(value: Long, currencyCode: String)\nMoney(1, \"PLN\").focus(_.asInstanceOf[Long]).set(100)"
    assert(
      compileErrors(code).contains("error: focus must have shape: _.field1.field2.field3")
    )
  }

  test("Give compilation error for non accessor method focus mixed with field accessor") {
    val code = "case class Money(value: Long, currencyCode: String)\nMoney(1, \"PLN\").focus(_.value.toLong).set(100)"
    assert(
      compileErrors(code).contains("error: focus must have shape: _.field1.field2.field3")
    )
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