package app.restlike.res

import io.shaka.http.Http._
import io.shaka.http._
import net.liftweb.json._
import scala.Some
import scala.collection.immutable.HashMap
import scala.collection.{mutable, concurrent}

object Spike extends App {
  private def getJson(url: String) = {
    val r = http(JSON_GET(url)).entityAsString
    if (debug) println("### " + url + " =>\n" + r) else print(".")
    parse(r)
  }

  private def doIt(brd: String, off: String): Unit = {
    val url = s"https://api.ba.com/rest-v1/v1/flightOfferBasic;departureCity=$brd;arrivalCity=$off;cabin=business;journeyType=roundTrip;range=monthLow.json"

    implicit val formats = Serialization.formats(NoTypeHints)
    val json = getJson(url)
    //  val r = json.extract[PricedItinerariesResponse]
    val elements = (json \\ "PricedItinerary").children
    val r = elements.map(acct => acct.extract[Record])
    println(r.mkString("\n"))
  }

  val debug = true
  val brd = "FRA"
  val off = "HKG"

  doIt(brd, off)
}

case object CLIENT_KEY extends HttpHeader {val name = "client-key"}

object JSON_GET {
  def apply(url: Url) = Request(Method.GET, url, Headers(List((CLIENT_KEY, "39kj4ry2ktcxwwhjv9mqtm4w"))))
  def unapply(req: Request): Option[String] = if (req.method == Method.GET) Some(req.url) else None
}

case class PricedItinerariesResponse(pricedItinerary: PricedItinerary)
case class PricedItinerary(items: Array[Record])

case class Record(DepartureCityCode: String, ArrivalCityCode: String, TravelMonth: String, Price: Price) {
  override def toString() = s"$DepartureCityCode $ArrivalCityCode $TravelMonth ${Price.Amount.Amount} ${Price.Amount.CurrencyCode}"
}

case class Price(Amount: Amount)
case class Amount(Amount: Double, CurrencyCode: String)