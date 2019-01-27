package app.restlike.rtm

import server.ServiceFactory.rimServerActor
import app.restlike.common.Colours.customGrey
import app.restlike.common.Responder._
import app.restlike.common._
import app.server.ModelChanged
import net.liftweb.http._
import net.liftweb.json._

object Controller {
  private var universe = Persistence.load

  def process(who: String, req: Req, token: String) = {
    universe.modelFor(token) match {
      case Some(model) => {

//        val refProvider = RefProvider(
//          if (model.allIssuesIncludingReleased.isEmpty) 0
//          else model.allIssuesIncludingReleased.map(_.ref.toLong).max
//        )

        JsonRequestHandler.handle(req)((json, req) ⇒ {
          synchronized {
            val value = CliRequestJson.deserialise(pretty(render(json))).value.toLowerCase.trim.replaceAll("\\|", "")
            Tracker(s"data/${Rtm.appName}.tracking").track(who, value, universe.tokenToUser(token), Nil)
            val out = Commander.process(value, who, model, /*refProvider,*/ token)
            out.updatedModel.foreach(m => {
              universe = universe.updateModelFor(token, m)
              //rimServerActor() ! ModelChanged(Some(m), token, out.changed)
              //println("rtmServerActor notified")
              Persistence.save(universe)
            })
            val result = s"> ${Rtm.appName} $value" :: "" :: out.messages.toList
            t(result.map(customGrey(_)))
          }
        })

      }
      case None => t(List("bad request"))
    }
  }
}