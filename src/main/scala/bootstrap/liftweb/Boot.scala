package bootstrap.liftweb

import app.Control._
import app.agent.AppPage
import app.comet.RimPage
import app.restlike.broadcast.BroadcastFlash
import app.restlike.demo.Demo
import app.restlike.rem.Rem
import app.restlike.rim.Rim
import app.restlike.gtd.Gtd
import app.restlike.rtm.Rtm
import app.view.{AppView, RimView}
import app.ServiceFactory
import im.mange.jetpac.page.Pages
import net.liftmodules.JQueryModule
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap.Loc.LocGroup
import net.liftweb.sitemap._
import net.liftweb.util._
import app.restlike.iam.Iam
import app.restlike.dogfood.{Blink1, Dogfood}

class Boot extends Loggable {
  def boot() {
    logger.info("Lift is booting ...")
    fatalOnFailure(unsafeBoot())(logger)
  }

  val topBar = LocGroup("topBar")

  private def unsafeBoot() {
    LiftRules.addToPackages("app")

//    val entries = List(
//      Menu(S ? "Probate") / "index",
//      Menu(S ? "Rim") / "rim"
////      Menu(S ? "Rim") / "index"
//    )
//
//    LiftRules.setSiteMap(SiteMap(entries: _*))
//
//    LiftRules.viewDispatch.append {
//      case List("index") ⇒ Left(() ⇒ Full(AppView()))
//      case List("rim") ⇒ Left(() ⇒ Full(RimView()))
////      case List("index") ⇒ Left(() ⇒ Full(RimView()))
//    }

    val protectedPages = Seq(
      AppPage("index"),
      RimPage("rim", topBar)//,
    )
    Pages(
      protectedPages ++ Seq(
////        LoginFailed(),
////        Logout(logUserOut)
      ): _*
    )

    LiftRules.uriNotFound.prepend(NamedPF("404handler") {
      case (req, failure) ⇒ NotFoundAsTemplate(ParsePath(List("404"), "html", false, false))
    })

    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    LiftRules.useXhtmlMimeType = false
    LiftRules.stripComments.default.set(() ⇒ false)
    LiftRules.explicitlyParsedSuffixes += "csv"

    LiftRules.statelessDispatch.append(Blink1)
    LiftRules.statelessDispatch.append(Dogfood)
    LiftRules.statelessDispatch.append(BroadcastFlash)
    LiftRules.statelessDispatch.append(Demo)

//    LiftRules.statelessDispatch.append(Iam)
    LiftRules.statelessDispatch.append(Rim)
    LiftRules.statelessDispatch.append(Rem)
    LiftRules.statelessDispatch.append(Gtd)
    LiftRules.statelessDispatch.append(Rtm)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    JQueryModule.InitParam.JQuery=JQueryModule.JQuery211

    //TODO: we probably need an init to kick things off.
    ServiceFactory.probeProviderActor()

    logger.info("Lift has Booted.")
  }
}