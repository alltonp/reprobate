import app.restlike.rim.{Commander, In, Model}
import org.scalatest.{MustMatchers, WordSpec}

class CommandSpec extends WordSpec with MustMatchers {

  "aka" in {
    val cmd = In(Some("aka"), List("a"))
    val current = Model(Nil, Map.empty, Nil, Nil)
    val expected = current.copy(userToAka = Map("anon" -> "A"))
    val out = Commander.process(cmd, "anon", current)
    out.updatedModel.mustEqual(Some(expected))
  }
}
