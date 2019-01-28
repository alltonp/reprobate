package app.restlike.rim

import server.ServiceFactory.rimServerActor
import app.restlike.common.Colours.customGrey
import app.restlike.common.Responder._
import app.restlike.common._
import app.server.ModelChanged
import net.liftweb.http._
import net.liftweb.json._
import server.ServiceFactory

object Controller {
  private var universe = Persistence.load

  val tokensHead = universe.tokenToAccess.keys.head

  def process(who: String, req: Req, token: String) = {
    JsonRequestHandler.handle(req)((json, req) ⇒ {
      val value = CliRequestJson.deserialise(pretty(render(json))).value.toLowerCase.trim.replaceAll("\\|", "")
      t(execute(who, token, value))
    })
  }

  def execute(who: String, token: String, value: String): List[String] = {
    synchronized {
      universe.modelForCli(token) match {
        case Some(model) => {
          val refProvider = RefProvider(
            if (model.allIssuesIncludingReleased.isEmpty) 0
            else model.allIssuesIncludingReleased.map(_.ref.toLong).max
          )

          val out = Commander.process(value, who, model, refProvider, token)

          Tracker(s"${ServiceFactory.dataDir}/${Rim.appName}.tracking").track(who, value, token, out.changed)

          out.updatedModel.foreach(m => {
            universe = universe.updateModelFor(token, m)
            rimServerActor() ! ModelChanged(Some(m), token, out.changed)
//            println("rimServerActor notified")
            Persistence.save(universe)
          })
          val result = s"> ${Rim.appName} $value" :: "" :: out.messages.toList
          result.map(customGrey(_))
        }

        case None => List("bad request")
      }
    }
  }
}