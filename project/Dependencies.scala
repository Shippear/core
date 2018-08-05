import play.sbt._
import sbt._


object Dependencies {

  val resolvers = Seq(
    Classpaths.typesafeReleases,
    "Maven Central" at "http://central.maven.org/maven2/",
    "Big Bee Consultants" at "http://dl.bintray.com/rick-beton/maven/",
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    "Sbt Plugins" at "http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases",
    "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype repo" at "https://oss.sonatype.org/content/groups/scala-tools/",
    "Sonatype OSS" at "https://oss.sonatype.org/content/groups/public",
    "jitpack" at "https://jitpack.io"
  )


  val externalDependencies = Seq(
    PlayImport.guice,
    PlayImport.ws,
    "com.h2database" % "h2" % "1.4.196",
    "com.iheart" %% "ficus" % "1.4.3",
    "net.codingwell" %% "scala-guice" % "4.1.1",
    "org.mongodb.scala" %% "mongo-scala-driver" % "2.2.1",
    "ai.snips" %% "play-mongo-bson" % "0.4",
    "com.github.nscala-time" %% "nscala-time" % "2.18.0",
    "com.github.salat" %% "salat" % "1.11.2",
    "com.github.kenglxn.qrgen" % "javase" % "2.4.0",
    "com.logentries" % "logentries-appender" % "1.1.38",
    "com.github.nscala-time" %% "nscala-time" % "2.20.0"

  )

  val jsonDependencies = Seq(
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.5",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % "2.9.5"
  )

  val appDependencies = externalDependencies ++ jsonDependencies

  val testDependencies = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2",
    "org.mockito" % "mockito-all" % "1.10.19",
    "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.1.1"
  ).map(_ % Test)

}
