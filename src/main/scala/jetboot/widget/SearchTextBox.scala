package jetboot.widget

import java.util.concurrent.atomic.AtomicInteger
import net.liftweb.util.Schedule

//TODO: might be nice to have a clear button too (or perhaps that another widget entirely)
case class SearchTextBox(id: String, doSearch: String ⇒ Unit, default: String = "", placeholder: String = "", delay: Long = 500, onlyUpdateWhenChanged: Boolean = true) extends LiveTextBox {
  private var currentQuery = default
  private val lock = AnyRef
  private val counter = new AtomicInteger(0)

  def onKeyUp(query: String) { if (onlyUpdateWhenChanged && currentQuery != query) queueUp(query) }

  private def queueUp(query: String) {
    lock.synchronized {
      currentQuery = query
      if (counter.intValue() == 0) Schedule.perform(actuallyDoSearch _, delay)
      counter.incrementAndGet
    }
  }

  private def actuallyDoSearch() {
    lock.synchronized {
      counter.set(0)
      doSearch(currentQuery)
    }
  }
}