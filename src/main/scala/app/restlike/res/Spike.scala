package app.restlike.res

import java.nio.file.{Path, Paths}

import app.ServiceFactory.{dateFormats, systemClock}
import im.mange.little.file.Filepath
import io.shaka.http.Http._
import io.shaka.http._
import net.liftweb.json._
import net.liftweb.json.parse
import net.liftweb.json.render
import org.joda.time.LocalDate

import scala.reflect.io.Directory

//TODO: make Route() - route should include fare basis later Y/W/J/F
//TODO: highlight the cheapest fares on each row in colour

case class Cache(date: LocalDate) {
  //Filepath.save should do all of this ..
  private val base = s"res/${dateFormats().fileDateFormat.print(date)}"
  private val dir = Directory(base)
  if (!dir.exists) dir.createDirectory()

  def contains(route: String) = fileFor(route).toFile.exists()
  def store(route: String, json: Option[JValue]) = Filepath.save(pretty(render(json.getOrElse(JObject(Nil)))), fileFor(route))

  def load(route: String) = {
    val jValue: JValue = parse(Filepath.load(fileFor(route)))
    val resp: Either[String, Summary] = jValue match {
      case JObject(Nil) => Left(s"Unavailable")
      case _ => Right(API.parseToSummary(jValue))
    }
    resp
  }

  private def fileFor(route: String) = Paths.get(s"${dir.path}/${route}.json")
}

case class Scenario(name: String, brds: Set[String], offs: Set[String], ignored: Set[String] = Set.empty) {
  def run(cache: Cache) = brds.toSeq.map(brd => {
    offs.toSeq.map(off => {
      if (brd == off) ApiCall(s"$brd-$off", Left(s"Pointless"))
      else if (ignored.contains(s"$brd-$off")) ApiCall(s"$brd-$off", Left(s"Ignored"))
      else if (cache.contains(s"$brd-$off")) { /*print("+");*/ ApiCall(s"$brd-$off", cache.load(s"$brd-$off")) }
      else { print("-"); API.doIt(brd, off, cache) }
    })
  })
}

object API {
  private val debug = false

  def doIt(brd: String, off: String, cache: Cache) = {
    //    val cabin = "first"
    val cabin = "business"
    val url = s"https://api.ba.com/rest-v1/v1/flightOfferBasic;departureCity=$brd;arrivalCity=$off;cabin=$cabin;journeyType=roundTrip;range=monthLow.json"

    //TODO: save down past results

    val json = getJson(url)
    cache.store(s"$brd-$off", json)
    val resp: Either[String, Summary] = json match {
      case Some(j) => Right(parseToSummary(j))
      case None => Left(s"Unavailable")
    }
    Thread.sleep(1000)
    ApiCall(s"$brd-$off", resp)
  }

  def parseToSummary(j: JValue) = {
    implicit val formats = Serialization.formats(NoTypeHints)
    //  val r = json.extract[PricedItinerariesResponse]
    val elements = (j \\ "PricedItinerary").children
    val r = elements.map(acct => acct.extract[Record])
    //        println("\n" + Summary(r))
    Summary(r)
  }

  private def getJson(url: String) = {
    val r = http(JSON_GET(url))
    r.entity.map(e => {
      if (debug) println("### " + url + " =>\n" + r) else print(".")
      parse(e.toString())
    })
  }
}

case class GoogleFlight(from: String, to: String, month: String) {
  def url = s"https://www.google.com/flights/#search;f=${expand(from)};t=${expand(to)};d=${date(month, 1)};r=${date(month, 5)};sc=b;a=BA"

  private def expand(code: String) =
    if (code == "NYC") "JFK,EWR,LGA"
    else code

  private def date(month: String, day: Int) = {
    val monthNames = Seq("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
//    new SimpleDateFormat("MMM").format(cal.getTime()));
    var now = systemClock().date.minusDays(systemClock().date.getDayOfMonth -1)
//    println(monthNames(now.getMonthOfYear -1) + month)
    while (monthNames(now.getMonthOfYear -1) != month) {
      now = now.plusMonths(1)
//      println("more")
    }
//    println(now)
    now.plusDays(day)
  }
}

//TODO: look for 'F" too
//CPH-NYC = hot!
object Spike extends App {
  val cache = Cache(systemClock().date)

  val arbitragable = Set(
    "CPH", "OSL", "HEL", "ARN", "GOT",
    "FRA", "DUS", "MUC", "HAM", "CGN", "TXL",
    "DUB", "BFS",
    "MAD", "BCN",
    "AMS",
//    "JER",
    "CDG", "ZRH", "GVA", "LUX", "BRU",
    "MXP", "FCO",
    "LIS", "OPO")

  val locationArbitrage = Scenario("Location Arbitrage",
    brds = Set("LON") ++ arbitragable,
    offs = Set(
      "BOS", "NYC", "PHL", "ORD", "LAX", "MIA",
      "DXB",
      "TYO", "HKG", "CTU" ,"SIN", "KUL", "PVG", "BKK", "PEK",
//      "ICN",
      "SYD"//,
//      "GIG"//,
//      "EZE"
    )
  )

  val europeanBreaks = Scenario("European Breaks",
    brds = Set("LON"),
    offs = arbitragable ++ Set("RAK", "IBZ")
  )

  val scenario = locationArbitrage
  //seems broken .. largely current month only
//  val scenario = europeanBreaks

  val results = scenario.run(cache)

  val rights = results.flatten.flatMap(r => {
    r.outcome match {
      case Left(_) => None
      case Right(x) => Some(x)
    }
  })
  
  val byPrice = rights.sortBy(_.lowestedFx)

  val groupByOff = rights.sortBy(s => (s.off, s.lowestedFx) ).groupBy(_.off)
  val byOff = groupByOff.keys.toSeq.sorted.map(k => {
    s"$k: ${groupByOff(k).take(10).map(s => s"${s.lowestedFx} (${s.brd})").mkString(", ")}"
  })

  val groupByBrd = rights.sortBy(s => (s.brd, s.lowestedFx) ).groupBy(_.brd)
  val byBrd = groupByBrd.keys.toSeq.sorted.map(k => {
    s"$k: ${groupByBrd(k).take(10).map(s => s"${s.lowestedFx} (${s.off})").mkString(", ")}"
  })

  val deadBrd = scenario.brds -- rights.map(_.brd)
  val deadOff = scenario.offs -- rights.map(_.off)

  //TODO: include TP and price per TP
  //NYC: 140, CPH: 40 (360 round trip)
  //TODO: show completely dead routes
  //TODO: show by date - i.e. best fare ber month (colouring will kind of give that)
  //TODO: show diff between LON and arbitraged (include TP)
  //TODO: feed fx rates for each day

  //to try:
  //brd:
  //off: BJS CHI SHA GRU SFO BUE

  //a url ... https://www.google.com/flights/#search;f=HEL;t=JFK,EWR,LGA;d=2015-10-31;r=2015-11-02;sc=b;a=BA
  //then click calendar - job done!
  //could iframe this up!

  println(
    "\n\nBy Price:\n" + byPrice.mkString("\n") +
    "\n\nBy Destination:\n" + byOff.mkString("\n") +
    "\n\nBy Origin:\n" + byBrd.mkString("\n") +
    "\n\nDead Destination: " + deadOff.mkString(", ") +
    "\nDead Origins:     " + deadBrd.mkString(", ") +
   s"\n\n(${results.flatten.size})"
  )

  val test = byPrice.head
  println(GoogleFlight(test.brd, test.off, "SEP").url)

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
    "SEK" -> 0.0750840,
    "EUR" -> 0.703649,
    "CHF" -> 0.654815,
    "GBP" -> 1.0
  )

  val first = records.head
  val lowest = records.map(_.Price.Amount.Amount).min
  val off = records.head.ArrivalCityCode
  val brd = records.head.DepartureCityCode
  private val ccy = first.Price.Amount.CurrencyCode

  val lowestedFx = {
    val v = fxed(lowest).toString
    if (v.length == 3) " " + v else v
  }

  def fxed(value: Double) = {
    if (!fx.contains(ccy)) throw new RuntimeException(s"no fx rate for: $ccy")
    (value * fx(ccy)).round
  }

//  ${first.Price.Amount.CurrencyCode}
  override def toString() = s"${first.DepartureCityCode}-${first.ArrivalCityCode} ${lowestedFx} (GBP): " +
    records.map(r => s"${r.TravelMonth} ${fxed(r.Price.Amount.Amount.round)}").mkString(", ")
}