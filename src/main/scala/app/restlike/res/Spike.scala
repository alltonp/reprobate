package app.restlike.res

import io.shaka.http.Http._
import io.shaka.http._
import net.liftweb.json._

//TODO: look for 'F" too
//CPH-NYC = hot!
object Spike extends App {
  private def getJson(url: String) = {
    val r = http(JSON_GET(url))
    r.entity.map(e => {
      if (debug) println("### " + url + " =>\n" + r) else print(".")
      parse(e.toString())
    })
  }

  private def doIt(brd: String, off: String) = {
//    val cabin = "first"
    val cabin = "business"
    val url = s"https://api.ba.com/rest-v1/v1/flightOfferBasic;departureCity=$brd;arrivalCity=$off;cabin=$cabin;journeyType=roundTrip;range=monthLow.json"

    //TODO: save down past results

    implicit val formats = Serialization.formats(NoTypeHints)
    val json = getJson(url)
    val resp: Either[String, Summary] = json match {
      case Some(j) => {
        //  val r = json.extract[PricedItinerariesResponse]
        val elements = (j \\ "PricedItinerary").children
        val r = elements.map(acct => acct.extract[Record])
//        println("\n" + Summary(r))
        Right(Summary(r))
      }
      case None => Left(s"Unavailable")
    }
    Thread.sleep(1000)
    ApiCall(s"$brd-$off", resp)
  }

  val debug = false

  val ignored = Seq("DUS SIN", "MUC SIN", "DUS CTU", "MUC CTU", "DUS KUL", "MUC KUL", "FRA PVG", "DUS PVG", "MUC PVG",
    "DUS BKK", "MUC BKK", "FRA PEK", "DUS PEK", "MUC PEK")

  private val germany = Seq("FRA", "DUS", "MUC", "HAM"/*, "TXL", "CGN"*/)
  private val hongKongIsh = Seq("HKG", "SIN", "CTU", "KUL", "PVG", "BKK", "PEK")

//  val brds = germany // Seq("DUB", "CPH", "OSL")
//  val offs = hongKongIsh //Seq("LAX", "NYC")
  val brds = Seq("DUB", "CPH", "OSL", "FRA")
  val offs = Seq(/*"LAX", */"NYC", "SYD", "BOS", "HKG", "TYO")

  val results = brds.map(brd => {
    offs.map(off => {
      if (ignored.contains(s"$brd $off")) ApiCall(s"$brd-$off", Left(s"Ignored"))
      else doIt(brd, off)
    })
  })

  val out = results.flatten.map(r => {
    r.outcome match {
      case Left(_) => None
      case Right(x) => Some(x)
    }
  }).flatten.sortBy(_.lowestedFx).mkString("\n")

  println("\n" + out)
}

case object CLIENT_KEY extends HttpHeader {val name = "client-key"}

object JSON_GET {
  def apply(url: Url) = Request(Method.GET, url, Headers(List((CLIENT_KEY, "39kj4ry2ktcxwwhjv9mqtm4w"))))
  def unapply(req: Request): Option[String] = if (req.method == Method.GET) Some(req.url) else None
}

case class Record(DepartureCityCode: String, ArrivalCityCode: String, TravelMonth: String, Price: Price) {
  override def toString() = s"$DepartureCityCode-$ArrivalCityCode $TravelMonth ${Price.Amount.Amount} ${Price.Amount.CurrencyCode}"
}

case class Price(Amount: Amount)
case class Amount(Amount: Double, CurrencyCode: String)

case class ApiCall(query: String, outcome: Either[String, Summary])

case class Summary(records: Seq[Record]) {
  val fx = Map(
    "NOK" -> 0.0777638,
    "DKK" -> 0.0942893,
    "EUR" -> 0.703649
  )

  val first = records.head
  val lowest = records.map(_.Price.Amount.Amount).min

  val lowestedFx = fxed(lowest)

  def fxed(value: Double) = {
    (value * fx(first.Price.Amount.CurrencyCode)).round
  }

//  ${first.DepartureCityCode}-${first.ArrivalCityCode}
  override def toString() = s"${first.DepartureCityCode}-${first.ArrivalCityCode} (${lowestedFx} GBP) ${first.Price.Amount.CurrencyCode}: " +
    records.map(r => s"${r.TravelMonth} ${r.Price.Amount.Amount.round}").mkString(", ")
}