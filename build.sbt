import scala.util.Try


name := "reprobate"

organization := "im.mange"

version := Try(sys.env("TRAVIS_BUILD_NUMBER")).map("0.0." + _).getOrElse("1.0-SNAPSHOT")

scalaVersion := "2.11.1"

//seq(webSettings :_*)

libraryDependencies ++= {
  val liftVersion = "2.6-RC1"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
    "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910", // % "container",
    "org.eclipse.jetty" % "jetty-server" % "8.1.7.v20120910", // % "container",
//    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016", // %
//      "container,compile" artifacts Artifact("javax.servlet", "jar", "jar"),
//    "org.scala-lang.modules" %% "scala-xml" % "1.0.1",
    "io.shaka" %% "naive-http" % "48"
  )
}

////crossScalaVersions := Seq("2.10.4"/*, "2.11.0"*/)
//

//resolvers ++= Seq(
//  "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"
//)
//
////see: https://github.com/karma4u101/lift-jquery-module
//
//libraryDependencies ++= Seq(
//	"junit" % "junit" % "4.11" % "test->default",
//	"org.scalatest" %% "scalatest" % "2.2.0" % "test",
////  "net.liftweb" %% "lift-webkit" % "2.6" % "compile",
////  "net.liftmodules"   %% "lift-jquery-module" % ("2.6" + "-2.2"),
////  "org.eclipse.jetty" % "jetty-webapp"        % "8.1.7.v20120910"  % "container,test",
////  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
////  "ch.qos.logback" % "logback-classic" % "1.0.6",
////  9.2.3.v20140905
//"net.liftweb" %% "lift-webkit" % "2.6" % "compile",
//  "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910"  %
//    "container,test",
//  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" %
//    "container,compile" artifacts Artifact("javax.servlet", "jar", "jar"),
//  "io.shaka" %% "naive-http" % "48"
//)
//
//
//libraryDependencies := {
//  CrossVersion.partialVersion(scalaVersion.value) match {
//    case Some((2, scalaMajor)) if scalaMajor >= 11 => libraryDependencies.value :+ "org.scala-lang.modules" %% "scala-xml" % "1.0.1"
//    case _ => libraryDependencies.value
//  }
//}
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
