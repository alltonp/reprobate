module CommandEditorAgent exposing (..)

import Html exposing (..)
import CommandEditor.Model as Model exposing (..)
import CommandEditor.Msg as Msg exposing (..)
import CommandEditor.Update as CommandEditorUpdate exposing (..)
import CommandEditor.View as CommandEditorView exposing (..)
import CommandEditor.Messaging exposing (..)
import CommandEditor.Port exposing (..)


update : Msg -> Model -> ( Model, Cmd Msg )
update action model =
    CommandEditorUpdate.update action model


view : Model -> Html Msg
view model =
    CommandEditorView.view model


main : Program Never Model Msg
main =
    program
        { init = CommandEditorUpdate.init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }

subscriptions : Model -> Sub Msg
subscriptions model =
    commandEditorAgentFromLift CommandEditor.Messaging.portMessageToMsg
