import sbt._
import Keys._

object WebappDeploy extends Build {
//  import Dependency._
//  import Resolvers._
  import BuildSettings._
  import scala.collection._

  lazy val root = Project(id = "reprobate", base = file("."), settings = standardBuildSettings ++ Seq(
//    resolvers := Seq(jettyRepo, resolver, Classpaths.typesafeReleases),
//    libraryDependencies ++= jetty ++ scalaTest
  ))
}

object BuildSettings {

  val dist = taskKey[Unit]("dist")

  val standardBuildSettings: Seq[Def.Setting[_]] = Defaults.defaultSettings ++ Seq[Setting[_]](
//      organization := "im.mange",     //TODO these 3 lines are ignored here and picked from build.sbt instead
//      version := "1.0",
//      scalaVersion := "2.11.4",

    mappings in (Compile, packageBin) ++= {
      val webapp: File = baseDirectory.value / "src/main/webapp"
      for ((from, to) <- (webapp ***) x rebase(webapp, "webapp")) yield (from, to)
    },

    dist <<= (baseDirectory, target, packageBin in Compile, dependencyClasspath in Compile) map {
      (base, targetDir, artifact, libs) =>

        val jars = libs.map(_.data) x flat
        val script = file("webappDeploy.sh") x flat
        val files = Seq(
            artifact                  -> "webappDeploy.jar"
        )
      IO.zip(files ++ jars ++ script, targetDir / "dist.zip")
    }
  )
}

//object Resolvers {
//  val jettyRepo = "jetty repo" at "http://siasia.github.com/maven2"
//  val resolver = "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
//}

//object Dependency {
//  private val jettyVersion = "8.1.7.v20120910"
//  private val scalaTestVersion = "2.2.1"
//
//  val jetty = Seq (
//      "org.eclipse.jetty" % "jetty-server" % jettyVersion,
//      "org.eclipse.jetty" % "jetty-webapp" % jettyVersion
//    )
//
//  val scalaTest = Seq("org.scalatest" % "scalatest_2.11" % scalaTestVersion  % "test")
//}
