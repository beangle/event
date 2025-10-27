import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*
import sbt.Keys.*

ThisBuild / organization := "org.beangle.event"
ThisBuild / version := "0.1.3-SNAPSHOT"
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/beangle/event"),
    "scm:git@github.com:beangle/event.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "chaostone",
    name = "Tihua Duan",
    email = "duantihua@gmail.com",
    url = url("http://github.com/duantihua")
  )
)

ThisBuild / description := "The Beangle Event Library"
ThisBuild / homepage := Some(url("https://beangle.github.io/event/index.html"))

val beangle_commons = "org.beangle.commons" % "beangle-commons" % "5.6.32"
val beangle_jdbc = "org.beangle.jdbc" % "beangle-jdbc" % "1.1.3"

lazy val root = (project in file("."))
  .settings(
    name := "beangle-event",
    common,
    libraryDependencies ++= Seq(beangle_commons),
    libraryDependencies ++= Seq(beangle_jdbc % "test", logback_classic % "test", scalatest),
    libraryDependencies += (jedis % "optional"),
    libraryDependencies ++= Seq(postgresql % "optional", HikariCP % "optional"),
  )
