package app.restlike.demo

import app.ServiceFactory._
import im.mange.reprobate.api.{AlwaysPassProbe, Probe, Runner}
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.{GetRequest, Req}
import org.joda.time.LocalDateTime

//TODO: use new RestHelper
object Demo extends RestHelper {
  val demoCounter = DemoCounter()

  serve {
    case Req("demo" :: "success" :: Nil, _, GetRequest) ⇒ () ⇒ { Runner.run(AlwaysPassProbe) }
    case Req("demo" :: "failure" :: Nil, _, GetRequest) ⇒ () ⇒ { Runner.run(FailureProbe) }
    case Req("demo" :: "flipflop" :: Nil, _, GetRequest) ⇒ () ⇒ { Runner.run(FlipFloppingProbe) }
    case Req("demo" :: "oddevenminute" :: Nil, _, GetRequest) ⇒ () ⇒ { Runner.run(OddFailureEvenSuccessMinuteProbe) }
    case Req("demo" :: "oddevenhour" :: Nil, _, GetRequest) ⇒ () ⇒ { Runner.run(OddFailureEvenSuccessHourProbe) }
    case Req("demo" :: "failureafter" :: after :: Nil, _, GetRequest) ⇒ () ⇒ { Runner.run(FailureAfter(Integer.parseInt(after), demoCounter)) }
    case Req("demo" :: "slow" :: sleepSeconds :: Nil, _, GetRequest) ⇒ () ⇒ { Runner.run(SlowProbe(Integer.parseInt(sleepSeconds))) }
  }
}

case class DemoCounter() {
  private var count = 0

  def next = synchronized {
    count += 1
    count
  }
}

case object FailureProbe extends Probe {
  import im.mange.reprobate.api.ProbeResponse._

  def probe = epicFail(List("I always let myself down"))
}

case object FlipFloppingProbe extends Probe {
  import im.mange.reprobate.api.ProbeResponse._

  private var imAFailure = true

  def probe = {
    imAFailure = !imAFailure
    if (imAFailure) epicFail(List("I let myself down every other time")) else win
  }
}

case object OddFailureEvenSuccessMinuteProbe extends Probe {
  import im.mange.reprobate.api.ProbeResponse._

  def probe = if (isEven) win else epicFail(List("Every other minute I feel odd, then I let myself down: " + minute))

  private def minute = systemClock().localDateTime.getMinuteOfHour
  private def isEven = minute % 2 == 0
}

case object OddFailureEvenSuccessHourProbe extends Probe {
  import im.mange.reprobate.api.ProbeResponse._

  def probe = if (isEven) win else epicFail(List("Every other hour I feel odd, then I let myself down: " + hour))


  private def hour = systemClock().localDateTime.getHourOfDay
  private def isEven = hour % 2 == 0
}

case class FailureAfter(after: Int, counter: DemoCounter) extends Probe {
  import im.mange.reprobate.api.ProbeResponse._

  def probe = {
    val next = counter.next
    if (next > after) epicFail(List("I started well, now I just let myself down every time")) else win
  }
}

case class SlowProbe(sleepSeconds: Int) extends Probe {
  import im.mange.reprobate.api.ProbeResponse._

  def probe = {
    val sleepUntil = new LocalDateTime().plusSeconds(sleepSeconds)
    while (new LocalDateTime().isBefore(sleepUntil)) {
      Thread.sleep(1000)
      Thread.`yield`()
    }
    win
  }
}