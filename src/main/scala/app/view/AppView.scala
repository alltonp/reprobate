package app.view

import xml.{Elem, NodeSeq}

object AppView {
  def page(content: NodeSeq): Elem = <div class="lift:surround?with=app;at=content">{ content }</div>
//  def page(content: NodeSeq): Elem = <div>wibble</div>
//  def apply() = page(<div><lift:comet type="AppCometActor"/><div style="clear:both"/></div>)
//  def apply() = page(<div>monkeys<div style="clear:both"/></div>)
//  def apply() = <div class="lift:surround?with=app;at=content"><div>monkeys</div></div>

  //  def apply(): Elem = <lift:comet type="AppCometActor"/>
  //  def apply(): Elem = <div data-lift={"surround?with=app&at=content"}>moooooo</div>
  //  def apply() = <div class="lift:surround?with=app;at=content"><div>monkeys</div></div>
  //  def apply() = <div data-lift={"surround?with=app&at=content"}><b>monkeys</b></div>


  //this actually renders but no template stuff
  //  def apply() = <div><b>monkeys</b> <lift:comet type="AppCometActor"/></div>

  //this shows nothing, no replacement of id content - wierd the body is in the head!
  def apply() = {
    <div class="lift:surround?with=app;at=content"><div><b>monkeys</b> <lift:comet type="AppCometActor"/></div></div>
  }

  //
}