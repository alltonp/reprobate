package app.restlike.rim

import java.nio.file.Paths

import im.mange.little.file.Filepath
//import net.liftweb.json._
import org.json4s.native.JsonMethods._

import scala.collection.immutable

//TODO: use app name
object Persistence {
  private val file = Paths.get(s"${Rim.appName}.json")

  def load: Universe = {
    if (!file.toFile.exists()) { save(createEmpty) }
    Json.deserialise(Filepath.load(file))
  }

  //TODO: ultimately do this in the ui and return the token
  def add(email: String, name: String,
          preWorkflowState: String = "backlog",
          workflowStates: List[String] = List("next", "doing", "done"),
          postWorkflowState: String = "released") {

    val model = Model(
      Config(name, preWorkflowState, workflowStates, postWorkflowState, List[String]()),
      immutable.Map[String, String](), List[Issue](), List[Release]()
    )

    val token = java.util.UUID.randomUUID.toString
    val universe = load

    save(
      universe.copy(
        tokenToModel = universe.tokenToModel.updated(token, model),
        tokenToAccess = universe.tokenToAccess.updated(token, Access(Seq(email)))
      )
    )
  }

  def save(state: Universe) {
    Filepath.save(pretty(render(Json.serialise(state))), file)
  }

  private def createEmpty() = Universe(Map.empty, Map.empty)
}
