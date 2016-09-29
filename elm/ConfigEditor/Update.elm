module ConfigEditor.Update exposing (..)


import ConfigEditor.Model exposing (..)
import ConfigEditor.Msg as Msg exposing (..)
import ConfigEditor.Port exposing (..)
import ConfigEditor.Codec exposing (..)
import Belch exposing (..)
import Dict
import String


init : (Model, Cmd Msg)
init =
    (initialModel, Cmd.none)


update : Msg -> Model -> (Model, Cmd Msg)
update action model =
  case action of
    Error message -> { model | error = Just message } ! []

    Load agentModel -> { model | agentModel = Just agentModel, command = agentModel.config } ! []

    CommandChanged command -> { model | command = command } ! []

    RunCommand ->
        let model' = { model | command = "" }
        in (model', configEditorAgentToLift (runCommand model.command))

    NoOp -> model ! []


runCommand : String -> PortMessage
runCommand command =
    PortMessage "RunCommand" (command)