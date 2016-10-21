package app.agent.configeditor

import app.comet.{Init, RimToken}
import app.model.ProbeRegistry
import app.restlike.rim.Controller
import app.server.{CancelProbeConfig, SaveProbeConfig}
import im.mange.belch.{Belch, PortMessage, ToLiftPort}
import im.mange.jetpac.Renderable
import im.mange.jetpac.comet.Subscriber
import im.mange.little.json.{LittleJodaSerialisers, LittleSerialisers}
import org.json4s.NoTypeHints
import org.json4s.native.Serialization

case class AgentModel(columns: Seq[Column], config: String)
case class Column(name: String, selected: Boolean, system: Boolean)
case class ColumnConfig(columns: Seq[Column])

case class ConfigEditorAgent(initialColumnConfig: ColumnConfig, subscriber: Subscriber, parent: ConfigEditableAgent) extends Renderable {
  private var columnConfig = initialColumnConfig

  private val belch = Belch("configEditorAgent", "ConfigEditorAgent", Some(ToLiftPort(receiveFromElm)),
    messageDebug = true, bridgeDebug = false)

  def render = belch.render

  def onLoad(config: String) = belch.sendToElm(PortMessage("LoadAgentModel", toJson(AgentModel(columnConfig.columns, config))))

  def currentColumnConfig = columnConfig

  //TODO: this needs to be wrapped in an error handler
  private def receiveFromElm(message: PortMessage) {
    message match {
      case PortMessage("ColumnsChanged", payload) =>
        parent.onColumnsChanged
        columnConfig = tagsFromJson(payload)

      //Cancel
      case PortMessage("CancelCommand", _) =>
        subscriber ! CancelProbeConfig()

      //Save
      case PortMessage("RunCommand", command) =>
        subscriber ! SaveProbeConfig(command)

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
