package jetboot

import xml.NodeSeq

//TODO: possibly this should be a trait with implementations
//TODO: hmmm .. not sure this looks so good these days
case class ButtonPresentation(body: NodeSeq, classes: Classes = Classes(), styles: Styles = Styles())