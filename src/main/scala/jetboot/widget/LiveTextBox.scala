package jetboot.widget

import jetboot._
import net.liftweb.http.SHtml
import jetboot.jscmd.JsCmdFactory._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.util.Helpers._
import net.liftweb.http.js.JE.JsRaw

trait LiveTextBox extends Input with Styleable with Hideable with Disableable {
  val placeholder: String
  val default: String

  def render = SHtml.text(default, (query) ⇒ nothing, "id" → id, "style" → styles.render, "class" → classes.render,
    "placeholder" → placeholder) % ("onkeyup" → SHtml.ajaxCall(JsRaw("this.value"), (query) ⇒ onKeyUp(query) )._2)

  def onKeyUp(value: String): Unit
  def updateValue(value: String) = element.setValue(value)
}