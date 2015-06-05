package app.restlike.rim

//NEXT:
//'rim [tag] :*' - toggle importance
//'rim ±' - might look quite similar to rim @, but be tag instead
//colourise the statuses
//consider and nice 4 blue for what youve just changed
//and then you traffic lights for status, light blue for
//think about limiting WIP
//ultimately rim : [tag] might want to be sorted by status and not broken down by status
//consider a colour blob next to the status (like a rag status), could be round or reverse video

//remove the old release note option
//might want modifier to rim ± to exclude things e.g. tags and id's (it's like you want to fix it and then produce it)

// ..... how about . instead of : for less interesting tags, or even :; (winky tags)

// ..... can/should we lose some the extra : on id's and labels within rim, make it a bit cleaner

//consider * for collapse non blessed tags

//consider collapse non * tags in phb view
//should we sort by importance and then size

///pointy head reporting
//(1) we have done a b c
//(2) next we are doing d e f

//show the board when:
//'rim =' and issue is on the board
//'rim @=' and issue is on the board
//'rim [ref] :' and issue is on the board

//in "rim :"
//consider highlighting tags that are only in released, might indicate dead ... but oy probably wouldnt delete


//collect stats on command usage

//feedback from team
//Meta tags? Or mark some tags as private or business
//clear screen each time? (see below)
//rim = should parse tags
//always show the incoming command > and the response =>
//if something changes the board, then show the board (too)

//TIP: http://www.chriswrites.com/how-to-type-common-symbols-and-special-characters-in-os-x/
//rim ± = option shift equals .. iteration/management summary .. or use pointy hat symbol ^

//release notes
//- no id, no by and grouped by business tag
//- should be a 'rim note [release]' or 'rim [release] note'

//when doing +/ etc .. show the both the created ref and the new board (or colorise what changed)

//tags:
//show tags by most recent etc (maybe)
//Franck: tag many: `rim ref1 ref2 … refN : foo bar baz`
//tags should be [a-z0-9\-]
//should 'rim [tag] :-' remove tag .. (dicey) .. should be to detag all issues with that tag

//operations to support on many:
//rim 1 2 N .
//rim 1 2 N :
//rim 1 2 N :-

//more view options:
//rim / - show begin
//rim // - show nth state
//rim ! - show end state

//releases:
//store when we released .. useful for really simple tracking

//colouring: (orange = updated, cyan = me, ? = context)
//colorise what changed .. (could be property specific)
//when doing 'rim @' consider highlight what exactly is being done

//gaps:
//properly support multiple / in /// and +///
//properly support multiple . in ...

//dates: (not yet)
//store when released (eek, data change - so make it an option)
//show how long things have been in certain states
//show stats about akas ... entries, last used etc (top 5)
//show how long since aka X updated rim

//query:
//rim . foo => should maybe search like ? does, but just for the backlog for foo ...
//or maybe not because 'and' might cover it ... although how do you search for no status

//???:
//help should have an 'issues' section for working with multiples on =, : etc
//when doing rim = ... - it's easy to forget the to not copy the tags, seems like tags should be processed (i.e. add)
//might be nice to have rim audit (or track) and see the last x items from the history
//rim @ should sort/breakdown by status, so you can easily what you are doing/have done
//how do we handle rim releases getting too long?

//audit stuff
//might be good to capture who added the issue
//might be good to capture who last updated the issue
//actually if we just store the updates by id then we will get that for free
//store only things that result in a change
//rim [ref] history

//FUTURE:
//- private rims
//- grim
//- spartan bubble ui
//- hosted

//SOMEDAY/MAYBE:
//split and merge {}

//think about:
//would be nice to have a symbol for release ... it could be: ±
//so then show = 'rim ±' or _ .. as in draw a line under it
//so then create = 'rim ± [name]'
//so then notes = 'rim [name] ±' ... need something to differentiate from adding
//so maybe: '_+ [name]' to add, '_' to show, '[name] _' for notes

//UI clearing
//show the command that was entered =>

//TODO: handle corrupted rim.json

//TODO: protect against empty value
//TODO: discover common keys and present them when updating
//TODO: be careful with aka .. they need to be unique
//TODO: on update, don't show self in list of others and don't show anything if others are empty
//TODO: make it possible to ask questions and force others to answer them
//TODO: colourise
//http://stackoverflow.com/questions/287871/print-in-terminal-with-colors-using-python?rq=1
//http://apple.stackexchange.com/questions/74777/echo-color-coding-stopped-working-in-mountain-lion
//http://unix.stackexchange.com/questions/43408/printing-colored-text-using-echo
//e.g. printf '%s \e[0;31m%s\e[0m %s\n' 'Some text' 'in color' 'no more color'
//  def red(value: String) = s"\e[1;31m $value \e[0m"

//TODO: (maybe) support curl
//#MESSAGE="(Foo) Deployed ${VERSION} to ${MACHINE_NAME}"
//#curl --connect-timeout 15 -H "Content-Type: application/json" -d "{\"message\":\"${MESSAGE}\"}" http://localhost:8765/broadcast
//#wget --timeout=15 --no-proxy -O- --post-data="{\"message\":\"${MESSAGE}\"}" --header=Content-Type:application/json "http://localhost:8765/broadcast"
