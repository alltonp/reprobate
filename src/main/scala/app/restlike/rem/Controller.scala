package app.restlike.rem

import app.restlike.common.{Tracker, CliRequestJson, JsonRequestHandler, RefProvider}
import app.restlike.common.Responder._
import net.liftweb.http.Req
import net.liftweb.json._

object Controller {
  private var universe = Persistence.load

  def process(who: String, req: Req, token: String) = {
    println(token)
    if (universe.tokenToUser.contains(token)) {
      val user = universe.tokenToUser(token)
      val model = universe.userToModel(user)
      val refProvider = RefProvider(if (model.things.isEmpty) 0 else model.things.map(_.ref.toLong).max)

      JsonRequestHandler.handle(req)((json, req) ⇒ {
        synchronized {
          val value = CliRequestJson.deserialise(pretty(render(json))).value.toLowerCase.trim.replaceAll("\\|", "")
          Tracker(s"${Rem.appName}.tracking").track(who, value)
          val out = Commander.process(value, who, model, refProvider)
          out.updatedModel.foreach(m => {
            universe = universe.copy(userToModel = universe.userToModel.updated(user, m))
            Persistence.save(universe)
          })
          t(out.messages)
        }
      })
    } else {
      t(List("fail"))
    }
  }
}
