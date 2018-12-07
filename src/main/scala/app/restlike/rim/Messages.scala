package app.restlike.rim

object Messages {
  import app.restlike.common.Colours._

  val eh = "eh?"

  def notAuthorised(who: String) = List(customRed(s"easy ${who}, please set your initials first: ") + "'rim aka pa'")

  def notFound(ref: String) = problem(s"issue not found: $ref")

  def descriptionEmpty = problem(s"description is empty")

  def duplicateIssue(ref: String) = problem(s"issue already exists: $ref")

  def problem(message: String) = List(customRed(s"problem: ") + customGrey(message))

  def success(what: String) = List(what)

  def successfulUpdate(what: String) = List(customGreen(s"= $what"))

  //TODO: use appName everywhere ...
  //TODO: how about advance and retreat instead of forward/back or push/pull or left/right
  def help(who: String) = List(
    s"hello ${who}, welcome to rim - rudimentary issue management © 2015-2017 spabloshi ltd",
    "",
    "issues:",
    "  - create                         ⇒ 'rim + [the name] {: tag1 tag2 tagX}'     ⇒ e.g. 'rim + description : tag1 tag2'",
    "  - update                         ⇒ 'rim [ref] ='",
    "  - delete                         ⇒ 'rim [ref] -'",
    "  - own                            ⇒ 'rim [ref] @'",
    "  - disown                         ⇒ 'rim [ref] @-'",
    "  - assign                         ⇒ 'rim [ref] @= [aka]'",
    "  - move forward                   ⇒ 'rim [ref] /'",
    //    "  - move forward many              ⇒ 'rim [ref] //'",
    "  - move to end                    ⇒ 'rim [ref] /!'",
    "  - move backward                  ⇒ 'rim [ref] .'",
    //    "  - move backward many         ⇒ 'rim [ref] ..'",
    //TODO: this should be dynamic off of preWorkflowState
    "  - move to backlog                ⇒ 'rim [ref] .!'",
    "  - make top priority              ⇒ 'rim [ref] _'",
    "  - prioritise under other issue   ⇒ 'rim [ref] _ [under ref]'",
    "  - block                          ⇒ 'rim [ref] % [reason]'",
    "  - unblock                        ⇒ 'rim [ref] %'",
    "  - tag                            ⇒ 'rim [ref] : [tag1] {tag2} {tagX}'",
    "  - detag                          ⇒ 'rim [ref] :- [tag1] {tag2} {tagX}'",
    "  - set values                     ⇒ 'rim [ref] [key1=value1] {key2=value2} {key3=value3}'",
    "  - unset values                   ⇒ 'rim [ref] [key1=-] {key2=-} {key3=-}'",
    "  - add comment                    ⇒ 'rim [ref] , the comment'",
    "  - history                        ⇒ 'rim [ref]'",
    "",
    //is this 'reports'
    "show:",
    "  - board                          ⇒ 'rim'",
    //TODO: this should be dynamic off of preWorkflowState
    "  - backlog                        ⇒ 'rim .'",
    "  - phb board summary              ⇒ 'rim ^{_} {tag1 tag2 tagX}'                      ⇒ i.e. in tag priority order",
    "  - phb release summary            ⇒ 'rim [release] ^{_} {tag1 tag2 tagX}'",
    "  - releases                       ⇒ 'rim ±'",
//    "  - release notes                  ⇒ 'rim note [release]'",
    "  - tag usage                      ⇒ 'rim :'",
//  TODO: should probably support sanitise
    "  - all for tag                    ⇒ 'rim : [tag]'",
    "  - untagged                       ⇒ 'rim :-'",
    "  - who is doing what              ⇒ 'rim @'",
    "  - help                           ⇒ 'rim help'",
    "",
    "tags:",
    "  - migrate                        ⇒ 'rim [oldtag] := [newtag]'",
  //TODO: consider having the user type yes on the end (previewing it first)
    "  - kill all usages                ⇒ 'rim [killtag] :--'",
    "",
    "search:",
    "  - all issues                     ⇒ 'rim ? {term1 term2 termX}'                      ⇒ e.g. 'rim ? :tag ^status @aka text'",
    "",
    "releases:",
    "  - create                         ⇒ 'rim ± [label]'",
    "",
    "config:",
    //TODO: aka should work like tags i.e. = and show
    "  - set aka                        ⇒ 'rim aka [initials]'",
    "  - show tag priority              ⇒ 'rim tags'",
    "  - set tag priority               ⇒ 'rim tags = {tag1 tag2 tagX}'                    ⇒ i.e. in tag priority order",
    "  - set workflow                   ⇒ 'rim workflow = {state1 state2 stateX}'",
    "",
    "expert mode:",
    "  - create, forward and tag        ⇒ 'rim +/ description {: tag1 tag2 tagX}'",
    "  - create, forward many and tag   ⇒ 'rim +// description {: tag1 tag2 tagX}'",
    "  - create, end and tag            ⇒ 'rim +! description {: tag1 tag2 tagX}'",
    "",
    "where: [arg] = mandatory, {arg} = optional, {_} = sanitise, phb = pointy haired boss",
    ""
  )
}
