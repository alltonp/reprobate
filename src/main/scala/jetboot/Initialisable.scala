package jetboot

import net.liftweb.http.js.JsCmd

trait Initialisable {
  def init: JsCmd
}
