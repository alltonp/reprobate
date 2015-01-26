import scala.util.Try

name := "reprobate"

organization := "im.mange"

version := Try(sys.env("TRAVIS_BUILD_NUMBER")).map("0.0." + _).getOrElse("1.0-SNAPSHOT")

scalaVersion := "2.11.4"

libraryDependencies ++= {
  val jettyVersion = "8.1.7.v20120910"
  Seq(
    "org.eclipse.jetty" % "jetty-webapp" % jettyVersion,
    "org.eclipse.jetty" % "jetty-server" % jettyVersion,
    "io.shaka" %% "naive-http" % "48",
    "ch.qos.logback" % "logback-classic" % "1.0.6",
    "im.mange" %% "jetboot" % "0.0.7",
    "im.mange" %% "shoreditch-api" % "0.0.60"
  )
}