package jetboot

import net.liftweb.http.js.JsCmd

trait Select extends Input with Hideable with Styleable {
  //TODO: this should be an Option me thinks and mapped to a Lift Box
  def default: String
  def options: List[(String, String)]
  def onChange(newValue: String): JsCmd
}
