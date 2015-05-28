package bootstrap.liftweb

import app.Control._
import app.restlike.broadcast.BroadcastFlash
import app.restlike.demo.Demo
import app.restlike.rem.Rem
import app.restlike.rim.Rim
import app.view.AppView
import app.{ServiceFactory}
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.util._
import app.restlike.iam.Iam
import app.restlike.dogfood.Dogfood

class Boot extends Loggable {
  def boot() {
    logger.info("Lift is booting ...")
    fatalOnFailure(unsafeBoot())(logger)
  }

  private def unsafeBoot() {
    LiftRules.addToPackages("app")

    val entries = List(
      Menu(S ? "Probate") / "index"
    )

    LiftRules.setSiteMap(SiteMap(entries: _*))

    LiftRules.viewDispatch.append {
      case List("index") ⇒ Left(() ⇒ Full(AppView()))
    }
    
    LiftRules.uriNotFound.prepend(NamedPF("404handler") {
      case (req, failure) ⇒ NotFoundAsTemplate(ParsePath(List("404"), "html", false, false))
    })

    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    LiftRules.useXhtmlMimeType = false
    LiftRules.stripComments.default.set(() ⇒ false)
    LiftRules.explicitlyParsedSuffixes += "csv"

    LiftRules.statelessDispatch.append(Dogfood)
    LiftRules.statelessDispatch.append(BroadcastFlash)
    LiftRules.statelessDispatch.append(Demo)
    LiftRules.statelessDispatch.append(Iam)
    LiftRules.statelessDispatch.append(Rim)
    LiftRules.statelessDispatch.append(Rem)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    //TODO: we probably need an init to kick things off.
    ServiceFactory.probeProviderActor()

    logger.info("Lift has Booted.")
  }
}