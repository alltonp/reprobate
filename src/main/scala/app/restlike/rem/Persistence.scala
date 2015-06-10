package app.restlike.rem

import java.nio.file.Paths

import im.mange.little.file.Filepath
import net.liftweb.json._

import scala.collection.immutable

object Persistence {
  private val file = Paths.get(s"${Rem.appName}.json")

  def load: Universe = {
    if (!file.toFile.exists()) save(
      Universe(
        Map("---email---" -> Model(/*defaultStatuses,*/ immutable.Map[String, String](), List[Thing]()/*, List[Release]()*/)),
        Map("---token---" -> "---email---")
      )
    )
    Json.deserialise(Filepath.load(file))
  }

  def save(state: Universe) {
    Filepath.save(pretty(render(Json.serialise(state))), file)
  }
}
