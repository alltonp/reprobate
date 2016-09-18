module ColumnEditor.Update exposing (..)


import ColumnEditor.Model exposing (..)
import ColumnEditor.Util exposing (..)
import ColumnEditor.Msg as Msg exposing (..)
import ColumnEditor.Port exposing (..)
import ColumnEditor.Codec exposing (..)
import Belch exposing (..)
import Dict
import String
import Validate exposing (..)
import Regex


init : (Model, Cmd Msg)
init =
    (initialModel, Cmd.none)


--TODO: highlight the last change, so we can see where it went e.g. grey and dark-grey etc
--TODO: consider light wobbling, a-la ios
--TODO: support reset

update : Msg -> Model -> (Model, Cmd Msg)
update action model =
  case action of
    Error message -> { model | error = Just message } ! []

    Load agentModel -> { model | agentModel = Just agentModel, editedColumns = agentModel.columns } ! []

    EditColumns -> { model | editing = True } ! []

    SaveChanges ->
      let model' = { model | editing = False }
      in (model', columnEditorAgentToLift (PortMessage "SaveColumns" "") )

    CancelChanges ->
      let
        editedColumns' = (Maybe.map (\m -> m.columns) model.agentModel) |> Maybe.withDefault []
        model' = { model | editing = False, editedColumns = editedColumns' }
      in (model', columnEditorAgentToLift (columnsChanged editedColumns') )

    ToggleSelected name ->
      let
        editedColumns' = List.map (\t -> if t.name == name then { t | selected = not t.selected } else t ) model.editedColumns
        model' = { model | editedColumns = editedColumns' }
      in (model', columnEditorAgentToLift (columnsChanged editedColumns') )

    CommandChanged command -> { model | command = command } ! []

    RunCommand ->
        let model' = { model | command = "" }
        in (model', columnEditorAgentToLift (runCommand model.command))



columnsChanged : List Column -> PortMessage
columnsChanged editedColumns =
    PortMessage "ColumnsChanged" (columnsEncoder editedColumns)


runCommand : String -> PortMessage
runCommand command =
    PortMessage "RunCommand" (command)