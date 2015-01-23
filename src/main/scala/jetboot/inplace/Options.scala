package jetboot.inplace

import net.liftweb.http.js.JE.JsObj

case class Options(var inputclasses: List[String], emptytext: String = "--", source: Source = Source(Nil)) {
  def toJsCmd = JsObj(
    "onblur" → "submit",
    "mode" → "inline",
    "inputclass" → inputclasses.mkString(" "),
    "showbuttons" → false,
    "send" → "never",
    "emptytext" → emptytext,
    "source" → source.toJsArray
  ).toJsCmd
}