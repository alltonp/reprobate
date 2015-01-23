package app

import net.liftweb.common.Logger

object Control {
  def logAndSuppressExceptions(logger: Logger)(f: ⇒ Unit) { try { f } catch { case e: Throwable ⇒ logger.error(e) } }

  def fatalOnFailure(f: ⇒ Unit)(implicit logger: Logger) {
    try {
      f
    } catch {
      case e: Throwable ⇒
        logger.error("A Fatal Error has occurred. The system will be shutdown immediately.", e)
        e.printStackTrace()
        System.exit(1)
    }
  }
}