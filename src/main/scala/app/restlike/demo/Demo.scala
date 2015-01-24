package app.restlike.demo

import app.ServiceFactory._
import im.mange.reprobate.api.Runner
import im.mange.shoreditch.api.Check
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

object AlwaysPassProbe extends Check {
  def run = success
}

case object FailureProbe extends Check {
  def run = failure(List("I always let myself down"))
}

case object FlipFloppingProbe extends Check {
  private var imAFailure = true

  def run = {
    imAFailure = !imAFailure
    if (imAFailure) failure(List("I let myself down every other time")) else success
  }
}

case object OddFailureEvenSuccessMinuteProbe extends Check {
  def run = if (isEven) success else failure(List("Every other minute I feel odd, then I let myself down: " + minute))

  private def minute = systemClock().localDateTime.getMinuteOfHour
  private def isEven = minute % 2 == 0
}

case object OddFailureEvenSuccessHourProbe extends Check {
  def run = if (isEven) success else failure(List("Every other hour I feel odd, then I let myself down: " + hour))

  private def hour = systemClock().localDateTime.getHourOfDay
  private def isEven = hour % 2 == 0
}

case class FailureAfter(after: Int, counter: DemoCounter) extends Check {
  def run = {
    val next = counter.next
    if (next > after) failure(List("I started well, now I just let myself down every time")) else success
  }
}

case class SlowProbe(sleepSeconds: Int) extends Check {
  def run = {
    val sleepUntil = new LocalDateTime().plusSeconds(sleepSeconds)
    while (new LocalDateTime().isBefore(sleepUntil)) {
      Thread.sleep(1000)
      Thread.`yield`()
    }
    success
  }
}