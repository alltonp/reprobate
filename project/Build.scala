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

    dist <<= (baseDirectory, target, packageBin in Compile, dependencyClasspath in Compile) map {
      (base, targetDir, artifact, libs) =>
        val jars = libs.map(_.data) x flat
        val script = file("reprobate.sh") x flat
        //TODO: put version in jar name ...
        val files = Seq(artifact -> "reprobate.jar")
        IO.zip(files ++ jars ++ script, targetDir / "dist.zip")
        //TODO: put libs in a lib dir
        //TODO: exclude resolution-cache∂
        //TODO: exclude streams
        //TODO: exclude scala 2.11
        //TODO: exclude compiler jars etc
    }
  )
}