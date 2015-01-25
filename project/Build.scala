import sbt.Keys._
import sbt._

object Reprobate extends Build {
  import BuildSettings._

  lazy val root = Project(id = "reprobate", base = file("."), settings = standardBuildSettings/* ++
    addArtifact(artifact in (Compile, dist), dist).settings*/)
}

object BuildSettings {
  val dist = taskKey[Unit]("dist")

  //http://www.scala-sbt.org/0.13/docs/Combined+Pages.html
//  // create an Artifact for publishing the .war file
//  artifact in (Compile, packageBin) := {
//    val previous: Artifact = (artifact in (Compile, dist)).value
//    println(s"####### artifact: " + previous)
//    previous.copy(`type` = "zip", extension = "zip")
//  }

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
        val dist = "dist"
        val distdir: File = base / "dist"
        IO.delete(distdir)
        IO.createDirectory(distdir)
        val jars = libs.map(_.data).pair(flatRebase("dist/lib"))
        val script = file("reprobate.sh").pair(flatRebase("dist"))
        val files = Seq(artifact -> "dist/reprobate.jar")
//        println(s"####### dist: " + outputZip.getName)
        println(s"### Building dist.zip for $version ...")
        IO.zip(files ++ jars ++ script, targetDir / "dist.zip")
//        outputZip

    }
  )
}