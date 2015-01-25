import sbt.Keys._
import sbt._

import scala.util.Try

object Reprobate extends Build {
  import BuildSettings._

  lazy val root = Project(id = "reprobate", base = file("."), settings = standardBuildSettings)
}

object BuildSettings {
  val dist = taskKey[Unit]("dist")

  val standardBuildSettings: Seq[Def.Setting[_]] = Defaults.defaultSettings ++ Seq[Setting[_]](
    mappings in (Compile, packageBin) ++= {
      val webapp: File = baseDirectory.value / "src/main/webapp"
      for ((from, to) <- (webapp ***) x rebase(webapp, "webapp")) yield (from, to)
    },

    //TODO: give up and just use - https://github.com/xerial/sbt-pack

    dist <<= (baseDirectory, target, packageBin in Compile, dependencyClasspath in Compile) map {
      (base, targetDir, artifact, libs) =>
        //TOOO: push jar into the lib dir
        //TOOO: give jar version
        //TODO: tidy up start script to remove jar name
        //TODO: put version in dist name ...
        //TODO: apparently gzip will maintain permissions ...
        //TODO: exclude compiler jars etc
        val ver = Try(sys.env("TRAVIS_BUILD_NUMBER")).map("0.0." + _).getOrElse("1.0-SNAPSHOT")
        val dist = s"reprobate-$ver"
        val distdir: File = base / dist
        IO.delete(distdir)
        IO.createDirectory(distdir)
        val jars = libs.map(_.data).pair(flatRebase(s"$dist/lib"))
        val script = file("reprobate.sh").pair(flatRebase(dist))
        val files = Seq(artifact -> s"$dist/lib/$dist.jar")
        println(s"### Building $dist.zip")
        IO.zip(files ++ jars ++ script, targetDir / "dist.zip")
    }
  )
}