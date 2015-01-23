package jetboot.widget

import jetboot.Select
import net.liftweb.http.SHtml
import net.liftweb.common.Full

trait ServerSideSelect extends Select {
  def render = SHtml.ajaxSelect(options.toSeq, Full(default), (newValue) ⇒ onChange(newValue),
                                "id" → id, "style" → styles.render, "class" → classes.render)
}