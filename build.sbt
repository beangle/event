import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*
import sbt.Keys.libraryDependencies

ThisBuild / organization := "org.beangle.event"
ThisBuild / version := "0.0.3"
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

val beangle_common_ver = "5.6.14"
val beangle_data_ver = "5.8.7"
val beangle_commons_core = "org.beangle.commons" %% "beangle-commons-core" % beangle_common_ver
val beangle_commons_text = "org.beangle.commons" %% "beangle-commons-text" % beangle_common_ver
val beangle_data_jdbc = "org.beangle.data" %% "beangle-data-jdbc" % beangle_data_ver

val commonDeps = Seq(beangle_commons_core, logback_classic % "test", logback_core % "test", scalatest, gson)

lazy val root = (project in file("."))
  .settings()
  .aggregate(core, bus)

lazy val core = (project in file("core"))
  .settings(
    name := "beangle-event-core",
    common,
    libraryDependencies += (jedis % "optional"),
    libraryDependencies += beangle_data_jdbc % "test",
    libraryDependencies ++= Seq(postgresql % "optional", HikariCP % "optional"),
    libraryDependencies ++= commonDeps
  )

lazy val bus = (project in file("bus"))
  .settings(
    name := "beangle-event-bus",
    common,
    libraryDependencies += beangle_data_jdbc % "test",
    libraryDependencies ++= Seq(postgresql % "optional", HikariCP % "optional"),
    libraryDependencies ++= commonDeps
  ).dependsOn(core)

publish / skip := true
