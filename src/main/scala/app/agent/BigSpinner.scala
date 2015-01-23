package app.agent

import jetboot.{Element, Renderable}

//TODO: should be in jetpack or whatever
case class BigSpinner(id: String, message: String) extends Renderable {
  private val element = Element(id)

  def render =
    <div id={id} style="text-align: center;">
      <p>
        <h4>{message}
          <br/>
        </h4>
        <img src="/images/spinner.gif"/>
      </p>
    </div>

  def start = element.show
  def stop = element.hide
}