import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*
import sbt.Keys.*

ThisBuild / organization := "org.beangle.event"
ThisBuild / version := "0.0.4"
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

val beangle_common_ver = "5.6.15"
val beangle_jdbc_ver = "1.0.0"
val beangle_commons = "org.beangle.commons" % "beangle-commons" % beangle_common_ver
val beangle_jdbc = "org.beangle.jdbc" % "beangle-jdbc" % beangle_jdbc_ver

val commonDeps = Seq(beangle_commons, logback_classic % "test", logback_core % "test", scalatest, gson)

lazy val root = (project in file("."))
  .settings(
    name := "beangle-event",
    common,
    libraryDependencies ++= commonDeps,
    libraryDependencies += (jedis % "optional"),
    libraryDependencies += beangle_jdbc % "test",
    libraryDependencies ++= Seq(postgresql % "optional", HikariCP % "optional"),
  )
