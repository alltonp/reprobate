package jetboot.widget

import jetboot.Renderable
import jetboot.widget.SimpleWidgets._
import jetboot.style.Bootstrap
import Bootstrap._

//TODO: do a contentClasses maybe
//TODO: when I grow up I want to be a jetboot.bootstrap3.panel
case class Portlet(label: Renderable, content: Renderable, labelClasses: List[String] = Nil) extends Renderable {
  def render =
      div(
        table(
          thead(
            tr(th(label)).classes(labelClasses.mkString(" "))
          )/*.classes(tableHeaderStyle)*/,
          tbody(
            tr(td(div(content).classes("contentClasses"))) //padding: 3px 0px 4px 0px;
          )/*.classes(tableBodyStyle)*/
        ).classes(tableCondensed, tableBordered, "yyy") // font-size: smaller; background-color: #ffffff;
      ).classes("zzz").render // width: 100%; overflow: hidden;
}
