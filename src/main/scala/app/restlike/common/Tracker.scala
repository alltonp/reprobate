package app.restlike.common

import java.nio.file.Paths

import im.mange.little.file.Filepath
import org.joda.time.DateTime

//TODO: probably what to move this out into rim .. as the 'what' bit is not going to be generic
//actually, can have generic History, but extned/wrap for the rim specific bits
case class History(content: String) {
  private val contentBits = content.split("\\|")
  val what = contentBits.lift(3)
  private val whatBits = what.map(_.split(" "))

//  try {
//    val ref = whatBits.flatMap(_.lift(0))
    val refs = contentBits.lift(4).map(_.split(",").toList).getOrElse(Nil)

//    val action = whatBits.flatMap(_.lift(1))

    val when = contentBits.lift(1).map(ms => new DateTime(ms.toLong))
    val who = contentBits.lift(2)
    val token = contentBits.lift(0)

    println(s"$content -> ${contentBits.size} $refs")
    val maybePrintable: List[String] = contentBits.drop(1).toList
    val printable = (if (maybePrintable.size == 3) maybePrintable else maybePrintable ++ " ").mkString("|")

  //rim 1 is broken ... history wise

//    println(s"$content - $ref $email $who $action")

//  } catch {
//    case e: Exception => println(s"${e.getMessage} in $content")
//      val ref = ""
//      val email = ""
//  }
}

case class Tracker(filename: String) {
  private val file = Paths.get(filename)

  def track(who: String, what: String, token: String, changed: Seq[String]) {
    val content = List(token, DateTime.now.getMillis, who, what, changed.mkString(",")).mkString("|") + "\n"
    Filepath.append(content, file)
  }

  def view(token: String) = Filepath.load(file).split("\n").filterNot(_.isEmpty).reverse.map(History(_)).filter(_.token == Some(token)).toSeq
}
