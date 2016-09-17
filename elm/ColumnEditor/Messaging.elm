module ColumnEditor.Messaging exposing (..)


import ColumnEditor.Codec exposing (..)
import ColumnEditor.Msg as Msg exposing (..)
import Belch exposing (..)


portMessageToMsg : PortMessage -> Msg
portMessageToMsg message =
  case message.typeName of
    "LoadAgentModel" ->
        case decodeAgentModel message.payload of
          Ok value -> Load value
          Err error -> Error error

    x -> Error ("Received unknown message: " ++ toString message)
