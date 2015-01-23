package jetboot

//TODO: this should be Set backed really
case class Classes(private val classNames: String*) {
  val render = classNames.mkString(" ")

  def add(toAdd: String*) = Classes(toAdd.toList ::: classNames.toList)
}

object Classes {
  def apply(classNames: List[String]): Classes = Classes(classNames:_*)
}
