import scala.util.Try

name := "reprobate"

organization := "im.mange"

version := Try(sys.env("TRAVIS_BUILD_NUMBER")).map("0.0." + _).getOrElse("1.0-SNAPSHOT")

scalaVersion := "2.11.4"

libraryDependencies ++= {
  val liftVersion = "2.6"
  val jettyVersion = "8.1.7.v20120910"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
    "org.eclipse.jetty" % "jetty-webapp" % jettyVersion,
    "org.eclipse.jetty" % "jetty-server" % jettyVersion,
    "io.shaka" %% "naive-http" % "48",
    "ch.qos.logback" % "logback-classic" % "1.0.6",
    "im.mange" %% "jetboot" % "0.0.7",
    "im.mange" %% "shoreditch-api" % "0.0.60"
  )
}

//resolvers ++= Seq(
//  "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"
//)
//
//sonatypeSettings
//
//publishTo <<= version { project_version ⇒
//  val nexus = "https://oss.sonatype.org/"
//  if (project_version.trim.endsWith("SNAPSHOT"))
//    Some("snapshots" at nexus + "content/repositories/snapshots")
//  else
//    Some("releases" at nexus + "service/local/staging/deploy/maven2")
//}
//
//publishMavenStyle := true
//
//publishArtifact in Test := false
//
////publishArtifact in (Compile, packageBin) := false
//
////addArtifact(artifact in (Compile, BuildSettings.dist), BuildSettings.dist).settings
//
//homepage := Some(url("https://github.com/alltonp/reprobate"))
//
//licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))
//
//credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USER"), System.getenv("SONATYPE_PASSWORD"))
//
//pomExtra :=
//    <scm>
//      <url>git@github.com:alltonp/reprobate.git</url>
//      <connection>scm:git:git@github.com:alltonp/reprobate.git</connection>
//    </scm>
//    <developers>
//      <developer>
//        <id>alltonp</id>
//      </developer>
//    </developers>
//
