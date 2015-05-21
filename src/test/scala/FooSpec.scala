import app.DateFormatForHumans
import app.ServiceFactory.systemClock
import org.scalatest.{MustMatchers, WordSpec}

class FooSpec extends WordSpec with MustMatchers {
   "test blah" in {
     val dt = systemClock().dateTime
     val formatted = DateFormatForHumans.standardDateTimeFormat.print(dt)
     println(s"original: $dt -> formatted: $formatted")
  }
}
