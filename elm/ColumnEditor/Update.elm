module ColumnEditor.Update exposing (..)


import ColumnEditor.Model exposing (..)
import ColumnEditor.Util exposing (..)
import ColumnEditor.Msg as Msg exposing (..)
import ColumnEditor.Port exposing (..)
import ColumnEditor.Codec exposing (..)
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

    Load agentModel -> { model | agentModel = Just agentModel, editedColumns = agentModel.columns } ! []

    CommandChanged command -> { model | command = command } ! []

    RunCommand ->
        let model' = { model | command = "" }
        in (model', columnEditorAgentToLift (runCommand model.command))


runCommand : String -> PortMessage
runCommand command =
    PortMessage "RunCommand" (command)