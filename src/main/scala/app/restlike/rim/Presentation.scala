package app.restlike.rim

//TODO: add issue
object Presentation {
  def board(model: Model, changed: Seq[String], aka: String) = {
    groupByStatus(includeReleased = false, includeBacklog = false, hideBy = false, hideTags = false, model.issues, model, changed, Some(aka))
  }

  def release(release: Release) = {
    val r = release.issues.map(i => s"\n  ${i.render(hideStatus = true)}").mkString
    s"${release.tag}: (${release.issues.size})" + r + "\n" :: Nil
  }

  def issuesForUser(aka: String, issues: Seq[Issue]) = {
    val r = issues.map(i => s"\n  ${i.render(hideBy = true)}").mkString
    s"${aka}: (${issues.size})" + r + "\n"
  }

  def tags(all: Seq[Tag]) = sortedByPopularity(all).map(t => s"${t.name} (${t.count})").mkString(", ") :: Nil

  //TODO: we should include the released on the board too
  //TODO: render or remove tag
  def tagDetail(tag: String, issues: Seq[Issue], currentModel: Model) = {
    groupByStatus(includeReleased = true, includeBacklog = true, hideBy = true, hideTags = true, issues, currentModel, Nil, None)
  }

//  //TODO: render or remove release
//  def releaseNotes(release: String, issues: Seq[Issue], currentModel: Model) = {
//    val tagNames = issues.flatMap(_.tags).distinct
//    println(tagNames)
//    val tags = currentModel.tags.filter(t => tagNames.contains(t.name))
//    sieveByTag(sortedByPopularity(tags), issues, currentModel)
//  }

  //TODO: render or remove release
  def releaseNotes2(release: String, issues: Seq[Issue], blessedTags: List[String], currentModel: Model, sanitise: Boolean) = {
    val tagNames = issues.flatMap(_.tags).distinct
    val tags = currentModel.tags.filter(t => tagNames.contains(t.name))
    sieveByTag(sortedByImportance(tags, blessedTags), issues, currentModel, sanitise)
  }

  //TODO: introduce a DisplayOptions()
  //TODO: this is getting well shonky
  private def groupByStatus(includeReleased: Boolean, includeBacklog: Boolean, hideBy: Boolean, hideTags: Boolean, issues: Seq[Issue], currentModel: Model,
                            changed: Seq[String], aka: Option[String]) = {
    val stateToIssues = issues.groupBy(_.status.getOrElse("backlog"))
    val interestingStates = (if (includeBacklog) List("backlog") else Nil) ::: currentModel.workflowStates ::: (if (includeReleased) List("released") else Nil)
    interestingStates.map(s => {
      val issuesForState = stateToIssues.getOrElse(s, Nil)
      val issues = issuesForState.map(i => s"\n  ${
        i.render(hideStatus = true, hideBy = hideBy, hideTags = hideTags, highlight = changed.contains(i.ref), highlightAka = aka)
      }").mkString
      if (issuesForState.isEmpty) None else Some(s"$s: (${issuesForState.size})" + issues + "\n")
    }).flatten
  }

  private def sieveByTag(tags: Seq[Tag], issues: Seq[Issue], currentModel: Model, sanitise: Boolean) = {
    case class TagAndIssues(tag: String, issues: Seq[Issue])
//    println(tags.mkString(", "))
    var remainingIssues = issues
    val r = tags.map(t => {
      val issuesForTag = remainingIssues.filter(_.tags.contains(t.name))
//      println(s"\n$t: $issuesForTag")
      remainingIssues = remainingIssues.diff(issuesForTag)
//      renderTagAndIssues(t.name, issuesForTag)
      TagAndIssues(t.name, SortByStatus(issuesForTag.map(i => i.copy(tags = i.tags.-(t.name))), currentModel))
    }) ++ Seq(TagAndIssues("", SortByStatus(remainingIssues, currentModel)))
    r.filterNot(_.issues.isEmpty)/*.sortBy(_.issues.size)*/.map(tai =>
      renderTagAndIssues(sanitise, tai.tag, tai.issues)
    )
  }

  private def renderTagAndIssues(sanitise: Boolean, tag: String, issuesForTag: Seq[Issue]): String = {
    val issues = issuesForTag.map(i => s"\n  ${
      i.render(hideStatus = sanitise, hideBy = sanitise, hideTags = sanitise)
    }").mkString
    s"$tag: ${if (sanitise) "" else "(${issuesForTag.size})"}" + issues + "\n"
  }

  private def sortedByPopularity(all: Seq[Tag]) = all.sortBy(t => (-t.count, t.name))
  private def sortedByImportance(all: Seq[Tag], blessedTags: List[String]) = {
    val blessed = blessedTags.flatMap(bt => all.find(_.name == bt))
    val remainder = all.diff(blessed).sortBy(_.name)
    blessed ++ remainder
  }
}
