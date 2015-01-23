package app.comet

import net.liftweb.http.CometActor

trait MessageCapturingCometActor extends CometActor with Subscriber {
  final override def lowPriority = PartialFunction(captureAndHandle)

  private def captureAndHandle(any: Any) {
//    ServiceFactory.backdoor().captureMessage(any, this)
    handleLowPriority(any)
  }

  def handleLowPriority(any: Any)
}