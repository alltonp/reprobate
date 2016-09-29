import java.io.File
import java.nio.charset.StandardCharsets._
import java.nio.file.{Files, Path}
import java.nio.file.Files._
import java.nio.file.StandardOpenOption._

import sbt.Keys._
import sbt.{File, _}

import scala.util.Try

object Reprobate extends Build {
  import Build._

  lazy val root = Project(id = "reprobate", base = file("."), settings = standardBuildSettings)
}

object Build {
  val dist = taskKey[Unit]("dist")

  val standardBuildSettings: Seq[Def.Setting[_]] = Defaults.defaultSettings ++ Seq[Setting[_]](
    mappings in (Compile, packageBin) ++= {
      val webapp: File = baseDirectory.value / "src/main/webapp"
      for ((from, to) <- (webapp ***) x rebase(webapp, "webapp")) yield (from, to)
    },

    dist <<= (baseDirectory, target, packageBin in Compile, dependencyClasspath in Compile) map {
      (base, targetDir, artifact, libs) =>
        //TODO: apparently gzip will maintain permissions ...
        //TODO: exclude compiler jars etc
        val ver = Try(sys.env("TRAVIS_BUILD_NUMBER")).map("0.0." + _).getOrElse("1.0-SNAPSHOT")
        val dist = s"reprobate-$ver"
        val distdir = targetDir / dist
        val jars = libs.map(_.data).pair(flatRebase(s"$dist/lib"))
        val script = file("package/reprobate.sh").pair(flatRebase(dist))
        IO.write(file("target/dist/version.txt"), ver)
        val version = file("target/dist/version.txt").pair(flatRebase(dist))
        val files = Seq(artifact -> s"$dist/lib/$dist.jar")
        println(s"### Building $dist.zip")
        IO.zip(files ++ jars ++ script ++ version, targetDir / "dist.zip")
    }
  )
}