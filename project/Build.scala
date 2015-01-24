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
        val dist = "dist"
        val distdir: File = base / "dist"
        IO.delete(distdir)
        IO.createDirectory(distdir)

//        val libdir = distdir / "lib"

        val jars = libs.map(_.data).pair(flatRebase("dist/lib"))

        val script = file("reprobate.sh").pair(flatRebase("dist"))
        //TODO: put version in jar name ...
        val files = Seq(artifact -> "reprobate.jar")
//        val files = file("reprobate.jar") x flat

        println(s"artifact: $artifact")
        println(s"script: $script")
        println(s"files: $files")
//        println(s"distdir: $distdir")

//        IO.move(files ++ jars, distdir)

        IO.zip(files ++ jars ++ script, targetDir / "dist.zip")
//        IO.zip(distdir., targetDir / "dist.zip")

        //TODO: apparently gzip will maintain permissions ...
        //TODO: exclude resolution-cache∂
        //TODO: exclude streams
        //TODO: exclude scala 2.11
        //TODO: exclude compiler jars etc
    }
  )
}