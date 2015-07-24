package app.comet

import net.liftweb.http.CometActor

//TODO: kill this and use the jetboot one
@deprecated("use jetboot", "24-jul-2015")
trait MessageCapturingCometActor extends CometActor with Subscriber {
  final override def lowPriority = PartialFunction(captureAndHandle)

  private def captureAndHandle(any: Any) {
//    ServiceFactory.backdoor().captureMessage(any, this)
    handleLowPriority(any)
  }

  def handleLowPriority(any: Any)
}