package app.restlike.rtm

import java.nio.file.Paths

import im.mange.little.file.Filepath
//import net.liftweb.json._
import org.json4s.native.JsonMethods._

import scala.collection.immutable

//TODO: use app name
object Persistence {
  private val file = Paths.get(s"data/${Rtm.appName}.json")
//  private val defaultStatuses = List("next", "doing", "done")

  //TODO: could Model be 'T'ed up?
  def load: Universe = {
    if (!file.toFile.exists()) save(
      Universe(
        Map("---email---" -> Model(immutable.Map[String, immutable.Map[String, String]](), immutable.Map[String, String]())),
        Map("---token---" -> "---email---")
      )
    )
    Json.deserialise(Filepath.load(file))
  }

  def save(state: Universe) {
    Filepath.save(pretty(render(Json.serialise(state))), file)
  }
}
