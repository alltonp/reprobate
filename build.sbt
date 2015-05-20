import scala.util.Try

name := "reprobate"

organization := "im.mange"

version := Try(sys.env("TRAVIS_BUILD_NUMBER")).map("0.0." + _).getOrElse("1.0-SNAPSHOT")

scalaVersion := "2.11.6"

libraryDependencies ++= {
  Seq(
    "io.shaka" %% "naive-http" % "48",
    "ch.qos.logback" % "logback-classic" % "1.0.6",
    "im.mange" %% "little" % "0.0.7",
    "im.mange" %% "little-server" % "0.0.8",
    "im.mange" %% "shoreditch-api" % "0.0.65",
    "im.mange" %% "jetboot" % "0.0.55",
    "net.liftweb" %% "lift-webkit" % "2.6.1"
      exclude("javax.mail", "mail")
      exclude("net.liftweb", "lift-markdown_2.11"),
    //TODO: re-enable this soon
    //exclude("org.scala-lang", "scala-compiler")
//    ,
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  )
}