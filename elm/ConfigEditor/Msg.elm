module ConfigEditor.Msg exposing (..)


import ConfigEditor.Model exposing (..)


type Msg
    = Error (String)
    | Load (AgentModel)

    | CommandChanged (String)
    | RunCommand
    | CancelCommand
    | NoOp
