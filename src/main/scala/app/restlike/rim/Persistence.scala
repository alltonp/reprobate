package app.restlike.rim

import java.nio.file.Paths

import im.mange.little.file.Filepath
//import net.liftweb.json._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.write
import org.json4s.native.{JsonParser, Serialization}

import scala.collection.immutable

//TODO: use app name
object Persistence {
  private val file = Paths.get(s"${Rim.appName}.json")
  private val defaultStatuses = List("next", "doing", "done")

  //TODO: could Model be 'T'ed up?
  def load: Model = {
    if (!file.toFile.exists()) save(Model(defaultStatuses, immutable.Map[String, String](), List[Issue](), List[Release](), List[String]()))
    Json.deserialise(Filepath.load(file))
  }

  def save(state: Model) {
    Filepath.save(pretty(render(Json.serialise(state))), file)
  }
}
