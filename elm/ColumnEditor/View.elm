module ColumnEditor.View exposing (view)

import Html exposing (..)
import Html.Events exposing (..)
import Html.Attributes exposing (..)
import ColumnEditor.Model as Model exposing (..)
import ColumnEditor.Util exposing (..)
import ColumnEditor.Msg as Msg exposing (..)
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
  userNameEditor "" False


userNameEditor : String -> Bool -> Html Msg
userNameEditor v disable =
  input [ type' "text"
        , placeholder "Username"
        , class "form-control input-sm"
--        , onInput (\v -> (UserNameChanged v))
        , disabled disable
        , value v
        ] []


editButton : Html Msg
editButton =
  simpleIconButton "Edit Columns" "fa-th-list" EditColumns


saveButton : Html Msg
saveButton =
  simpleIconButton "Save" "fa-check" SaveChanges


cancelButton : Html Msg
cancelButton =
  simpleIconButton "Cancel" "fa-times" CancelChanges


simpleIconButton : String -> String -> Msg -> Html Msg
simpleIconButton name icon msg =
  button
    [ class "btn btn-link borderlessFocus", style [ "padding" => "0px", "margin" => "0px", "margin-left" => "3px" ]
    , onClick msg
    , title name
    ]
    [ span [ style [ "vertical-align" => "middle" ] ] [ i [ class ("fa " ++ icon) ] [] ] ]


editColumnsView : AgentModel -> Model -> Html Msg
editColumnsView agentModel model =
  let
    included = List.filter (\t -> t.selected) model.editedColumns
    excluded = List.filter (\t -> not t.selected) model.editedColumns
  in
    div [] [
      div [ ] [ saveButton, cancelButton ]
    , div [ class "round-corners", style [ "padding-top" => "2px", "padding-bottom" => "5px", "margin" => "0px", "margin-bottom" => "3px" ] ]
      [ span [ style [ "padding-right" => "3px" , "padding-top" => "0px" ] ] [ span [ class "label label-default", style [ "font-size" => "xx-small" ] ] [ text "Included" ] ]
      , span [] (List.map (\t -> columnView t included True) included)
      , div [] [ hr [ style [ "margin" => "3px 0px 3px 0px" ] ] [ ] ]
      , span [ style [ "padding-right" => "3px" , "padding-top" => "0px" ] ] [ span [ class "label label-default", style [ "font-size" => "xx-small" ] ] [ text "Excluded" ] ]
      , span [] (List.map (\t -> columnView t excluded False) excluded)
      ]
    ]


moveLeftButton : Maybe Int -> Column -> List Column -> Html Msg
moveLeftButton columnPosition column columns =
    button
      [ class "btn btn-link borderlessFocus", style [ "padding" => "0px", "margin" => "0px" ]
      , onClick (MoveLeft column.name)
      , title "Move Left"
      --TODO: use Maybe.map or Maybe.withDefault
      , disabled (case columnPosition of
        Nothing -> True
        Just index -> (index == 0))
      ]
      [ span [ style [ "vertical-align" => "middle" ] ] [ i [ class "fa fa-arrow-left" ] [] ] ]


toggleButton : Column -> Html Msg
toggleButton column =
    let options = { preventDefault = True, stopPropagation = True }
    in button
      [ class "btn btn-link borderlessFocus", style [ "padding" => "0px", "margin" => "0px" ]
      , onClick (ToggleSelected column.name)
      , title (if column.selected then "Exclude" else "Include")
      , disabled column.system
      ]
      [ span [ class "label label-primary", style [ "font-size" => "xx-small" ] ] [ text column.name ] ]


moveRightButton : Maybe Int -> Column -> List Column -> Html Msg
moveRightButton columnPosition column columns =
   button
     [ class "btn btn-link borderlessFocus", style [ "padding" => "0px", "margin" => "0px" ]
     , onClick (MoveRight column.name)
     , title "Move Right"
     --TODO: use Maybe.map or Maybe.withDefault
     , disabled (case columnPosition of
       Nothing -> True
       Just index -> (index + 1 == (List.length columns)))
     ]
     [ span [ style [ "vertical-align" => "middle" ] ] [ i [ class "fa fa-arrow-right" ] [] ] ]


columnView : Column -> List Column -> Bool -> Html Msg
columnView column columns showMoveButtons =
    let
     options = { preventDefault = True, stopPropagation = False }
     columnPosition = indexOf column.name (List.map (\t -> t.name) columns)
    in
    span [ {--class "round-corners",--} style [("margin" => "0px"), ("padding" => "0px"), ("margin-right" => "3px"), ("margin-bottom" => "0px"), ("display" => "inline-block")] ] [
      span [ style [("text-align" => "center")] ]
      [ if showMoveButtons then (moveLeftButton columnPosition column columns) else text ""
      , toggleButton column
      , if showMoveButtons then (moveRightButton columnPosition column columns) else text ""
      ]
    ]


spinner : Html Msg
spinner =
  div [ class "text-center", style [ "font-size" => "x-small" ] ] [ i [ class ("fa fa-cog fa-5x fa-spin")] [] ]
