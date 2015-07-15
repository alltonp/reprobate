package app.restlike.gtd

import app.restlike.common.Colours.customGrey
import app.restlike.common.Responder._
import app.restlike.common._
import net.liftweb.http._
import net.liftweb.json._

object Controller {
  private var universe = Persistence.load

  def process(who: String, req: Req, token: String) = {
    universe.modelFor(token) match {
      case Some(model) => {

        val refProvider = RefProvider(
          if (model.allIssuesIncludingDone.isEmpty) 0
          else model.allIssuesIncludingDone.map(_.ref.toLong).max
        )

        JsonRequestHandler.handle(req)((json, req) ⇒ {
          synchronized {
            val value = CliRequestJson.deserialise(pretty(render(json))).value.toLowerCase.trim.replaceAll("\\|", "")
            Tracker(s"${Gtd.appName}.tracking").track(who, value, universe.tokenToUser(token))
            val out = Commander.process(value, who, model, refProvider)
            out.updatedModel.foreach(m => {
              universe = universe.updateModelFor(token, m)
              Persistence.save(universe)
            })
            val result = s"> ${Gtd.appName} $value" :: "" :: out.messages.toList
            t(result.map(customGrey(_)))
          }
        })

      }
      case None => t(List("bad request"))
    }
  }
}