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

    MoveLeft name ->
      let
        current = indexOf name (List.map (\t -> t.name) model.editedColumns)
        editedColumns' = case current of
          Nothing -> model.editedColumns
          Just index ->
            let
              beforeAndThis = List.take (index + 1) model.editedColumns
              after = List.drop (index + 1) model.editedColumns
              this = List.drop index beforeAndThis
              oldBefore = (List.take index beforeAndThis)
              newBefore = (List.take (index - 1) beforeAndThis)
              --d0 = Debug.log "oldbefore" (toString oldBefore)
              --d1 = Debug.log "newbefore" (toString newBefore)
              --d2 = Debug.log "this" (toString this)
              --d3 = Debug.log "after" (toString after)
              updated = newBefore ++ this ++ (List.drop (index - 1) oldBefore) ++ after
              --u = Debug.log "updated" (toString updated)
            in
              updated
        model' = { model | editedColumns = editedColumns' }
      in (model', columnEditorAgentToLift (columnsChanged editedColumns') )

    MoveRight name ->
      let
        current = indexOf name (List.map (\t -> t.name) model.editedColumns)
        editedColumns' = case current of
          Nothing -> model.editedColumns
          Just index ->
            let
              before = List.take index model.editedColumns
              thisAndAfter = List.drop (index) model.editedColumns
              this = List.take 1 thisAndAfter
              oldAfter = (List.drop 1 thisAndAfter)
              newAfter = (List.drop 2 thisAndAfter)
              --d0 = Debug.log "oldAfter" (toString oldAfter)
              --d1 = Debug.log "newAfter" (toString newAfter)
              --d2 = Debug.log "this" (toString this)
              --d3 = Debug.log "before" (toString before)
              updated = before ++ (List.take 1 oldAfter) ++ this ++ newAfter
              --u = Debug.log "updated" (toString updated)
            in
              updated
        model' = { model | editedColumns = editedColumns' }
      in (model', columnEditorAgentToLift (columnsChanged editedColumns') )

    CommandChanged command -> { model | command = command } ! []



columnsChanged : List Column -> PortMessage
columnsChanged editedColumns =
    PortMessage "ColumnsChanged" (columnsEncoder editedColumns)