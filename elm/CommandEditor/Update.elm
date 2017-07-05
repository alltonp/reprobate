module CommandEditor.Update exposing (..)

import CommandEditor.Model exposing (..)
import CommandEditor.Msg as Msg exposing (..)
import CommandEditor.Port exposing (..)
import CommandEditor.Codec exposing (..)
import Belch exposing (..)
import Dict
import String


init : ( Model, Cmd Msg )
init =
    ( initialModel, Cmd.none )


update : Msg -> Model -> ( Model, Cmd Msg )
update action model =
    case action of
        Error message ->
            { model | error = Just message } ! []

        Load agentModel ->
            { model | agentModel = Just agentModel } ! []

        CommandChanged command ->
            { model | command = command } ! []

        RunCommand ->
            let
                model_ =
                    { model | command = "" }
            in
                ( model_, commandEditorAgentToLift (runCommand model.command) )

        NoOp ->
            model ! []


runCommand : String -> PortMessage
runCommand command =
    PortMessage "RunCommand" (command)
