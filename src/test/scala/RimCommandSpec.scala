import app.restlike.rim._
import org.scalatest.{MustMatchers, WordSpec}

class RimCommandSpec extends WordSpec with MustMatchers {

//TODO: start with happy path
//  case In(Some(""), Nil) => onShowBoard(currentModel)
//  case In(Some("help"), Nil) => onHelp(who, currentModel)
//  case In(Some("?"), Nil) => onQueryIssues(currentModel, None)
//  case In(Some("?"), List(query)) => onQueryIssues(currentModel, Some(query))
//  case In(Some(ref), List("-")) => onRemoveIssue(ref, currentModel)
//  case In(Some(ref), args) if args.nonEmpty && args.head == "=" => onEditIssue(ref, args.drop(1), currentModel)
//  case In(Some(ref), List("/")) => onForwardIssue(who, ref, currentModel)
//  case In(Some(ref), List("/!")) => onFastForwardIssue(who, ref, currentModel)
//  case In(Some(ref), List(".")) => onBackwardIssue(who, ref, currentModel)
//  case In(Some(ref), List(".!")) => onFastBackwardIssue(who, ref, currentModel)
//  case In(Some(ref), List("@")) => onOwnIssue(who, ref, currentModel)
//  case In(Some(ref), args) if args.nonEmpty && args.size > 1 && args.head == ":" => onTagIssue(ref, args.drop(1), currentModel)
//  case In(Some(ref), args) if args.nonEmpty && args.size > 1 && args.head == ":-" => onDetagIssue(ref, args.drop(1), currentModel)
//  case In(Some("release"), List(tag)) => onRelease(tag, currentModel)
//  case In(Some("releases"), Nil) => onShowReleases(currentModel)
//  case In(head, tail) => onUnknownCommand(head, tail)

  private val next = "next"
  private val doing = "doing"
  private val done = "done"
  private val workflowStates = List(next, doing, done)
  private val aka = "A"
  private val usersToAka = Map("anon" -> aka)

  "set aka" in {
    val current = Model(Nil, Map.empty, Nil, Nil)
    val expected = current.copy(userToAka = usersToAka)
    runAndExpect("aka a", current, expected)
  }

  "add issue" in {
    val current = Model(workflowStates, usersToAka, Nil, Nil)
    val expected = current.copy(issues = List(Issue("1", "an item", None, None)))
    runAndExpect("+ an item", current, expected)
  }

  "add and move forward one state" in {
    val current = Model(workflowStates, usersToAka, Nil, Nil)
    val expected = current.copy(issues = List(Issue("1", "an item", Some(next), Some(aka))))
    runAndExpect("+/ an item", current, expected)
  }

  "add and move forward to end state" in {
    val current = Model(workflowStates, usersToAka, Nil, Nil)
    val expected = current.copy(issues = List(Issue("1", "an item", Some(done), Some(aka))))
    runAndExpect("+/! an item", current, expected)
  }

  "move forward one state" in {
    val issue = Issue("1", "an item", None, None)
    val current = Model(workflowStates, usersToAka, List(issue), Nil)
    val expected = current.copy(issues = List(issue.copy(status = Some(next), by = Some(aka))))
    runAndExpect("1 /", current, expected)
  }

  "move forward to end state" in {
    val issue = Issue("1", "an item", None, None)
    val current = Model(workflowStates, usersToAka, List(issue), Nil)
    val expected = current.copy(issues = List(issue.copy(status = Some(done), by = Some(aka))))
    runAndExpect("1 /!", current, expected)
  }

  "move back a state" in {
    val issue = Issue("1", "an item", Some(doing), None)
    val current = Model(workflowStates, usersToAka, List(issue), Nil)
    val expected = current.copy(issues = List(issue.copy(status = Some(next), by = Some(aka))))
    runAndExpect("1 .", current, expected)
  }

  "move back a state (into backlog)" in {
    val issue = Issue("1", "an item", Some(next), None)
    val current = Model(workflowStates, usersToAka, List(issue), Nil)
    val expected = current.copy(issues = List(issue.copy(status = None, by = Some(aka))))
    runAndExpect("1 .", current, expected)
  }

  private def runAndExpect(in: String, current: Model, expected: Model) {
    run(s"$in", current).updatedModel.mustEqual(Some(expected))
  }

  private def run(in: String, current: Model) = Commander.process(in, "anon", current, RefProvider(0))
}
