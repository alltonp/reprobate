module ConfigEditor.View exposing (view)

import Html exposing (..)
import Html.Events exposing (..)
import Html.Attributes exposing (..)
import ConfigEditor.Model as Model exposing (..)
import ConfigEditor.Msg as Msg exposing (..)
import String
import Dict
import Json.Encode as JsonEncode
import Date
import Date.Format as DateFormat
import Http
import Json.Decode as JsonDecode

(=>) = (,)


view : Model -> Html Msg
view model =
  div [ class "row" ]
    [ div [ class "col-md-12" ]
        [ div [ style [("margin-top", "3px")] ] [ text (Maybe.withDefault "" model.error) ]
        , agentView model
        ]
    ]


agentView : Model -> Html Msg
agentView model =
    div [] [ configEditor (model.command) False
           , div [ class "form-group" ] [ saveButton, cancelButton ]
           ]


configEditor : String -> Bool -> Html Msg
configEditor v disable =
  textarea [ class "form-control input-sm"
        , onInput (\v -> (CommandChanged v))
        , disabled disable
        , value v
        , rows 30
        ] []



saveButton : Html Msg
saveButton =
  button
      [ class "btn btn-link", style [ "padding" => "0px", "margin" => "0px" ]
      , onClick RunCommand
      , title "Save"
      ]
      [ i [ class "fa fa-check fa-2x" ] [] ]


cancelButton : Html Msg
cancelButton =
  button
      [ class "btn btn-link", style [ "padding" => "0px", "margin" => "0px" ]
      , onClick CancelCommand
      , title "Cancel"
      ]
      [ i [ class "fa fa-times fa-2x" ] [] ]
