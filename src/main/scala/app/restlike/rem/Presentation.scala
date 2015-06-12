package app.restlike.rem

//TODO: add issue
object Presentation {
//  def board(model: Model) = {
//    val stateToIssues = model.things.groupBy(_.status)
//    model.workflowStates.map(s => {
//      val issuesForState = stateToIssues.getOrElse(Some(s), Nil)
//      val issues = issuesForState.map(i => s"\n  ${i.render(hideStatus = true)}").mkString
//      s"$s: (${issuesForState.size})" + issues + "\n"
//    })
//  }

//  def release(release: Release) = {
//    val r = release.issues.map(i => s"\n  ${i.render(hideStatus = true)}").mkString
//    s"${release.tag}: (${release.issues.size})" + r + "\n"
//  }

//  def issuesForUser(aka: String, issues: List[Thing]) = {
//    val r = issues.map(i => s"\n  ${i.render(hideBy = true)}").mkString
//    s"${aka}: (${issues.size})" + r + "\n"
//  }

  def things(all: Seq[Thing]) = {
    all.sortBy(_.ref.toInt).reverseMap(i => i.render()).toList
  }

  def tags(all: Seq[Tag]) = {
    val sorted = all.sortBy(t => (-t.count, t.name)).map(t => s"${t.name} (${t.count})")
    ": " + sorted.mkString(", ") :: Nil
  }
}
