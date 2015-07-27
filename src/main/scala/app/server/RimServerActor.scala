package app.server

import app.restlike.rim.Model
import im.mange.jetboot.Bangable
import im.mange.jetboot.comet.{PushToAllSubscribers, Subscriber, MulticastLiftActor, MessageCapturingLiftActor}
import net.liftweb.actor.LiftActor

class RimServerActor extends MessageCapturingLiftActor with MulticastLiftActor with Bangable[Any] {
  override def onCapturedMessage(message: Any, actor: LiftActor) { println(s"$this: $message $actor") }

  def handleMessage: PartialFunction[Any, Unit] = {
    case i:ModelChanged => this ! PushToAllSubscribers(i)
    case e                           => throw new RuntimeException(s"I don't know how to handle a: $e")
  }

  override def afterSubscribe(subscriber: Subscriber) {
    println(s"afterSubscribe $subscriber")
    //    this ! PushAllProductsTo(subscriber)
    //    this ! PushAllChatsTo(subscriber)
  }

}

//TODO: ultimately this could have the before and after and the last x transations
case class Init()
case class ModelChanged(updated: Option[Model], token: String)
