port module ColumnEditor.Port exposing (..)


import Belch exposing (..)


port columnEditorAgentFromLift : (PortMessage -> msg) -> Sub msg


port columnEditorAgentToLift : PortMessage -> Cmd msg
