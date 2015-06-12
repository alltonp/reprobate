import app.restlike.common.RefProvider
import app.restlike.rem.Commander
import app.restlike.rem._
import org.scalatest.{MustMatchers, WordSpec}

class RemSpec extends WordSpec with MustMatchers {

  //TODO: work out what examples are missing

  private val aka = "A"
  private val aka2 = "B"
  private val usersToAka = Map("anon" -> aka, "anon2" -> aka2)
  private val emptyModelWithWorkflow = Model(/*usersToAka, */Nil)

//  "set aka" in {
//    val current = Model(Map("anon2" -> aka2), Nil)
//    val expected = current.copy(userToAka = usersToAka)
//    runAndExpect("aka a", current, expected)
//  }

  //adding

  "add thing" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(things = List(Thing("1", "key", Some("value"))))
    runAndExpect("+ key = value", current, expected)
  }

  "add thing (ignoring surplus noise)" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(things = List(Thing("1", "key", Some("value"))))
    runAndExpect("+ key   =   value  ", current, expected)
  }

  "add thing (no value)" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(things = List(Thing("1", "key", None)))
    runAndExpect("+ key", current, expected)
  }

  "add with tags" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(things = List(Thing("1", "key", Some("value"), Set("tag1", "tag2"))))
    runAndExpect("+ key = value : tag1 tag2", current, expected)
  }

  "add with tags (and no value)" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(things = List(Thing("1", "key", None, Set("tag1", "tag2"))))
    runAndExpect("+ key : tag1 tag2", current, expected)
  }

  "strip dodgy chars from tags" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(things = List(Thing("1", "key", None, Set("tag1", "tag2"))))
    runAndExpect("+ key : :tag1 :tag2", current, expected)
  }

  //editing

  "edit value" in {
    val issue = Thing("1", "key", Some("value"))
    val current = modelWithThing(issue)
    val expected = current.copy(things = List(Thing("1", "key", Some("new value"))))
    runAndExpect("1 _= new value", current, expected)
  }

  "edit key" in {
    val issue = Thing("1", "key", Some("value"))
    val current = modelWithThing(issue)
    val expected = current.copy(things = List(Thing("1", "new key", Some("value"))))
    runAndExpect("1 =_ new key", current, expected)
  }

  //tagging

  "tag" in {
    (pending)
    val issue = Thing("1", "key", None)
    val current = modelWithThing(issue)
    val expected = current.copy(things = List(issue.copy(tags = Set("tag"))))
    runAndExpect("1 : tag", current, expected)
  }

  "detag" in {
    (pending)
    val issue = Thing("1", "key", None, Set("tag"))
    val current = modelWithThing(issue)
    val expected = current.copy(things = List(issue.copy(tags = Set.empty)))
    runAndExpect("1 :- tag", current, expected)
  }

  "tag multi" in {
    (pending)
    val issue = Thing("1", "key", None)
    val current = modelWithThing(issue)
    val expected = current.copy(things = List(issue.copy(tags = Set("tag1", "tag2", "tagN"))))
    runAndExpect("1 : tag1 tag2 tagN", current, expected)
  }

  "edit tag" in {
    (pending)
    val issue = Thing("1", "key", None, tags = Set("tag1", "tag2", "tagN"))
    val current = modelWithThing(issue)
    val expected = current.copy(things = List(issue.copy(tags = Set("tagX", "tag2", "tagN"))))
    runAndExpect("tag1 := tagX", current, expected)
  }

  //migrate?
//  "edit tag in released" in {
//    val issue = Issue("1", "an item", Some(next), None, tags = Set("tag1", "tag2", "tagN"))
//    val current = modelWithReleasedIssue(issue)
//    val expected = current.copy(released = List(current.released.head.copy(issues = List(issue.copy(tags = Set("tagX", "tag2", "tagN"))))))
//    runAndExpect("tag1 := tagX", current, expected)
//  }

  private def runAndExpect(in: String, current: Model, expected: Model) {
    run(s"$in", current).updatedModel.mustEqual(Some(expected))
  }

  private def run(in: String, current: Model) = Commander.process(in, "anon", current, RefProvider(0), "foo@bar.com")

  private def modelWithThing(issue: Thing) = Model(/*usersToAka, */List(issue))
}
