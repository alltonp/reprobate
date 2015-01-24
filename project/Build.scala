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
        val script = file("webappDeploy.sh") x flat
        val files = Seq(
            artifact                  -> "webappDeploy.jar"
        )
      IO.zip(files ++ jars ++ script, targetDir / "dist.zip")
    }
  )
}