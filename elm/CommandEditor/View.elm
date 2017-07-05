module CommandEditor.View exposing (view)

import Html exposing (..)
import Html.Events exposing (..)
import Html.Attributes exposing (..)
import CommandEditor.Model as Model exposing (..)
import CommandEditor.Msg as Msg exposing (..)
import String
import Dict
import Json.Encode as JsonEncode
import Date
import Date.Format as DateFormat
import Json.Decode as JsonDecode


(=>) =
    (,)


view : Model -> Html Msg
view model =
    div [ class "row" ]
        [ div [ class "col-md-12" ]
            [ div [ style [ ( "margin-top", "3px" ) ] ] [ text (Maybe.withDefault "" model.error) ]
            , agentView model
            ]
        ]


agentView : Model -> Html Msg
agentView model =
    div []
        [ --      div [ class "form-inline" ] [
          --        div [ style [ ( "margin" => "7px" ) ] ]
          --          [
          --          div [ class ("form-group"), style [ "padding-right" => "3px" ] ]
          --            [
          commandEditor model.command False
          --,  span [ class "glyphicon glyphicon-ok form-control-feedback", (property "aria-hidden" (JsonEncode.string "true")) ] [ ]
          --            ]
          --, div [ class "form-group" ] [ runButton (False) ]
          --          ]
          --      ]
        ]


commandEditor : String -> Bool -> Html Msg
commandEditor v disable =
    input
        [ type' "text"
        , placeholder "Command"
        , class "form-control input-sm"
        , onInput (\v -> (CommandChanged v))
        , onEnter RunCommand
        , disabled disable
        , value v
        ]
        []



--TIP: borrowed from https://github.com/evancz/elm-todomvc/blob/master/Todo.elm


onEnter : Msg -> Attribute Msg
onEnter msg =
    let
        tagger code =
            if code == 13 then
                msg
            else
                NoOp
    in
        on "keydown" (JsonDecode.map tagger keyCode)


runButton : Bool -> Html Msg
runButton disable =
    button
        [ class "btn btn-link"
        , style [ "padding" => "0px", "margin" => "0px" ]
        , onClick RunCommand
        , title "Run Command"
        , disabled disable
        ]
        [ i [ class "fa fa-plus-circle fa-2x" ] []
        ]
