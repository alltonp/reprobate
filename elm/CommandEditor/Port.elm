port module CommandEditor.Port exposing (..)


import Belch exposing (..)


port commandEditorAgentFromLift : (PortMessage -> msg) -> Sub msg


port commandEditorAgentToLift : PortMessage -> Cmd msg
