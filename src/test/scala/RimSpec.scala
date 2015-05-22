import app.restlike.rim._
import org.scalatest.{MustMatchers, WordSpec}

class RimSpec extends WordSpec with MustMatchers {

  //TODO: work out what examples are missing

  private val next = "next"
  private val doing = "doing"
  private val done = "done"
  private val workflowStates = List(next, doing, done)
  private val aka = "A"
  private val aka2 = "B"
  private val usersToAka = Map("anon" -> aka, "anon2" -> aka2)
  private val emptyModelWithWorkflow = Model(workflowStates, usersToAka, Nil, Nil)

  "set aka" in {
    val current = Model(Nil, Map("anon2" -> aka2), Nil, Nil)
    val expected = current.copy(userToAka = usersToAka)
    runAndExpect("aka a", current, expected)
  }

  //adding

  "add issue" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", None, None)))
    runAndExpect("+ an item", current, expected)
  }

  "add issue (ignoring surplus noise)" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", None, None)))
    runAndExpect("+ an   item  ", current, expected)
  }

  "add and move forward to begin state" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", Some(next), None)))
    runAndExpect("+/ an item", current, expected)
  }

  "add and move forward to second state" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", Some(doing), Some(aka))))
    runAndExpect("+// an item", current, expected)
  }

  "add and move forward to end state" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", Some(done), Some(aka))))
    runAndExpect("+/! an item", current, expected)
  }

  "add with tags" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", None, None, Set("tag1", "tag2"))))
    runAndExpect("+ an item : tag1 tag2", current, expected)
  }

  "strip dodgy chars from tags" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", None, None, Set("tag1", "tag2"))))
    runAndExpect("+ an item : :tag1 :tag2", current, expected)
  }

  "add and move forward to begin state with tags" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", Some(next), None, Set("tag1", "tag2"))))
    runAndExpect("+/ an item : tag1 tag2", current, expected)
  }

  "add and move forward to end state with tags" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", Some(done), Some(aka), Set("tag1", "tag2"))))
    runAndExpect("+/! an item : tag1 tag2", current, expected)
  }

  //moving

  "move forward one state" in {
    val issue = Issue("1", "an item", Some(doing), None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = Some(done), by = Some(aka))))
    runAndExpect("1 /", current, expected)
  }

  //TODO: this is not completely trivial, e.g. going past done etc
//  "move forward two states" in {
//    val issue = Issue("1", "an item", Some(doing), None)
//    val current = modelWithIssue(issue)
//    val expected = current.copy(issues = List(issue.copy(status = Some(done), by = Some(aka))))
//    runAndExpect("1 //", current, expected)
//  }

  "move forward to an initial leaves disowned" in {
    val issue = Issue("1", "an item", None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = Some(next))))
    runAndExpect("1 /", current, expected)
  }

  "move forward two states" in {
    (pending)
    val issue = Issue("1", "an item", None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = Some(doing), by = Some(aka))))
    runAndExpect("1 //", current, expected)
  }

  "move forward to end state" in {
    val issue = Issue("1", "an item", None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = Some(done), by = Some(aka))))
    runAndExpect("1 /!", current, expected)
  }

  "move back a state" in {
    val issue = Issue("1", "an item", Some(done), None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = Some(doing), by = Some(aka))))
    runAndExpect("1 .", current, expected)
  }

  "move back a state to begin state" in {
    val issue = Issue("1", "an item", Some(doing), None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = Some(next), by = None)))
    runAndExpect("1 .", current, expected)
  }

  //TODO: by should be None
  "move back a state (into backlog)" in {
    val issue = Issue("1", "an item", Some(next), None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = None, by = None)))
    runAndExpect("1 .", current, expected)
  }

  "move back to begin state (into backlog)" in {
    val issue = Issue("1", "an item", Some(done), None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = None, by = None)))
    runAndExpect("1 .!", current, expected)
  }

  //owning

  "own" in {
    val issue = Issue("1", "an item", Some(next), None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(by = Some(aka))))
    runAndExpect("1 @", current, expected)
  }

  "disown" in {
    val issue = Issue("1", "an item", Some(next), Some(aka))
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(by = None)))
    runAndExpect("1 @-", current, expected)
  }

  "assign" in {
    val issue = Issue("1", "an item", Some(next), Some(aka))
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(by = Some(aka2))))
    runAndExpect("1 @= b", current, expected)
  }

  "assign (invalid aka)" in {
    (pending) //TODO: TODO: need to start asserting the Out().messages
    val issue = Issue("1", "an item", Some(next), Some(aka))
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(by = Some(aka2))))
    runAndExpect("1 @= c", current, expected)
  }

  //tagging

  "tag" in {
    val issue = Issue("1", "an item", Some(next), None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(tags = Set("tag"))))
    runAndExpect("1 : tag", current, expected)
  }

  "detag" in {
    val issue = Issue("1", "an item", Some(next), None, Set("tag"))
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(tags = Set.empty)))
    runAndExpect("1 :- tag", current, expected)
  }

  "tag multi" in {
    val issue = Issue("1", "an item", Some(next), None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(tags = Set("tag1", "tag2", "tagN"))))
    runAndExpect("1 : tag1 tag2 tagN", current, expected)
  }

  "edit tag" in {
    val issue = Issue("1", "an item", Some(next), None, tags = Set("tag1", "tag2", "tagN"))
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(tags = Set("tagX", "tag2", "tagN"))))
    runAndExpect("tag1 := tagX", current, expected)
  }

  //show

  "show board" in {
    (pending) //TODO: need to start asserting the Out().messages
    val issue = Issue("1", "an item", Some(done), None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = None, by = None)))
    runAndExpect("", current, expected)
  }


  private def runAndExpect(in: String, current: Model, expected: Model) {
    run(s"$in", current).updatedModel.mustEqual(Some(expected))
  }

  private def run(in: String, current: Model) = RimCommander.process(in, "anon", current, RefProvider(0))

  private def modelWithIssue(issue: Issue) = Model(workflowStates, usersToAka, List(issue), Nil)
}
