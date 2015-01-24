import sbt.Keys._
import sbt._

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
        //TODO: put version in dist name ...
        //TODO: apparently gzip will maintain permissions ...
        //TODO: exclude compiler jars etc
        val dist = "dist"
        val distdir: File = base / "dist"
        IO.delete(distdir)
        IO.createDirectory(distdir)
        val jars = libs.map(_.data).pair(flatRebase("dist/lib"))
        val script = file("reprobate.sh").pair(flatRebase("dist"))
        val files = Seq(artifact -> "dist/reprobate.jar")
        IO.zip(files ++ jars ++ script, targetDir / "dist.zip")
    }
  )
}