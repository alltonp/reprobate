package app.restlike.rim

import app.restlike.common.Responder._
import app.restlike.common._
import net.liftweb.http._
import net.liftweb.json._

object Controller {
  private var model = Persistence.load

  private val refProvider = RefProvider(
    if (model.allIssuesIncludingReleased.isEmpty) 0
    else model.allIssuesIncludingReleased.map(_.ref.toLong).max
  )

  def process(who: String, req: Req) =
    JsonRequestHandler.handle(req)((json, req) ⇒ {
      synchronized {
        val value = CliRequestJson.deserialise(pretty(render(json))).value.toLowerCase.trim.replaceAll("\\|", "")
        Tracker(s"${Rim.appName}.tracking").track(who, value)
        val out = Commander.process(value, who, model, refProvider)
        out.updatedModel.foreach(m => {
          model = m
          Persistence.save(model)
        })
        t(s"> ${Rim.appName} $value" :: "" :: out.messages.toList)
      }
    })
}
