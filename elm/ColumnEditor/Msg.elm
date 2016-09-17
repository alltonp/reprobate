module ColumnEditor.Msg exposing (..)


import ColumnEditor.Model exposing (..)


type Msg
    = Error (String)
    | Load (AgentModel)
    | EditColumns
    | SaveChanges
    | CancelChanges
    | ToggleSelected (String)
    | MoveLeft (String)
    | MoveRight (String)
