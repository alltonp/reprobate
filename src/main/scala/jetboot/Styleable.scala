package jetboot

trait Styleable {
  var classes: Classes = Classes()
  var styles: Styles = Styles()

  def classes(c: Classes): this.type = {classes = c; this}
  def classes(c: String*): this.type = classes(Classes(c:_*))
  def styles(s: Styles): this.type = {styles = s; this}
  def styles(s: Style*): this.type = styles(Styles(s:_*))
}
