port module ConfigEditor.Port exposing (..)


import Belch exposing (..)


port configEditorAgentFromLift : (PortMessage -> msg) -> Sub msg


port configEditorAgentToLift : PortMessage -> Cmd msg
