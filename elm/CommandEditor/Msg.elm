module CommandEditor.Msg exposing (..)

import CommandEditor.Model exposing (..)


type Msg
    = Error (String)
    | Load (AgentModel)
    | CommandChanged (String)
    | RunCommand
    | NoOp
