import BuildSettings._

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).
  enablePlugins(PlayJava).
  settings(watchSources ++= (baseDirectory.value / "public/ui" ** "*").get).
  settings(basicSettings).
  settings(
    libraryDependencies ++= Dependencies.appDependencies,
    libraryDependencies ++= Dependencies.testDependencies

  )