package server.tea

import java.io.FileNotFoundException
import java.net.ConnectException
import java.util.concurrent.TimeUnit._

import server.ServiceFactory._
import app.model.{Broadcast, ProbateRegistry, Probe, ProbeRegistry}
import app.probe.HttpClient
import app.restlike.broadcast.BroadcastFlash
import app.restlike.dogfood.{GetProbeStatuses, GetState, ProbeStatuses}
import app.server._
import im.mange.jetpac.Bangable
import im.mange.jetpac.comet._
import im.mange.reprobate.api.Json

import scala.concurrent.{Await, _}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration


class Update extends MessageCapturingLiftActor with MulticastLiftActor with Bangable[Any] {
  override def onCapturedMessage(message: Any) { }

  //TODO: lose field and delegate to ServiceFactory.model() - maybe not, see TODO's at handler. below
  private val modelInstance = Model()

  private def model() = modelInstance

  this ! ExecuteProbeRun

  //TODO: possibly pass this to an undate() function that takes model and returns model
  //then handler is basically....   _ => SendToView(update(model, _)
  //would need to save the model obvs
  //in which case maybe this class just becomes App/Teat and model not in ServiceFactory, but app() is

  def handleMessage: PartialFunction[Any, Unit] = {
    case ExecuteProbeRun => onExecuteProbeRun()
    case PreExecuteProbe(p) => onPreExecuteProbe(p)
    case ReallyExecuteProbe(p) => onReallyExecuteProbe(p)
    case PostExecuteProbe(p, r) => onPostExecuteProbe(p, r)
    case u:CurrentRunStatusUpdate => onCurrentRunStatusUpdate(u)
    //TODO: should probably rename this to StatusMessage
    case u:Message => onMessage(u)
    case u:AllRunsStatusUpdate => onAllRunsStatusUpdate(u)
    case u:ProbeStatusUpdate => onProbeStatusUpdate(u)
    case ProbeSummaryRequest(s) => onProbeSummaryRequest(s)
    case ProbeConfigRequest(s) => onProbeConfigRequest(s)
    case BroadcastsRequest(s) => onBroadcastsRequest(s)
    //TODO: should probably rename this to StatusMessage
    case b:BroadcastFlash => onBroadcast(b)
    case GetProbeStatuses => reply(ProbeStatuses(model().currentProbeStatuses.failures))
    case GetState => reply(model().state)
    case c:ConfigChanged => onConfigChanged(c)
  }

  private def onConfigChanged(configChanged: ConfigChanged) {
    this ! PushToAllSubscribers(configChanged)
  }

  private def onExecuteProbeRun() {
    val thisInstance = this
    //TODO: old messages should die
    thisInstance ! createCurrentRunStatusUpdate
    thisInstance ! createAllRunsStatusUpdate

    //TODO: should probably be scheduled
    Thread.sleep(1000) //TODO: make me a config - sleep between probes

    new Thread(new Runnable() {
      override def run() {
        var in = 10
        while (in > 0) {
          Thread.sleep(1000)
          thisInstance ! createMessageUpdate("waiting", "Next run starting in ... " + in)
          in = in - 1
        }

        val nextRun = model().createProbeRun

        if (model().currentRun.probes != nextRun.probes) {
//          model().probeRunHistory.add(model().currentRun)
//          model().currentRun.executedCount
          model().onConfigChanged()
          thisInstance ! ConfigChanged(nextRun.probes)
          println("### " + dateFormats().timeNow + " - configuration change")
          thisInstance ! createMessageUpdate("detected", "Configuration change")
          Thread.sleep(2000)
        }

        model().currentRun = nextRun
        model().probeRunHistory.add(model().currentRun)

        thisInstance ! PreExecuteProbe(model().currentRun.nextToRun)
      }
    }).start()
  }

  //TODO: progress still iffy on the first run ...
  private def onPreExecuteProbe(probe: Probe) {
    println("### " + dateFormats().timeNow + " - checking: " + probe.name)

    this ! createCurrentRunStatusUpdate
    this ! createMessageUpdate("checking", probe.name)
    this ! ReallyExecuteProbe(probe)
  }

  private def onReallyExecuteProbe(probe: Probe) {
//    //TODO: should probably be scheduled
//    Thread.sleep(1000) //TODO: make me a config - sleep between probes

    val result = doRunRun(probe)

    model().currentRun.update(probe, result)
    model().incidentLog.update(probe, result)
    model().currentProbeStatuses.update(probe, result)

    this ! PostExecuteProbe(probe, result)
  }

  private def onPostExecuteProbe(probe: Probe, result: ProbeStatus) {
    this ! ProbeStatusUpdate(probe, result, model().incidentLog.currentOpenIncident(probe))
    //TODO: maybe not the best thing to do (or place to do it)
    this ! createAllRunsStatusUpdate

    //TODO: this probably shouldn't be here
    this ! (if (model().currentRun.runFinished) ExecuteProbeRun
            else PreExecuteProbe(model().currentRun.nextToRun))
  }

  //TODO: get out sooner when !probe.isActive
  //TODO: why does probe took too long kill later probes
  //http://stackoverflow.com/questions/13097754/asynchronous-io-in-scala-with-futures
  //TODO: the message seems out of whack ... it's the not the one we actually checking ...
  //TODO: this is little-server in disguise
  private def doRunRun(probe: Probe): ProbeStatus = {
    try {
      //OR blocking
      val f = future { blocking {
        //TODO: should probably be scheduled
        //begin move this out ..
        Thread.sleep(1000) //TODO: make me a config - sleep between probes
        if (probe.isActive && model().broadcastLog.notInAReleaseWindow(probe)) unsafeRun(probe) else ProbeInactive }
        //end move this out
      }
      f onSuccess { case status => status }
      f onFailure { case e => probeFailed(exceptionMessage(e), probe) }
      //TODO: make timeout be configurable
      Await.result(f, Duration(30, SECONDS))
    } catch {
      //TODO: might need to have a handler for this
      case e: TimeoutException => {
        println("### e:" + e + " with " + probe.description)
        probeFailed("Check took too long", probe)
      }
      //TODO: more gracefully handle ...
      //net.liftweb.json.JsonParser.ParseException
      //CancellationException
      //InterruptedException
      case e: FileNotFoundException => probeFailed("Check does not exist", probe)
      case e: ConnectException => probeFailed("Server not responding", probe)
      case e: Exception => probeFailed(exceptionMessage(e), probe)
    }
  }

  private def exceptionMessage(e: Throwable) = e.getClass.getCanonicalName + ": " + e.getMessage

  private def probeFailed(message: String, probe: Probe) = ProbeFailure(List(message))

  private def unsafeRun(probe: Probe) = {
    val raw = HttpClient.unsafeGet(probe.url, probe.needsProxy)
    try {
      val probeResponse = Json.deserialise(raw)
      if (probeResponse.failures.isEmpty) ProbeSuccess else ProbeFailure(probeResponse.failures)
    }

    catch {
      case e: Exception => ProbeFailure(List("Check returned unexpected response", raw))
    }
  }

  private def createCurrentRunStatusUpdate = CurrentRunStatusUpdate(model().currentRun.successCount,
    model().currentRun.failureCount, model().currentRun.ignoreCount, model().currentRun.totalCount)

  private def createMessageUpdate(subject: String, detail: String) = Message(subject, detail)

  def createAllRunsStatusUpdate = AllRunsStatusUpdate(model().probeRunHistory.totalExecuted,
    model().incidentLog.totalIncidents, model().incidentLog.open, model().incidentLog.closed)

  private def onProbeStatusUpdate(update: ProbeStatusUpdate) { this ! PushToAllSubscribers(update)}
  private def onCurrentRunStatusUpdate(update: CurrentRunStatusUpdate) { this ! PushToAllSubscribers(update) }
  private def onMessage(update: Message) { this ! PushToAllSubscribers(update) }
  private def onAllRunsStatusUpdate(update: AllRunsStatusUpdate) { this ! PushToAllSubscribers(update) }

  private def onBroadcast(flash: BroadcastFlash) {
    val broadcast = Broadcast(flash.messages, flash.env, flash.durationSeconds)
    model().broadcastLog.update(broadcast)
    this ! PushToAllSubscribers(broadcast)
  }

  //TODO: if jetboot exposed number of subscrivers (but not the actual subscribers)
  override def afterSubscribe(subscriber: Subscriber) {
    println("### " + dateFormats().timeNow + " - onSubscribe: " + subscriber)

    subscriber ! app.comet.Init(model().currentRun.probes)
    model().currentProbeStatuses.statuses.map { p => subscriber ! ProbeStatusUpdate(p._1, p._2, model().incidentLog.currentOpenIncident(p._1)) }
  }

  override def afterUnsubscribe(subscriber: Subscriber) {
    println("### " + dateFormats().timeNow + " - onUnsubscribe: " + subscriber)
  }

  //TODO: we should use ForSubscriber
  private def onProbeSummaryRequest(subscriber: Subscriber) {
    subscriber ! ProbeSummaryResponse(model().probeRunHistory.probesWithHistory)
  }

  private def onProbeConfigRequest(subscriber: Subscriber) {
    subscriber ! ProbeConfigResponse(ProbeRegistry.loadRaw.mkString("\n"))
  }

  private def onBroadcastsRequest(subscriber: Subscriber) {
    subscriber ! BroadcastsResponse(model().broadcastLog.mostRecent)
  }
}
