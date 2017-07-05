module ConfigEditorAgent exposing (..)

import Html exposing (..)
import ConfigEditor.Model as Model exposing (..)
import ConfigEditor.Msg as Msg exposing (..)
import ConfigEditor.Update as ConfigEditorUpdate exposing (..)
import ConfigEditor.View as ConfigEditorView exposing (..)
import ConfigEditor.Messaging exposing (..)
import ConfigEditor.Port exposing (..)


update : Msg -> Model -> ( Model, Cmd Msg )
update action model =
    ConfigEditorUpdate.update action model


view : Model -> Html Msg
view model =
    ConfigEditorView.view model


main : Program Never Model Msg
main =
    program
        { init = ConfigEditorUpdate.init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }


subscriptions : Model -> Sub Msg
subscriptions model =
    configEditorAgentFromLift ConfigEditor.Messaging.portMessageToMsg
