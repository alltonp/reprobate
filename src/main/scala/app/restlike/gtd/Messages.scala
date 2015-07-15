package app.restlike.gtd

object Messages {
  import app.restlike.common.Colours._

  val eh = "eh?"

  def notFound(ref: String) = problem(s"issue not found: $ref")

  def descriptionEmpty = problem(s"description is empty")

  def duplicateIssue(ref: String) = problem(s"issue already exists: $ref")

  def problem(message: String) = List(red(s"problem: ") + customGrey(message))

  def success(what: String) = List(what)

  def successfulUpdate(what: String) = List(customGreen(s"=> $what"))

  //TODO: use appName everywhere ...
  //TODO: how about advance and retreat instead of forward/back or push/pull or left/right
  def help(who: String) = List(
    s"hello ${who}, welcome to gtd - getting things done © 2015 spabloshi ltd",
    "",
    "things:",
    "  - create                         ⇒ 'gtd + [the description] {: tag1 tag2 tagX}'",
    "  - update                         ⇒ 'gtd [ref] ='",
    "  - delete                         ⇒ 'gtd [ref] -'",
    "  - do                             ⇒ 'gtd [ref] !'",
    "  - undo                           ⇒ 'gtd [ref] .'",
    "  - next                           ⇒ 'gtd [ref] /'",
//    "  - own                            ⇒ 'rim [ref] @'",
//    "  - disown                         ⇒ 'rim [ref] @-'",
//    "  - assign                         ⇒ 'rim [ref] @= [aka]'",
    "  - tag                            ⇒ 'gtd [ref] : [tag1] {tag2} {tagX}'",
    "  - detag                          ⇒ 'gtd [ref] :- [tag1] {tag2} {tagX}'",
    "",
    //is this 'reports'
    "show:",
    "  - all                            ⇒ 'gtd'",
//    "  - backlog                        ⇒ 'rim .'",
//    "  - phb board summary              ⇒ 'rim ^{_} {tag1 tag2 tagX}'                      ⇒ i.e. in tag priority order",
//    "  - phb release summary            ⇒ 'rim [release] ^{_} {tag1 tag2 tagX}'",
//    "  - releases                       ⇒ 'rim ±'",
//    "  - release notes                  ⇒ 'rim note [release]'",
//    "  - tag usage                      ⇒ 'rim :'",
//  TODO: should probably support sanitise
//    "  - all for tag                    ⇒ 'rim : [tag]'",
//    "  - untagged                       ⇒ 'rim :-'",
//    "  - who is doing what              ⇒ 'rim @'",
    "  - help                           ⇒ 'gtd help'",
//    "",
//    "tags:",
//    "  - migrate                        ⇒ 'rim [oldtag] := [newtag]'",
  //TODO: consider having the user type yes on the end (previewing it first)
//    "  - kill all usages                ⇒ 'rim [killtag] :--'",
    "",
    "search:",
    "  - all things                     ⇒ 'gtd ? {term1 term2 termX}'                      ⇒ e.g. 'gtd ? :tag ^date text'",
//    "",
//    "releases:",
//    "  - create                         ⇒ 'rim ± [label]'",
//    "",
//    "config:",
//    TODO: aka should work like tags i.e. = and show
//    "  - set aka                        ⇒ 'rim aka [initials]'",
//    "  - show tag priority              ⇒ 'rim tags'",
//    "  - set tag priority               ⇒ 'rim tags = {tag1 tag2 tagX}'                    ⇒ i.e. in tag priority order",
//    "",
//    "expert mode:",
//    "  - create, forward and tag        ⇒ 'rim +/ description {: tag1 tag2 tagX}'",
//    "  - create, forward many and tag   ⇒ 'rim +// description {: tag1 tag2 tagX}'",
//    "  - create, end and tag            ⇒ 'rim +! description {: tag1 tag2 tagX}'",
    "",
//    "where: [arg] = mandatory, {arg} = optional, {_} = sanitise, phb = pointy haired boss",
    "where: [arg] = mandatory, {arg} = optional",
    ""
  )
}
