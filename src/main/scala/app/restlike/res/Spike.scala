package app.restlike.res

import java.nio.file.{Path, Paths}

import app.ServiceFactory._
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

case class Scenario(name: String, cabinCode: String, brds: Set[String], offs: Set[String], ignored: Set[String] = Set.empty) {
  def run(cache: Cache) = brds.toSeq.map(brd => {
    offs.toSeq.map(off => {
      val trip = Trip(cabinCode, Route(brd, off))
      if (brd == off) ApiCall(trip, Left(s"Pointless"))
      else if (ignored.contains(s"$brd-$off")) ApiCall(trip, Left(s"Ignored"))
      else if (cache.contains(trip.name)) { /*print("+");*/ ApiCall(trip, cache.load(trip.name)) }
      else { print("-"); API.doIt(trip, cache) }
    })
  })
}

case class Trip(cabinCode: String, route: Route) {
  val baCabinName = cabinCode match {
    case "F" => "first"
    case "J" => "business"
    case _ => throw new RuntimeException(s"Unsdupported cabin: $cabinCode")
  }

  val name = s"${route.name}-${cabinCode}"
}

case class Route(brd: String, off: String) {
  val name = s"$brd-$off"
}

object API {
  private val debug = false

  def doIt(trip: Trip, cache: Cache) = {
    val url = s"https://api.ba.com/rest-v1/v1/flightOfferBasic;departureCity=${trip.route.brd};arrivalCity=${trip.route.off};cabin=${trip.baCabinName};journeyType=roundTrip;range=monthLow.json"

    //TODO: save down past results

    val json = getJson(url)
    cache.store(trip.name, json)
    val resp: Either[String, Summary] = json match {
      case Some(j) => Right(parseToSummary(j))
      case None => Left(s"Unavailable")
    }
    Thread.sleep(1000)
    ApiCall(trip, resp)
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

object Months {
  val monthNames = Seq("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

  def date(month: String, day: Int) = {
    var when = systemClock().date.minusDays(systemClock().date.getDayOfMonth).plusDays(day)
    while (monthNames(when.getMonthOfYear -1) != month) {
      when = when.plusMonths(1)
    }
    if (when.isBefore(systemClock().date)) systemClock().date.plusDays(day -1) else when
  }

  def nextMonthsFromNow = {
    var when = systemClock().date.minusDays(systemClock().date.getDayOfMonth).plusDays(1)
    nextMonthsFrom(monthNames(when.getMonthOfYear -1))
  }

  private def nextMonthsFrom(month: String) = {
    val current = monthNames.indexOf(month)
    (monthNames.drop(current) ++ monthNames).take(12)
  }
}

case class GoogleFlight(from: String, to: String, month: String) {
  //TODO: consider a=ONEWORLD
  private val cabin = "b"
//  private val cabin = "f"

  def url = s"https://www.google.com/flights/#search;f=${expand(from)};t=${expand(to)};d=${Months.date(month, 1)};r=${Months.date(month, 5)};sc=${cabin};a=BA"

  private def expand(code: String) =
    if (code == "NYC") "JFK,EWR,LGA"
    else code
}

//CPH-NYC = hot!
object Spike extends App {
  val cache = Cache(systemClock().date)

//  val nonFirst = SEL, BUE, BKK, CTU

  val arbitragable = Set(
    "CPH", "OSL", "HEL", "STO", "GOT",
    "FRA", "DUS", "MUC", "HAM", "BER", /*"CGN" (dead),*/
    "DUB", "BFS",
    "MAD", "BCN",
    "AMS",
//    "JER",
    "PAR", "ZRH", "GVA", "LUX", "BRU",
    "MIL", "ROM",
    "LIS", "OPO")

  val locationArbitrage = Scenario("Location Arbitrage", "F",
    brds = /*Set("LON") ++ */arbitragable,
    offs = Set(
//      "BOS", "NYC", "PHL", "CHI", "LAX",
      "MIA", //"SFO",
//      "DXB",
//      "TYO", "HKG", "CTU" ,"SIN", "KUL", "SHA", "BKK", "BJS",
//      "SEL",
//      "SYD",
      "RIO", "SAO"//,
//      "BUE"
    )
  )

  val europeanBreaks = Scenario("European Breaks", "J",
    brds = Set("LON"),
    offs = arbitragable ++ Set("RAK", "IBZ")
  )

  val scenario = locationArbitrage

  //europe offers seems broken .. largely current month only
  //val scenario = europeanBreaks

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
    val best = groupByOff(k).head
    s"$k: ${groupByOff(k).take(10).map(s => s"${s.lowestedFx} (${s.brd})").mkString(", ")}" +
      " -> " + GoogleFlight(best.brd, best.off, best.lowestMonth).url + " " + best.originalPrice
  })

  val groupByBrd = rights.sortBy(s => (s.brd, s.lowestedFx) ).groupBy(_.brd)
  val byBrd = groupByBrd.keys.toSeq.sorted.map(k => {
    val best = groupByBrd(k).head
    s"$k: ${groupByBrd(k).take(10).map(s => s"${s.lowestedFx} (${s.off})").mkString(", ")}" +
      " -> " + GoogleFlight(best.brd, best.off, best.lowestMonth).url + " " + best.originalPrice
  })

  val deadBrd = scenario.brds -- rights.map(_.brd)
  val deadOff = scenario.offs -- rights.map(_.off)

  //TODO: include TP and price per TP
  //NYC, CTU: 140,
  //CPH: 40
  //HEL: 80
  //TODO: show completely dead routes
  //TODO: show by date - i.e. best fare ber month (colouring will kind of give that)
  //TODO: show diff between LON and arbitraged (include TP)
  //TODO: feed fx rates for each day
  //TODO: use liftweb scheduling to get prices in the am

  //to try:
  //brd:
  //off:
  //see: https://en.wikipedia.org/wiki/International_Air_Transport_Association_airport_code

  //TIP: HEL is 160 TP
  //and malta and FNC
  //http://www.headforpoints.com/2015/08/18/160-tier-point-routes-british-airways-club-europe/

  //TODO: put link in iframe - job done!
  //TODO: do a by month (i.e. I want to go this month)

  //TODO: ultimately it will be one link for month to populate iframe

  //TOD): this is a very interesting url ...
  //https://www.britishairways.com/travel/fx/execclub/_gf/en_fi?eId=111054&data=
  // F100LF251120150740BA0795JHELLHRT251120151215BA0295JLHRORDT011220151710BA0294JORDLHRF021220151115BA0794JLHRHELF&p=EUR1570.10&e=FE&c=J&source=FareQuoteEmail&isEmailHBOFareQuote=false&utm_source=HEL.ORD.flightsearch&utm_medium=com&utm_campaign=googleflightsearch&DM1_Channel=TS&DM1_Campaign=googleflightsearch&DM1_CD=HELORDRT

  // F100LF
  // 25112015 0740 BA0795 J HEL LHR T
  // 25112015 1215 BA0295 J LHR ORD T
  // 01122015 1710 BA0294 J ORD LHR F
  // 02122015 1115 BA0794 J LHR HEL F
  //
  // &p=EUR1570.10&e=FE&c=J&source=FareQuoteEmail&isEmailHBOFareQuote=false&utm_source=HEL.ORD.flightsearch&utm_medium=com&utm_campaign=googleflightsearch&DM1_Channel=TS&DM1_Campaign=googleflightsearch&DM1_CD=HELORDRT

  //TODO: this is also interesting ...
  //https://www.google.com/flights/#search;f=HEL;t=CHI,ORD;d=2015-11-25;r=2015-12-01;sel=HELLHR0BA795-LHRORD0BA295,ORDLHR0BA294-LHRHEL1BA794;sc=b;a=BA;eo=e

  val title = "        BEST" + Months.nextMonthsFromNow.map(m => s"  $m").mkString + "\n"

  println(
    "\n\nBest by Price:\n" + title + byPrice.map(s => s + " -> " + GoogleFlight(s.brd, s.off, s.lowestMonth).url + " " + s.originalPrice).mkString("\n") +
    "\n\nBest by Destination:\n" + byOff.mkString("\n") +
    "\n\nBest by Origin:\n" + byBrd.mkString("\n") +
    "\n\nDead Destinations: " + deadOff.mkString(", ") +
    "\nDead Origins:      " + deadBrd.mkString(", ") +
   s"\nRoutes: ${results.flatten.size}"
  )
}

case object CLIENT_KEY extends HttpHeader {val name = "client-key"}

object JSON_GET {
  def apply(url: Url) = Request(Method.GET, url, Headers(List((CLIENT_KEY, "39kj4ry2ktcxwwhjv9mqtm4w"))))
//  def apply(url: Url) = Request(Method.GET, url, Headers(List((CLIENT_KEY, "faje8sxhy274vk2a9yhpeuth"))))
  def unapply(req: Request): Option[String] = if (req.method == Method.GET) Some(req.url) else None
}

case class Record(DepartureCityCode: String, ArrivalCityCode: String, TravelMonth: String, Price: Price) {
  override def toString() = s"$DepartureCityCode-$ArrivalCityCode $TravelMonth ${Price.Amount.Amount} ${Price.Amount.CurrencyCode}"
}

case class Price(Amount: Amount)
case class Amount(Amount: Double, CurrencyCode: String)

case class ApiCall(trip: Trip, outcome: Either[String, Summary])

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
  private val bestRecords = records.sortBy(_.Price.Amount.Amount)//.head
  val lowestMonth = bestRecords.head.TravelMonth
  val lowestPrice = bestRecords.head.Price.Amount.Amount.round
  val off = records.head.ArrivalCityCode
  val brd = records.head.DepartureCityCode
  private val ccy = first.Price.Amount.CurrencyCode
  val originalPrice = s"(${lowestPrice} ${ccy})"

  val lowestedFx = pad(fxed(lowestPrice).toString)

  def fxed(value: Double) = {
    if (!fx.contains(ccy)) throw new RuntimeException(s"no fx rate for: $ccy")
    pad((value * fx(ccy)).round.toString)
  }

  private def pad(v: String) = if (v.length == 3) " " + v else v

  override def toString() = s"${first.DepartureCityCode}-${first.ArrivalCityCode} ${lowestedFx}: " +
    records.map(r => s"${r.TravelMonth} ${fxed(r.Price.Amount.Amount.round)}").mkString(", ")
}