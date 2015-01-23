package jetboot

trait HtmlElement {
  var title: String = ""

  def title(t: String): this.type = {title = t; this}
}
