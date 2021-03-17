ThisBuild / scalaVersion := "3.0.0-RC1"
ThisBuild / version := "0.1.0"

ThisBuild / developers := List(
  Developer(
    id    = "KacperFKorban",
    name  = "Kacper Korban",
    email = "kacper.f.korban@gmail.com",
    url = url("https://twitter.com/KacperKorban")
  )
)

ThisBuild / description := "(Experimental) implementation of lightweight lenses for Scala 3"
ThisBuild / licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/KacperFKorban/lightlens"))

lazy val core = project
  .in(file(".core"))
  .settings(
    name := "lightlens-core",
    Compile / scalaSource := baseDirectory.value / ".." / "src" / "core",
  )

lazy val examples = project
  .in(file(".examples"))
  .dependsOn(core)
  .settings(
    name := "lightlens-examples",
    Compile / scalaSource := baseDirectory.value / ".." / "src" / "examples",
  )

lazy val test = project
  .in(file(".test"))
  .dependsOn(core)
  .settings(
    name := "lightlens-test",
    Test / scalaSource := baseDirectory.value / ".." / "src" / "test",
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.22" % Test,
    testFrameworks += new TestFramework("munit.Framework")
  )