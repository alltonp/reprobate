package app.model

case class Defcon(level: String, shake: String, backgroundColour: String)

object Defcons {
  val one = Defcon("1", "shake-crazy", "#cc0000")
  val two = Defcon("2", "shake-hard", "#cc0000")
  val three = Defcon("3", "shake-slow", "#cc0000")
  val four = Defcon("4", ""/*basic */, "#cc0000")
  val five = Defcon("5", "shake-little", "#cc0000")

  def apply(level: String) = {
    level match {
      case "1" => one
      case "2" => two
      case "3" => three
      case "4" => four
      case _ => five
    }
  }
}