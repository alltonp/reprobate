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
    if (!file.toFile.exists()) {
      save(createEmpty)
      println(add("demo@rim.com", "rim", "backlog", List("next", "doing", "done"), "released"))
      println(add("demo@rim.com", "timesheets", "unprocessed", List("submitted", "invoiced", "paid"), "archived"))
    }
    Json.deserialise(Filepath.load(file))
  }

  //TODO: ultimately do this in the ui/endpoint
  def add(email: String, name: String, preWorkflowState: String, workflowStates: List[String], postWorkflowState: String) = {

    //TODO: blow up if email and name combo already exists ... return Either error or token

    val model = Model(
      Config(name, preWorkflowState, workflowStates, postWorkflowState, Nil), Map.empty, Nil, Nil
    )

    val token = java.util.UUID.randomUUID.toString

    val universe = load

    save(
      universe.copy(
        tokenToModel = universe.tokenToModel.updated(token, model),
        tokenToAccess = universe.tokenToAccess.updated(token, Access(Seq(email)))
      )
    )

    token
  }

  def save(state: Universe) {
    Filepath.save(pretty(render(Json.serialise(state))), file)
  }

  private def createEmpty() = Universe(Map.empty, Map.empty)
}
