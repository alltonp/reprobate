package app.restlike.gtd

import app.ServiceFactory.{systemClock, dateFormats}
import org.joda.time.LocalDate

//TODO: add issue
object Presentation {
  implicit def dateTimeOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)

  def basedOnUpdateContext(model: Model, changed: Seq[String], found: Option[Thing]) = {
//    val thingsToShow = if (model.collectedNeedProcessing) model.things.filter(_.date.isEmpty) else model.things
//    val thingsByDate = model.things.groupBy(_.date)

    val summary = model.allThingsIncludingDone.groupBy(_.inferredState(Some(model))).map{
      case (k, vs) => (k, vs.size)
    }

    //TODO: split these out ...
    found.fold(List.empty[String])(f => Messages.successfulUpdate(s"${f.render(model)}") ::: List("")) :::
      List(summary.map{case (k, c) => s"$k ($c)" }.toList.sorted.mkString(", ")) ::: List("") :::
      groupByStatus(model, compressEmptyStates = false, includeReleased = false, hideNextIfUnprocessed = true, hideBy = false, hideTags = false, model.things, model, changed)

//    model.things.sortBy(_.date).map(t => t.render(model, hideStatus = true, highlight = changed.contains(t.ref))).mkString("\n") :: Nil
  }

//  def release(model: Model, release: Release, highlightAka: Option[String]) = {
//    val r = release.issues.map(i => s"\n  ${i.render(model, hideStatus = true, highlightAka = highlightAka)}").mkString
//    s"${release.tag}: (${release.issues.size})${release.when.fold("")(" - " + dateFormats().today(_))}" + r + "\n" :: Nil
//  }

//  def issuesForUser(model: Model, aka: String, issues: Seq[Thing]) = {
//    val r = issues.map(i => s"\n  ${i.render(model, hideBy = true)}").mkString
//    s"${aka}: (${issues.size})" + r + "\n"
//  }

  def tags(all: Seq[Tag]) = sortedByPopularity(all).map(t => s"${t.name} (${t.count})").mkString(", ") :: Nil

  //TODO: we should include the released on the board too
  //TODO: render or remove tag
//  def tagDetail(tag: String, issues: Seq[Thing], currentModel: Model) = {
//    groupByStatus(currentModel, compressEmptyStates = true, includeReleased = true, includeBacklog = true, hideBy = true, hideTags = true, issues, currentModel, Nil, None)
//  }

  //TODO: render or remove release
  //TODO: we should show the release name if its a release ...
//  def pointyHairedManagerView(release: String, issues: Seq[Thing], blessedTags: List[String], currentModel: Model, sanitise: Boolean, aka: String) = {
//    val tagNames = issues.flatMap(_.tags).distinct
//    val tags = currentModel.tags.filter(t => tagNames.contains(t.name))
//    sieveByTag(sortedByImportance(tags, blessedTags), issues, currentModel, sanitise, aka)
//  }

  //TODO: introduce a DisplayOptions()
  //TODO: this is getting well shonky
  //TODO: this should show a nice "there is nothing to see" if that is the case
  private def groupByStatus(model: Model, compressEmptyStates: Boolean, includeReleased: Boolean, hideNextIfUnprocessed: Boolean, hideBy: Boolean, hideTags: Boolean, issues: Seq[Thing], currentModel: Model,
                            changed: Seq[String]) = {
    val stateToIssues = issues.groupBy(_.inferredState(Some(model)))

    val interestingStates = if (hideNextIfUnprocessed && stateToIssues.contains("collected")) List("collected")
                            else if (hideNextIfUnprocessed && stateToIssues.contains("next")) List("next")
                            else stateToIssues.keys.toList ::: (if (includeReleased) List("done") else Nil)

//    println(interestingStates)

    interestingStates.flatMap(s => {
      val issuesForState = stateToIssues.getOrElse(s, Nil).sortBy(_.ref.toLong).sortBy(_.inferredState(Some(model)))

//      issuesForState.sortBy(_.inferredState(Some(model)))
//      println(issuesForState)

      val issues = issuesForState.map(i => s"\n  ${
        i.render(model, hideStatus = true, hideBy = hideBy, hideTags = hideTags, highlight = changed.contains(i.ref))
      }").mkString
      if (issuesForState.isEmpty && compressEmptyStates) None else Some(s"$s: (${issuesForState.size})" + issues + "\n")
    })
  }

//  private def sieveByTag(tags: Seq[Tag], issues: Seq[Thing], currentModel: Model, sanitise: Boolean, aka: String) = {
//    case class TagAndIssues(tag: String, issues: Seq[Thing])
////    println(tags.mkString(", "))
//    var remainingIssues = issues
//    val r = tags.map(t => {
//      val issuesForTag = remainingIssues.filter(_.tags.contains(t.name))
////      println(s"\n$t: $issuesForTag")
//      remainingIssues = remainingIssues.diff(issuesForTag)
////      renderTagAndIssues(t.name, issuesForTag)
//      TagAndIssues(t.name, SortByStatus(issuesForTag.map(i => i.copy(tags = i.tags.-(t.name))), currentModel))
//    }) ++ Seq(TagAndIssues("?", SortByStatus(remainingIssues, currentModel)))
//    r.filterNot(_.issues.isEmpty)/*.sortBy(_.issues.size)*/.map(tai =>
//      renderTagAndIssues(currentModel, sanitise, tai.tag, tai.issues, aka)
//    )
//  }

  private def renderTagAndIssues(model: Model, sanitise: Boolean, tag: String, issuesForTag: Seq[Thing], aka: String): String = {
    val issues = issuesForTag.map(i => s"\n  ${
      i.render(model, hideStatus = sanitise, hideBy = sanitise, hideTags = sanitise, hideId = sanitise, highlightAka = Some(aka))
    }").mkString
    s"$tag: ${if (sanitise) "" else s"(${issuesForTag.size})"}" + issues + "\n"
  }

  private def sortedByPopularity(all: Seq[Tag]) = all.sortBy(t => (-t.count, t.name))
  private def sortedByImportance(all: Seq[Tag], blessedTags: List[String]) = {
    val blessed = blessedTags.flatMap(bt => all.find(_.name == bt))
    val remainder = all.diff(blessed).sortBy(_.name)
    blessed ++ remainder
  }
}
