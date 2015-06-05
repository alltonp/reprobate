import app.ServiceFactory
import app.ServiceFactory._
import app.restlike.common.RefProvider
import app.restlike.rim._
import im.mange.little.clock.FrozenClock
import org.joda.time.DateTime
import org.scalatest.{MustMatchers, WordSpec}

class RimSpec extends WordSpec with MustMatchers {
  //TOOD: this is naughty
  ServiceFactory.systemClock.default.set(FrozenClock(new DateTime()))

  //TODO: work out what examples are missing

  private val next = "next"
  private val doing = "doing"
  private val done = "done"
  private val released = "released"
  private val workflowStates = List(next, doing, done)
  private val aka = "A"
  private val aka2 = "B"
  private val usersToAka = Map("anon" -> aka, "anon2" -> aka2)
  private val emptyModelWithWorkflow = Model(workflowStates, usersToAka, Nil, Nil, Nil)

  //config

  "set aka" in {
    val current = Model(Nil, Map("anon2" -> aka2), Nil, Nil, Nil)
    val expected = current.copy(userToAka = usersToAka)
    runAndExpect("aka a", current, expected)
  }

  "set priority tags" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(priorityTags = List("a", "b", "c"))
    runAndExpect("tags = a b c", current, expected)
  }

  "unset priority tags" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(priorityTags = Nil)
    runAndExpect("tags =", current, expected)
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
    runAndExpect("+! an item", current, expected)
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
    runAndExpect("+! an item : tag1 tag2", current, expected)
  }

  //editing

  "edit issue retains by, tags and status" in {
    val issue = Issue("1", "an item", Some(doing), Some(aka), Set("tag1", "tag2"))
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(Issue("1", "an item edited", Some(doing), Some(aka), Set("tag1", "tag2"))))
    runAndExpect("1 = an item edited", current, expected)
  }

  "edit with tags adds tags" in {
    (pending)
    val issue = Issue("1", "an item", Some(doing), Some(aka), Set("tag1", "tag2"))
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(Issue("1", "an item edited", None, None, Set("tag1", "tag2", "tags3"))))
    runAndExpect("1 = an item edited : tag3", current, expected)
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

  "migrate tag" in {
    val issue = Issue("1", "an item", Some(next), None, tags = Set("tag1", "tag2", "tagN"))
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(tags = Set("tagX", "tag2", "tagN"))))
    runAndExpect("tag1 := tagX", current, expected)
  }

  "migrate tag in released" in {
    val issue = Issue("1", "an item", Some(done), None, tags = Set("tag1", "tag2", "tagN"))
    val current = modelWithReleasedIssue(issue)
    val expected = current.copy(released = List(current.released.head.copy(issues = List(issue.copy(tags = Set("tagX", "tag2", "tagN"))))))
    runAndExpect("tag1 := tagX", current, expected)
  }

  //releases

  "releasing moves done issue and status to released" in {
    val issue = Issue("1", "an item", Some(done), None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = Nil, released = List(Release("a", List(issue.copy(status = Some(released))), Some(systemClock().dateTime))))
    runAndExpect("± a", current, expected)
  }

  "releasing ignores other states" in {
    (pending) // fails presumably because nothing to release
    //TODO: need modelWithIssues
    val issue = Issue("1", "an item", Some(doing), None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue), released = Nil)
    runAndExpect("± a", current, expected)
  }

  "migrate legacy 'done' to 'released'" in {
    (pending) // fails presumably because nothing to release
    val issue = Issue("1", "an item", Some(done), None)
    val current = modelWithReleasedIssue(issue)
    val expected = current.copy(released = List(current.released.head.copy(issues = List(issue.copy(status = Some("released"))))))
    runAndExpect("± a", current, expected)
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

  private def run(in: String, current: Model) = Commander.process(in, "anon", current, RefProvider(0))

  private def modelWithTags(tags: List[String]) = Model(workflowStates, usersToAka, Nil, Nil, tags)
  private def modelWithIssue(issue: Issue) = Model(workflowStates, usersToAka, List(issue), Nil, Nil)

  private def modelWithReleasedIssue(issue: Issue) =
    Model(workflowStates, usersToAka, Nil, List(Release("release", List(issue), Some(systemClock().dateTime))), Nil)
}
