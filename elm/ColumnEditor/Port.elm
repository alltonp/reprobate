port module ColumnEditor.Port exposing (..)


import Belch exposing (..)


--TODO: ultimately this shoukld all be commandEditor
port columnEditorAgentFromLift : (PortMessage -> msg) -> Sub msg


port columnEditorAgentToLift : PortMessage -> Cmd msg
