package app.restlike.rem

object Messages {
  import app.restlike.common.Colours._

  val eh = "eh?"

//  def notAuthorised(who: String) = List(red(s"easy ${who}, please set your initials first: ") + "'rem aka pa'")
  def notFound(ref: String) = problem(s"thing not found: $ref")
  def descriptionEmpty = problem(s"description is empty")
  def duplicateThing(ref: String) = problem(s"thing already exists: $ref")
  def problem(message: String) = List(customRed(s"problem: ") + message)

  //TODO: use appName everywhere
  //TODO: how about advance and retreat instead of forward/back or push/pull or left/right
  def help(who: String) = List(
    s"hello ${who}, welcome to rem - the thing rememberer © 2015-2017 spabloshi ltd",
    "",
    "things:",
    "  - create                         ⇒ 'rem + [key] = {value} {: tag1 tag2 tagX}'",
    "  - update value                   ⇒ 'rem [ref] _= {value}'",
    "  - update key                     ⇒ 'rem [ref] =_ {key}'",
    "  - delete                         ⇒ 'rem [ref] -'",
//    "  - own                            ⇒ 'rim [ref] @'",
//    "  - disown                         ⇒ 'rim [ref] @-'",
//    "  - assign                         ⇒ 'rim [ref] @= [aka]'",
//    "  - tag                            ⇒ 'rim [ref] : [tag1] {tag2} {tagX}'",
//    "  - detag                          ⇒ 'rim [ref] :- [tag1] {tag2} {tagX}'",
//  //TODO: pull out to be under 'tags' section?
//    "  - migrate tag                    ⇒ 'rim [oldtag] := [newtag]'",
//    "  - move forward                   ⇒ 'rim [ref] /'",
////    "  - move forward many              ⇒ 'rim [ref] //'",
//    "  - move to end                    ⇒ 'rim [ref] /!'",
//    "  - move backward                  ⇒ 'rim [ref] .'",
//    //    "  - move backward many         ⇒ 'rim [ref] ..'",
//    "  - return to backlog              ⇒ 'rim [ref] .!'",
    "",
    "show:",
    "  - all                            ⇒ 'rem'",
//    "  - backlog                        ⇒ 'rim .'",
//    "  - releases                       ⇒ 'rim releases'",
//    "  - tags                           ⇒ 'rim :'",
//    "  - who is doing what              ⇒ 'rim @'",
    "  - help                           ⇒ 'rem help'",
    "",
    "search:",
    "  - all things                     ⇒ 'rem ? {term1 term2 termX}'                      ⇒ e.g. 'rem ? :tag text'",
    "",
//    //TODO: this will ultimately be 'config' once we also have 'releases'
    "other:",
//    "  - set aka                        ⇒ 'rem aka [initials]'",
//    "  - create release                 ⇒ 'rim release [label]'",
//    "",
//    "expert mode:",
//    "  - create, forward and tag        ⇒ 'rim +/ description {: tag1 tag2 tagX}'",
//    "  - create, forward many and tag   ⇒ 'rim +// description {: tag1 tag2 tagX}'",
//    "  - create, end and tag            ⇒ 'rim +! description {: tag1 tag2 tagX}'",
    "",
    "where: [arg] = mandatory, {arg} = optional",
    ""
  )
}
