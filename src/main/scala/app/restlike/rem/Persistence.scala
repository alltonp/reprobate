package app.restlike.rem

import java.nio.file.Paths

import im.mange.little.file.Filepath
import net.liftweb.json._

import scala.collection.immutable

object Persistence {
  private val file = Paths.get(s"${Rem.appName}.json")
//  private val defaultStatuses = List("next", "doing", "done")

  def load: Model = {
    if (!file.toFile.exists()) save(Model(/*defaultStatuses,*/ immutable.Map[String, String](), List[Thing]()/*, List[Release]()*/))
    Json.deserialise(Filepath.load(file))
  }

  def save(state: Model) {
    Filepath.save(pretty(render(Json.serialise(state))), file)
  }
}
