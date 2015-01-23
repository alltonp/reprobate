package jetboot.inplace

import net.liftweb.http.js.JE.{JsObj, JsArray}

case class Source(values: List[String]) {
  def toJsArray = JsArray(values.map(v ⇒ JsObj(("value", v), ("text", v))))
}