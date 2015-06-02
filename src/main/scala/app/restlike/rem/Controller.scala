package app.restlike.rem

import app.restlike.common.Responder._
import app.restlike.common.{CliRequestJson, JsonRequestHandler, RefProvider, Tracker}
import net.liftweb.common.Box
import net.liftweb.http.{LiftResponse, Req}
import net.liftweb.json._

object Controller {
  private var model = Persistence.load
  //TODO: this is wrong .. if you have everything release the counter will return to 0 (after a restart)
  //TODO: needs to include the released max ref as weel
  private val refProvider = RefProvider(if (model.things.isEmpty) 0 else model.things.map(_.ref.toLong).max)

  def process(who: String, req: Req): Box[LiftResponse] =
    JsonRequestHandler.handle(req)((json, req) ⇒ {
      synchronized {
        val value = CliRequestJson.deserialise(pretty(render(json))).value.toLowerCase.trim.replaceAll("\\|", "")
        Tracker("rem.tracking").track(who, value)
        val out = RemCommander.process(value, who, model, refProvider)
        out.updatedModel.foreach(m => {
          model = m
          Persistence.save(model)
        })
        t(out.messages)
      }

      //TODO:
      //show count of issues
      //show count of releases
    })
}
