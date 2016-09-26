package app.restlike.rim

import app.ServiceFactory.dateFormats
import org.joda.time.DateTime

//TODO: add issue
object Presentation {
  def basedOnUpdateContext(model: Model, updatedIssue: Issue, aka: String) = {
    if (model.onBoard_?(updatedIssue)) Presentation.board(model, changed = Seq(updatedIssue.ref), aka)
    else Messages.successfulUpdate(s"${updatedIssue.render(model)}")
  }

  def preWorkflowState(model: Model, aka: Option[String]) = {
    val matching = model.issues.filter(i => i.status == Some(0))
    if (matching.isEmpty) s"${model.config.preWorkflowState} is empty" :: Nil
    else matching.map(i => i.render(model, highlightAka = aka, hideStatus = true))
  }

  def board(model: Model, changed: Seq[String], aka: String, hideBy: Boolean = false) = {
    groupByStatus(model, compressEmptyStates = false, includeReleased = false, includePreWorkflowState = false, hideBy = hideBy, hideTags = false, model.issues, model, changed, Some(aka))
  }

  def release(model: Model, release: Release, highlightAka: Option[String]) = {
    val r = release.issues.map(i => s"\n ${i.render(model, hideStatus = true, highlightAka = highlightAka)}").mkString
    s"${release.tag}: (${release.issues.size})${release.when.fold("")(ts => " - " + dateFormats().today(new DateTime(ts)))}" + r + "\n" :: Nil
  }

  def issuesForUser(model: Model, aka: String, issues: Seq[Issue]) = {
    if (issues.isEmpty) ""
    else {
      val r = issues.map(i => s"\n ${i.render(model, hideBy = true)}").mkString
      s"${aka}: (${issues.size})" + r + "\n"
    }
  }

  def tags(all: Seq[Tag]) = sortedByPopularity(all).map(t => s"${t.name} (${t.count})").mkString(", ") :: Nil

  //TODO: we should include the released on the board too
  //TODO: render or remove tag
  def tagDetail(tag: String, issues: Seq[Issue], currentModel: Model) = {
    groupByStatus(currentModel, compressEmptyStates = true, includeReleased = true, includePreWorkflowState = true, hideBy = true, hideTags = true, issues, currentModel, Nil, None)
  }

  //TODO: render or remove release
  //TODO: we should show the release name if its a release ...
  def pointyHairedManagerView(issues: Seq[Issue], blessedTags: List[String], currentModel: Model, aka: String, hideStatus: Boolean, hideBy: Boolean, hideTags: Boolean, hideId: Boolean, hideCount: Boolean) = {
    val tagNames = issues.flatMap(_.tags).distinct
    val tags = currentModel.tags.filter(t => tagNames.contains(t.name))
    sieveByTag(sortedByImportance(tags, blessedTags), issues, currentModel, aka, hideStatus, hideBy, hideTags, hideId, hideCount)
  }

  //TODO: introduce a DisplayOptions()
  //TODO: this is getting well shonky
  //TODO: this should show a nice "there is nothing to see" if that is the case
  private def groupByStatus(model: Model, compressEmptyStates: Boolean, includeReleased: Boolean, includePreWorkflowState: Boolean, hideBy: Boolean, hideTags: Boolean, issues: Seq[Issue], currentModel: Model,
                            changed: Seq[String], aka: Option[String]) = {
    val stateToIssues: Map[Int, Seq[Issue]] = issues.groupBy(_.status.getOrElse(currentModel.config.lastWorkflowStateIncludingPre + 1))
    val interestingStates = currentModel.config.allStates ++ (if (includeReleased) Seq(currentModel.config.postWorkflowState) else Nil)
//      (if (includePreWorkflowState) List(model.config.preWorkflowState) else Nil) ::: currentModel.config.workflowStates ::: (if (includeReleased) List(currentModel.config.postWorkflowState) else Nil)

    val interestingStates2 = interestingStates.zipWithIndex.filter(sAndI => {
      if (!includePreWorkflowState && sAndI._2 == 0) false
      else true
    })

    interestingStates2.map(s => {
      val issuesForState = stateToIssues.getOrElse(s._2, Nil)
      val issues = issuesForState.map(i => s"\n ${
        i.render(model, hideStatus = true, hideBy = hideBy, hideTags = hideTags, highlight = changed.contains(i.ref), highlightAka = aka)
      }").mkString
      if (issuesForState.isEmpty && compressEmptyStates) None else Some(s"${s._1}: (${issuesForState.size})" + issues + "\n")
    }).flatten
  }

  private def sieveByTag(tags: Seq[Tag], issues: Seq[Issue], currentModel: Model, aka: String, hideStatus: Boolean, hideBy: Boolean, hideTags: Boolean, hideId: Boolean, hideCount: Boolean) = {
    case class TagAndIssues(tag: String, issues: Seq[Issue])
//    println(tags.mkString(", "))
    var remainingIssues = issues
    val r = tags.map(t => {
      val issuesForTag = remainingIssues.filter(_.tags.contains(t.name))
//      println(s"\n$t: $issuesForTag")
      remainingIssues = remainingIssues.diff(issuesForTag)
//      renderTagAndIssues(t.name, issuesForTag)
      TagAndIssues(t.name, SortByStatus(issuesForTag.map(i => i.copy(tags = i.tags.-(t.name))), currentModel))
    }) ++ Seq(TagAndIssues("?", SortByStatus(remainingIssues, currentModel)))
    r.filterNot(_.issues.isEmpty)/*.sortBy(_.issues.size)*/.map(tai =>
      renderTagAndIssues(currentModel, tai.tag, tai.issues, aka, hideStatus, hideBy, hideTags, hideId, hideCount)
    )
  }

  private def renderTagAndIssues(model: Model, tag: String, issuesForTag: Seq[Issue], aka: String, hideStatus: Boolean, hideBy: Boolean, hideTags: Boolean, hideId: Boolean, hideCount: Boolean): String = {
    val issues = issuesForTag.map(i => s"\n ${
      i.render(model, hideStatus = hideStatus, hideBy = hideBy, hideTags = hideTags, hideId = hideId, highlightAka = Some(aka))
    }").mkString
    s"$tag: ${if (hideCount) "" else s"(${issuesForTag.size})"}" + issues + "\n"
//    s"$tag: (${issuesForTag.size})" + issues + "\n"
  }

  private def sortedByPopularity(all: Seq[Tag]) = all.sortBy(t => (-t.count, t.name))

  private def sortedByImportance(all: Seq[Tag], blessedTags: List[String]) = {
    val blessed = blessedTags.flatMap(bt => all.find(_.name == bt))
    val remainder = all.diff(blessed).sortBy(_.name)
    blessed ++ remainder
  }
}
