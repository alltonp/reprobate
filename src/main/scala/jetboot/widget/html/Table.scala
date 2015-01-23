package jetboot.widget.html

import jetboot.{Styleable, Renderable}

//TODO: okay, theses are definitely all Elements

case class Th(content: Renderable) extends Renderable with Styleable {
  def render = <th class={classes.render} style={styles.render}>{content.render}</th>
}

case class Td(content: Renderable) extends Renderable with Styleable {
  def render = <td class={classes.render} style={styles.render}>{content.render}</td>
}

case class Tr(content: Renderable) extends Renderable with Styleable {
  def render = <tr class={classes.render} style={styles.render}>{content.render}</tr>
}

case class Tbody(content: Renderable) extends Renderable with Styleable {
  def render = <tbody class={classes.render} style={styles.render}>{content.render}</tbody>
}

case class Thead(content: Renderable) extends Renderable with Styleable {
  def render = <thead class={classes.render} style={styles.render}>{content.render}</thead>
}

//TODO: not sure about this table class here .. that looks like a Bootstrap thing
case class Table(thead: Thead, tbody: Tbody) extends Renderable with Styleable {
  def render =
    <table class={classes.add("table").render} style={styles.render}>
      {thead.render}
      {tbody.render}
    </table>
}