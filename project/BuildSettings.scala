import sbt.Keys._
import sbt.Resolver

object BuildSettings {

  lazy val basicSettings = Seq(
    name                  := "shippear",
    startYear             := Some(2018),
    scalaVersion          := "2.12.3",
    scalacOptions         += "-feature",
    scalacOptions         += "-language:implicitConversions",
    scalacOptions         += "-language:reflectiveCalls",
    scalacOptions         += "-language:postfixOps",
    crossPaths            := false,
    resolvers             ++= Dependencies.resolvers
  )

}