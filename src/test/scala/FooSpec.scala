import server.ServiceFactory._
import org.scalatest.{MustMatchers, WordSpec}

class FooSpec extends WordSpec with MustMatchers {
   "test blah" in {
     val dt = systemClock().dateTime
     val formatted = dateFormats().standardDateTimeFormat.print(dt)
//     println(s"original: $dt -> formatted: $formatted")
  }
}
