module ColumnEditor.Msg exposing (..)


import ColumnEditor.Model exposing (..)


type Msg
    = Error (String)
    | Load (AgentModel)

    | CommandChanged (String)
    | RunCommand
