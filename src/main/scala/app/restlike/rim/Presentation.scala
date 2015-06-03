package app.restlike.rim

//TODO: add issue
object Presentation {
  def board(model: Model, changed: Seq[String], aka: String) = {
    groupByStatus(hideBy = false, hideTags = false, model.issues, model, changed, Some(aka))
  }

  def release(release: Release) = {
    val r = release.issues.map(i => s"\n  ${i.render(hideStatus = true)}").mkString
    s"${release.tag}: (${release.issues.size})" + r + "\n" :: Nil
  }

  def issuesForUser(aka: String, issues: List[Issue]) = {
    val r = issues.map(i => s"\n  ${i.render(hideBy = true)}").mkString
    s"${aka}: (${issues.size})" + r + "\n"
  }

  def tags(all: Seq[Tag]) =
    ": " + sortedByPopularity(all).map(t => s"${t.name} (${t.count})").mkString(", ") :: Nil

  //TODO: we should include the released on the board too
  //TODO: render or remove tag
  def tagDetail(tag: String, issues: Seq[Issue], currentModel: Model) = {
    groupByStatus(hideBy = true, hideTags = true, issues, currentModel, Nil, None)
  }

  //TODO: render or remove release
  def releaseNotes(release: String, issues: Seq[Issue], currentModel: Model) = {
    val tagNames = issues.flatMap(_.tags).distinct
    println(tagNames)
    val tags = currentModel.tags.filter(t => tagNames.contains(t.name))
    sieveByTag(sortedByPopularity(tags), issues, currentModel)
  }

  private def groupByStatus(hideBy: Boolean, hideTags: Boolean, issues: Seq[Issue], currentModel: Model, changed: Seq[String], aka: Option[String]) = {
    val stateToIssues = issues.groupBy(_.status)
    currentModel.workflowStates.map(s => {
      val issuesForState = stateToIssues.getOrElse(Some(s), Nil)
      val issues = issuesForState.map(i => s"\n  ${
        i.render(hideStatus = true, hideBy = hideBy, hideTags = hideTags, highlight = changed.contains(i.ref), highlightAka = aka)
      }").mkString
      s"$s: (${issuesForState.size})" + issues + "\n"
    })
  }

  private def sieveByTag(tags: Seq[Tag], issues: Seq[Issue], currentModel: Model) = {
    case class TagAndIssues(tag: String, issues: Seq[Issue])
//    println(tags.mkString(", "))
    var remainingIssues = issues
    val r = tags.map(t => {
      val issuesForTag = remainingIssues.filter(_.tags.contains(t.name))
//      println(s"\n$t: $issuesForTag")
      remainingIssues = remainingIssues.diff(issuesForTag)
//      renderTagAndIssues(t.name, issuesForTag)
      TagAndIssues(t.name, issuesForTag)
    }) ++ Seq(TagAndIssues("?", remainingIssues))
    r.filterNot(_.issues.isEmpty).sortBy(_.issues.size).reverseMap(tai => renderTagAndIssues(tai.tag, tai.issues))
  }

  private def renderTagAndIssues(tag: String, issuesForTag: Seq[Issue]): String = {
    val issues = issuesForTag.map(i => s"\n  ${
      i.render(hideStatus = true, hideBy = true)
    }").mkString
    s"$tag: (${issuesForTag.size})" + issues + "\n"
  }

  private def sortedByPopularity(all: Seq[Tag]) = all.sortBy(t => (-t.count, t.name))
}
