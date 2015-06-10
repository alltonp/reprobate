package app.restlike.rem

import app.restlike.common.Responder._
import app.restlike.common.{CliRequestJson, JsonRequestHandler, RefProvider, Tracker}
import net.liftweb.http.Req
import net.liftweb.json._

object Controller {
  private var universe = Persistence.load

  //TODO: do we still need who? it's not doing so much for us these days ... kind of nice in tracking
  def process(who: String, req: Req, token: String) = {
    universe.modelFor(token) match {
      case Some(model) => {
        val refProvider = RefProvider(if (model.things.isEmpty) 0 else model.things.map(_.ref.toLong).max)
        JsonRequestHandler.handle(req)((json, req) ⇒ {
          synchronized {
            val value = CliRequestJson.deserialise(pretty(render(json))).value.toLowerCase.trim.replaceAll("\\|", "")
            Tracker(s"${Rem.appName}.tracking").track(who, value)
            val out = Commander.process(value, who, model, refProvider, universe.tokenToUser(token))
            out.updatedModel.foreach(m => {
              universe = universe.updateModelFor(token, m)
              Persistence.save(universe)
            })
            t(s"> ${Rem.appName} $value" :: "" :: out.messages)
          }
        })

      }
      case None => t(List("fail"))
    }
  }
}
