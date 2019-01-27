package app.restlike.rem

import java.nio.file.Paths

import im.mange.little.file.Filepath
import net.liftweb.json._

object Persistence {
  private val file = Paths.get(s"data/${Rem.appName}.json")

  def load: Universe = {
    if (!file.toFile.exists()) save(
      Universe(
        Map("---email---" -> Model(/*immutable.Map[String, String](), */List[Thing]())),
        Map("---token---" -> "---email---")
      )
    )
    Json.deserialise(Filepath.load(file))
  }

  def save(state: Universe) {
    Filepath.save(pretty(render(Json.serialise(state))), file)
  }
}
