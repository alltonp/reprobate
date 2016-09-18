package app.agent.columneditor

import app.comet.{Init, RimToken}
import app.restlike.rim.Controller
import im.mange.little.json.{LittleJodaSerialisers, LittleSerialisers}
import org.json4s.NoTypeHints
import im.mange.belch.{Belch, PortMessage, ToLiftPort}
import im.mange.jetpac.Renderable
import im.mange.jetpac.comet.Subscriber
import org.json4s.native.Serialization

case class AgentModel(columns: Seq[Column])
case class Column(name: String, selected: Boolean, system: Boolean)
case class ColumnConfig(columns: Seq[Column])

//case class Init()

case class ColumnEditorAgent(initialColumnConfig: ColumnConfig, subscriber: Subscriber, parent: ColumnEditableAgent) extends Renderable {
  private var columnConfig = initialColumnConfig

  private val belch = Belch("columnEditorAgent", "ColumnEditorAgent", Some(ToLiftPort(receiveFromElm)),
    messageDebug = true, bridgeDebug = true)

  def render = belch.render

  def onInit = belch.sendToElm(PortMessage("LoadAgentModel", toJson(AgentModel(columnConfig.columns))))

  def currentColumnConfig = columnConfig

  //TODO: this needs to be wrapped in an error handler
  private def receiveFromElm(message: PortMessage) {
    message match {
      case PortMessage("ColumnsChanged", payload) =>
        parent.onColumnsChanged
        columnConfig = tagsFromJson(payload)
//        subscriber ! Init()

      case PortMessage("SaveColumns", _) =>
        parent.onColumnsSaved
//        subscriber ! Init()

      case PortMessage("RunCommand", command) =>
        Controller.execute("PA", RimToken.token, command)
        subscriber ! Init

      case x => throw new RuntimeException(s"Don't know how to handle: $x")
    }
  }

  val defaults = Serialization.formats(NoTypeHints) ++ LittleSerialisers.all ++ LittleJodaSerialisers.all

  private def toJson(agentModel: AgentModel) = {
    val formats = defaults
    Serialization.write(agentModel)(formats)
  }

  private def tagsFromJson(json: String) = {
    implicit val formats = defaults
    Serialization.read[ColumnConfig](json)
  }
}
