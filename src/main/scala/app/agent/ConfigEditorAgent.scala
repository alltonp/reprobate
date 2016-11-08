package app.agent

import app.server.{CancelProbeConfig, SaveProbeConfig}
import im.mange.belch.{Belch, PortMessage, ToLiftPort}
import im.mange.jetpac.Renderable
import im.mange.jetpac.comet.Subscriber
import im.mange.little.json.{LittleJodaSerialisers, LittleSerialisers}
import org.json4s.NoTypeHints
import org.json4s.native.Serialization

case class ConfigEditorAgent(subscriber: Subscriber) extends Renderable {
  case class AgentModel(config: String)

  private val belch = Belch("configEditorAgent", "ConfigEditorAgent", Some(ToLiftPort(receiveFromElm)),
    messageDebug = true, bridgeDebug = false)

  def render = belch.render

  //aka LoadConfig
  def onLoad(config: String) = belch.sendToElm(PortMessage("LoadAgentModel", toJson(AgentModel(config))))

  private def receiveFromElm(message: PortMessage) {
    message match {
      //aka Cancel
      case PortMessage("CancelCommand", _) => subscriber ! CancelProbeConfig()
      //aka Save
      case PortMessage("RunCommand", command) => subscriber ! SaveProbeConfig(command)
      case x => throw new RuntimeException(s"Don't know how to handle: $x")
    }
  }

  private val defaults = Serialization.formats(NoTypeHints) ++ LittleSerialisers.all ++ LittleJodaSerialisers.all

  private def toJson(agentModel: AgentModel) = {
    val formats = defaults
    Serialization.write(agentModel)(formats)
  }
}
