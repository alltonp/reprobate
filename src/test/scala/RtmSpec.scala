import app.ServiceFactory
import app.ServiceFactory._
import app.restlike.common.RefProvider
import app.restlike.rtm._
import im.mange.little.clock.FrozenClock
import org.joda.time.{LocalDate, DateTime}
import org.scalatest.{MustMatchers, WordSpec}

class RtmSpec extends WordSpec with MustMatchers {
  //TODO: this is naughty
  private val clockDate = new DateTime(2015, 2, 1, 0, 0, 0)
  ServiceFactory.systemClock.default.set(FrozenClock(clockDate))

  //TODO: work out what examples are missing
  private val emptyModel = Model(Nil, Nil, Nil)

  //config

  "set priority tags" in {
    val current = emptyModel
    val expected = current.copy(priorityTags = List("a", "b", "c"))
    runAndExpect("tags = a b c", current, expected)
  }

  "unset priority tags" in {
    val current = emptyModel
    val expected = current.copy(priorityTags = Nil)
    runAndExpect("tags =", current, expected)
  }

  //adding

  "add issue" in {
    val current = emptyModel
    val expected = current.copy(things = List(Thing("1", "an item", None)))
    runAndExpect("+ an item", current, expected)
  }

  "add issue (ignoring surplus noise)" in {
    val current = emptyModel
    val expected = current.copy(things = List(Thing("1", "an item", None)))
    runAndExpect("+ an   item  ", current, expected)
  }

//  "add and move forward to begin state" in {
//    val current = emptyModelWithWorkflow
//    val expected = current.copy(issues = List(Thing("1", "an item", Some(next))))
//    runAndExpect("+/ an item", current, expected)
//  }

//  "add and move forward to second state" in {
//    val current = emptyModelWithWorkflow
//    val expected = current.copy(issues = List(Thing("1", "an item", Some(doing))))
//    runAndExpect("+// an item", current, expected)
//  }

//  "add and move forward to end state" in {
//    val current = emptyModelWithWorkflow
//    val expected = current.copy(issues = List(Thing("1", "an item", Some(done))))
//    runAndExpect("+! an item", current, expected)
//  }

  "add with tags" in {
    val current = emptyModel
    val expected = current.copy(things = List(Thing("1", "an item", None, Set("tag1", "tag2"))))
    runAndExpect("+ an item : tag1 tag2", current, expected)
  }

  "strip dodgy chars from tags" in {
    val current = emptyModel
    val expected = current.copy(things = List(Thing("1", "an item", None, Set("tag1", "tag2"))))
    runAndExpect("+ an item : :tag1 :tag2", current, expected)
  }

//  "add and move forward to begin state with tags" in {
//    val current = emptyModelWithWorkflow
//    val expected = current.copy(issues = List(Thing("1", "an item", Some(next), Set("tag1", "tag2"))))
//    runAndExpect("+/ an item : tag1 tag2", current, expected)
//  }

//  "add and move forward to end state with tags" in {
//    val current = emptyModelWithWorkflow
//    val expected = current.copy(issues = List(Thing("1", "an item", Some(done), Set("tag1", "tag2"))))
//    runAndExpect("+! an item : tag1 tag2", current, expected)
//  }

  //editing

  "edit issue retains date, tags and status" in {
    val issue = Thing("1", "an item", Some(new LocalDate(2015, 1, 1)), Set("tag1", "tag2"))
    val current = modelWithThing(issue)
    val expected = current.copy(things = List(Thing("1", "an item edited", Some(new LocalDate(2015, 1, 1)), Set("tag1", "tag2"))))
    runAndExpect("1 = an item edited", current, expected)
  }

  "edit with tags adds tags" in {
    (pending)
    val issue = Thing("1", "an item", Some(new LocalDate(2015, 1, 1)), Set("tag1", "tag2"))
    val current = modelWithThing(issue)
    val expected = current.copy(things = List(Thing("1", "an item edited", Some(new LocalDate(2015, 1, 1)), Set("tag1", "tag2", "tags3"))))
    runAndExpect("1 = an item edited : tag3", current, expected)
  }

  //doing

  "do thing" in {
    val issue = Thing("1", "an item", Some(new LocalDate(2015, 1, 1)), Set("tag1", "tag2"))
    val current = modelWithThing(issue)
    val expected = current.copy(things = Nil, done = List(Thing("1", "an item", Some(new LocalDate(2015, 2, 1)), Set("tag1", "tag2"))))
    runAndExpect("1 !", current, expected)
  }

  "undo thing" in {
    val issue = Thing("1", "an item", Some(new LocalDate(2015, 1, 1)), Set("tag1", "tag2"))
    val current = modelWithDone(issue)
    val expected = current.copy(things = List(Thing("1", "an item", Some(new LocalDate(2015, 1, 1)), Set("tag1", "tag2"))), done = Nil)
    runAndExpect("1 .", current, expected)
  }

  //tagging

//  "tag" in {
//    val issue = Thing("1", "an item", Some(next))
//    val current = modelWithIssue(issue)
//    val expected = current.copy(issues = List(issue.copy(tags = Set("tag"))))
//    runAndExpect("1 : tag", current, expected)
//  }

//  "detag" in {
//    val issue = Thing("1", "an item", Some(next), Set("tag"))
//    val current = modelWithIssue(issue)
//    val expected = current.copy(issues = List(issue.copy(tags = Set.empty)))
//    runAndExpect("1 :- tag", current, expected)
//  }

//  "tag multi" in {
//    val issue = Thing("1", "an item", Some(next))
//    val current = modelWithIssue(issue)
//    val expected = current.copy(issues = List(issue.copy(tags = Set("tag1", "tag2", "tagN"))))
//    runAndExpect("1 : tag1 tag2 tagN", current, expected)
//  }

//  "migrate tag" in {
//    val issue = Thing("1", "an item", Some(next), tags = Set("tag1", "tag2", "tagN"))
//    val current = modelWithIssue(issue)
//    val expected = current.copy(things = List(issue.copy(tags = Set("tagX", "tag2", "tagN"))))
//    runAndExpect("tag1 := tagX", current, expected)
//  }

//  "migrate tag in released" in {
//    val issue = Thing("1", "an item", Some(done), tags = Set("tag1", "tag2", "tagN"))
//    val current = modelWithReleasedIssue(issue)
//    val expected = current.copy(done = List(current.done.head.copy(issues = List(issue.copy(tags = Set("tagX", "tag2", "tagN"))))))
//    runAndExpect("tag1 := tagX", current, expected)
//  }

//  "delete tag" in {
//    val issue = Thing("1", "an item", Some(next), tags = Set("tag1", "tag2", "tagN"))
//    val current = modelWithIssue(issue)
//    val expected = current.copy(things = List(issue.copy(tags = Set("tag1", "tagN"))))
//    runAndExpect("tag2 :--", current, expected)
//  }

//  "delete tag in released" in {
//    val issue = Thing("1", "an item", Some(done), tags = Set("tag1", "tag2", "tagN"))
//    val current = modelWithReleasedIssue(issue)
//    val expected = current.copy(done = List(current.done.head.copy(issues = List(issue.copy(tags = Set("tag1", "tagN"))))))
//    runAndExpect("tag2 :--", current, expected)
//  }

  //show

//  "show board" in {
//    (pending) //TODO: need to start asserting the Out().messages
//    val issue = Thing("1", "an item", Some(done))
//    val current = modelWithIssue(issue)
//    val expected = current.copy(things = List(issue.copy(status = None)))
//    runAndExpect("", current, expected)
//  }

  private def runAndExpect(in: String, current: Model, expected: Model) {
    run(s"$in", current).updatedModel.mustEqual(Some(expected))
  }

  private def run(in: String, current: Model) = Commander.process(in, "anon", current, RefProvider(0))

  private def modelWithTags(tags: List[String]) = Model(Nil, Nil, tags)
  private def modelWithThing(thing: Thing) = Model(List(thing), Nil, Nil)
  private def modelWithDone(thing: Thing) = Model(Nil, List(thing), Nil)
}
