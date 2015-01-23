package app.comet

import net.liftweb.common.Loggable
import net.liftweb.http.CometActor

trait ExceptionHandlingActor extends CometActor with Loggable {
  override def exceptionHandler = {
    case e ⇒ logger.error(this.getClass.getName + " threw an exception", e)
  }

  def handleUnexpectedMessage(actor: CometActor, message: Any) = {
    logger.error(actor.getClass.getName + " received unexpected message:" + message)
  }
}