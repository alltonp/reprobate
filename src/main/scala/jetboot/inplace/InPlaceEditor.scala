package jetboot.inplace

import jetboot.{Identifiable, Renderable, Initialisable}
import net.liftweb.http.js.JsExp

trait InPlaceEditor extends Renderable with Initialisable with Identifiable {
  def currentValue: JsExp
  def editorClass = id + "_editor"
}