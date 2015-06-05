package app.restlike.rim

object Messages {
  import app.restlike.common.Colours._

  val eh = "eh?"

  def notAuthorised(who: String) = List(red(s"easy ${who}, please set your initials first: ") + "'rim aka pa'")

  def notFound(ref: String) = problem(s"issue not found: $ref")

  def descriptionEmpty = problem(s"description is empty")

  def duplicateIssue(ref: String) = problem(s"issue already exists: $ref")

  def problem(message: String) = List(red(s"problem: ") + message)

  def success(what: String) = List(what)

  def successfulUpdate(what: String) = List(orange(s"=> $what"))

  //TODO: use appName everywhere ...
  //TODO: how about advance and retreat instead of forward/back or push/pull or left/right
  def help(who: String) = List(
    s"hello ${who}, welcome to rim - rudimentary issue management © 2015 spabloshi ltd",
    "",
    "issues:",
    "  - create                         ⇒ 'rim + [the description] {: tag1 tag2 tagX}'",
    "  - update                         ⇒ 'rim [ref] ='",
    "  - delete                         ⇒ 'rim [ref] -'",
    "  - own                            ⇒ 'rim [ref] @'",
    "  - disown                         ⇒ 'rim [ref] @-'",
    "  - assign                         ⇒ 'rim [ref] @= [aka]'",
    "  - tag                            ⇒ 'rim [ref] : [tag1] {tag2} {tagX}'",
    "  - detag                          ⇒ 'rim [ref] :- [tag1] {tag2} {tagX}'",
    "  - move forward                   ⇒ 'rim [ref] /'",
    //    "  - move forward many              ⇒ 'rim [ref] //'",
    "  - move to end                    ⇒ 'rim [ref] /!'",
    "  - move backward                  ⇒ 'rim [ref] .'",
    //    "  - move backward many         ⇒ 'rim [ref] ..'",
    "  - return to backlog              ⇒ 'rim [ref] .!'",
    "",
    "show:",
    "  - board                          ⇒ 'rim'",
    "  - backlog                        ⇒ 'rim .'",
    "  - phb summary                    ⇒ 'rim ^'",
    "  - releases                       ⇒ 'rim releases'",
//    "  - release notes                  ⇒ 'rim note [release]'",
    "  - tags                           ⇒ 'rim :'",
    "  - all for tag                    ⇒ 'rim : [tag]'",
    "  - untagged                       ⇒ 'rim :-'",
    "  - who is doing what              ⇒ 'rim @'",
    "  - help                           ⇒ 'rim help'",
    "",
    "tags:",
    "  - migrate                        ⇒ 'rim [oldtag] := [newtag]'",
    "",
    "search:",
    "  - all issues                     ⇒ 'rim ? {term1 term2 termX}'                      ⇒ e.g. 'rim ? :tag ^status @aka text'",
    "",
    //TODO: this will ultimately be 'config' once we also have 'releases'
    "other:",
    "  - set aka                        ⇒ 'rim aka [initials]'",
    "  - create release                 ⇒ 'rim release [label]'",
    "",
    "expert mode:",
    "  - create, forward and tag        ⇒ 'rim +/ description {: tag1 tag2 tagX}'",
    "  - create, forward many and tag   ⇒ 'rim +// description {: tag1 tag2 tagX}'",
    "  - create, end and tag            ⇒ 'rim +! description {: tag1 tag2 tagX}'",
    "",
    "where: [arg] = mandatory, {arg} = optional",
    ""
  )
}
