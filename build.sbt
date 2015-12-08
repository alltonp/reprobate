import scala.util.Try

name := "reprobate"

organization := "im.mange"

version := Try(sys.env("TRAVIS_BUILD_NUMBER")).map("0.0." + _).getOrElse("1.0-SNAPSHOT")

scalaVersion := "2.11.7"

libraryDependencies ++= {
  Seq(
    "io.shaka" %% "naive-http" % "48",
    "ch.qos.logback" % "logback-classic" % "1.0.6",
    "im.mange" %% "little" % "0.0.35",
    "im.mange" %% "little-server" % "0.0.8",
    "im.mange" %% "shoreditch-api" % "0.0.65",
    "im.mange" %% "jetboot" % "0.0.121",
    "net.liftweb" %% "lift-webkit" % "2.6.2"
      exclude("javax.mail", "mail")
      exclude("net.liftweb", "lift-markdown_2.11"),
    "net.liftmodules"   %% "lift-jquery-module_2.6" % "2.8",
    //TODO: re-enable this soon
    //exclude("org.scala-lang", "scala-compiler")
    "org.json4s"        %% "json4s-native"          % "3.2.11"
      exclude("org.scala-lang", "scala-compiler")
      exclude("org.scala-lang", "scalap")
      exclude("joda-time", "joda-time")
    ,
    "org.json4s"        %% "json4s-ext"             % "3.2.11"
      exclude("joda-time", "joda-time")
    ,
//    ,
    "org.scalatest" %% "scalatest" % "2.2.4" % "test",
     //
    "com.github.piltt" %% "silky-persistence" % "1.0.57"
  )
}