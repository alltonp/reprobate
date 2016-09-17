module ColumnEditorAgent exposing (..)


import Html.App as Html
import Html exposing (..)
import ColumnEditor.Model as Model exposing (..)
import ColumnEditor.Msg as Msg exposing (..)
import ColumnEditor.Update as ColumnEditorUpdate exposing (..)
import ColumnEditor.View as ColumnEditorView exposing (..)
import ColumnEditor.Messaging exposing (..)
import ColumnEditor.Port exposing (..)


update : Msg -> Model -> (Model, Cmd Msg)
update action model =
    ColumnEditorUpdate.update action model


view : Model -> Html Msg
view model =
  ColumnEditorView.view model


main =
  Html.program
    { init = ColumnEditorUpdate.init, update = update, view = view, subscriptions = subscriptions }


subscriptions : Model -> Sub Msg
subscriptions model =
  columnEditorAgentFromLift ColumnEditor.Messaging.portMessageToMsg
