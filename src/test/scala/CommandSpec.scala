import app.restlike.rim.{Model, In, Commander}
import org.scalatest.{MustMatchers, WordSpec}

class CommandSpec extends WordSpec with MustMatchers {

  "aka" in {
    val cmd = In(Some("aka"), List("pa"))
    val model = Model(Nil, Map.empty, Nil, Nil)
    val out = Commander.process(cmd, "anon", model)
    out.updatedModel mustEqual(model)
  }
}
