
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
}

trait Fixture {
  enum Country:
    case Poland
    case Sweden
    case England
    case USA
  case class Names(firstName: String, lastName: String)
  case class Address(country: Country, city: String)
  case class Person(names: Names, age: Int, address: Address)
  case class Song(title: String, author: Person, features: List[Person])

  val eminem = Person(
    names = Names(
      firstName = "Marshall",
      lastName = "Mathers"
    ),
    age = 48,
    address = Address(
      country = Country.USA,
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
      country = Country.USA,
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