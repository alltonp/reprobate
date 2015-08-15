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
      case _ => Right(Spike.parseToSummary(jValue))
    }
    resp
  }

  private def fileFor(route: String) = Paths.get(s"${dir.path}/${route}.json")
}

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

  private def doIt(brd: String, off: String, cache: Cache) = {
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

  val debug = false

  val ignored = Seq("DUS-SIN", "MUC-SIN", "DUS-CTU", "MUC-CTU", "DUS-KUL", "MUC-KUL", "FRA-PVG", "DUS-PVG", "MUC-PVG",
    "DUS-BKK", "MUC-BKK", "FRA-PEK", "DUS-PEK", "MUC-PEK")

  val cache = Cache(systemClock().date)

  private val germany = Seq("FRA", "DUS", "MUC", "HAM"/*, "TXL", "CGN"*/)
  private val hongKongIsh = Seq("HKG", "SIN", "CTU", "KUL", "PVG", "BKK", "PEK")

//  val brds = germany // Seq("DUB", "CPH", "OSL")
//  val offs = hongKongIsh //Seq("LAX", "NYC")
  val brds = Seq("DUB", "CPH", "OSL", "FRA", "MAD", "DUS", "AMS", "JER", "BCN")
  val offs = Seq("LAX", "NYC", "SYD", "BOS", "HKG", "TYO", "MIA", "PHL")
//  val brds = Seq("DUB", "JER")
//  val offs = Seq("LAX")

  val results = brds.map(brd => {
    offs.map(off => {
      if (ignored.contains(s"$brd-$off")) ApiCall(s"$brd-$off", Left(s"Ignored"))
      else if (cache.contains(s"$brd-$off")) { print("+"); ApiCall(s"$brd-$off", cache.load(s"$brd-$off")) }
      else { println("-"); doIt(brd, off, cache) }
    })
  })

  val rights = results.flatten.flatMap(r => {
    r.outcome match {
      case Left(_) => None
      case Right(x) => Some(x)
    }
  })
  
  val byPrice = rights.sortBy(_.lowestedFx)
  val byOff = rights.sortBy(s => (s.off, s.lowestedFx) )
  val byBrd = rights.sortBy(s => (s.brd, s.lowestedFx) )

  //TODO: include TP and price per TP
  //TODO: cache query for the day ... (so no re-asking)

  println(
    "\n\nBy Price:\n" + byPrice.mkString("\n") +
    "\n\nBy Off:\n" + byOff.mkString("\n") +
    "\n\nBy Brd:\n" + byBrd.mkString("\n") +
   s"\n\n(${results.flatten.size})"
  )
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
  val off = records.head.ArrivalCityCode
  val brd = records.head.DepartureCityCode

  val lowestedFx = fxed(lowest)

  def fxed(value: Double) = {
    (value * fx(first.Price.Amount.CurrencyCode)).round
  }

//  ${first.Price.Amount.CurrencyCode}
  override def toString() = s"${first.DepartureCityCode}-${first.ArrivalCityCode} ${lowestedFx} (GBP): " +
    records.map(r => s"${r.TravelMonth} ${fxed(r.Price.Amount.Amount.round)}").mkString(", ")
}