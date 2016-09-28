package app.agent.configeditor

trait ConfigEditableAgent {
  def onColumnsChanged: Unit
  def onColumnsSaved: Unit
}
