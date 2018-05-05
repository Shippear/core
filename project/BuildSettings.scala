import sbt.Keys._
import sbt._

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
    resolvers             ++= Dependencies.resolvers,
    javaOptions in Test += "-Dlogger.file=test/resources/logback-test.xml",
    testOptions in Test += Tests.Setup(() => {  System.setProperty("config.file", "test/resources/conf/application.conf") }),
    testOptions in Test += Tests.Setup(() => {
      print(
        """
* Initializing Embedded MongoDB
* Play Test Server
          __                 __
  _______/  |______ ________/  |_
 /  ___/\   __\__  \\_  __ \   __\
 \___ \  |  |  / __ \|  | \/|  |
/____  > |__| (____  /__|   |__|
     \/            \/

""")
    }),
    testOptions in Test += Tests.Cleanup(() => {
      print(
        """
* Play Test Server
* Stopping MongoDB

          __
  _______/  |_  ____ ______
 /  ___/\   __\/  _ \\____ \
 \___ \  |  | (  <_> )  |_> >
/____  > |__|  \____/|   __/
     \/              |__|

""")
    })
  )
}