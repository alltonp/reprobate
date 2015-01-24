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
//        val distdir: File = target.value / "dist"
//        IO.delete(distdir)
//        IO.createDirectory(distdir)

//        val libdir = distdir / "lib"

        val jars = libs.map(_.data).pair(flatRebase("lib"))

//        IO.copy(jars, distdir)
//        jars.map(j => IO.copyFile(j._1, libdir))

        val script = file("reprobate.sh") x flat
        //TODO: put version in jar name ...
        val files = Seq(artifact -> "reprobate.jar")

        println(s"artifact: $artifact")
        println(s"jars: $jars")
        println(s"libs: $libs")
        println(s"script: $script")
        println(s"files: $files")
//        println(s"distdir: $distdir")

//        IO.copyFile(files, distdir)

        IO.zip(files ++ jars ++ script, targetDir / "dist.zip")
//        IO.zip(Seq[distdir, targetDir / "dist.zip")

//        def entries(f: File):List[File] = f :: (if (f.isDirectory) IO.listFiles(f).toList.flatMap(entries(_)) else Nil)
//        IO.zip(entries(distdir).map(d => (d, d.getAbsolutePath.substring(distdir.getParent.length))), targetDir / "dist.zip")

        //TODO: apparently gzip will maintain permissions ...
        //TODO: put libs in a lib dir
        //TODO: exclude resolution-cache∂
        //TODO: exclude streams
        //TODO: exclude scala 2.11
        //TODO: exclude compiler jars etc
    }
  )
}