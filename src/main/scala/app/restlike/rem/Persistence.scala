package app.restlike.rem

import java.nio.file.Paths

import im.mange.little.file.Filepath
import net.liftweb.json._

import scala.collection.immutable

object Persistence {
  private val file = Paths.get(s"${Rem.appName}.json")
//  private val defaultStatuses = List("next", "doing", "done")

  //TODO: use https://www.random.org/strings/?num=1&len=20&digits=on&upperalpha=on&loweralpha=on&unique=on&format=html&rnd=new
  def load: Universe = {
    if (!file.toFile.exists()) save(
      Universe(
        Map("paulallton@mac.com" -> Model(/*defaultStatuses,*/ immutable.Map[String, String](), List[Thing]()/*, List[Release]()*/)),
        Map("nTxCrC18GvvelOi62EST" -> "paulallton@mac.com")
      )
    )
    Json.deserialise(Filepath.load(file))
  }

  def save(state: Universe) {
    Filepath.save(pretty(render(Json.serialise(state))), file)
  }
}
