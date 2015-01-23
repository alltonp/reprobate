package jetboot

trait Identifiable {
  //TODO: could be a val maybe
  def id: String
  def element = Element(id)
}
