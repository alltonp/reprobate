package app.agent.columneditor

trait ColumnEditableAgent {
  def onColumnsChanged: Unit
  def onColumnsSaved: Unit
}
