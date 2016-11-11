module CommandEditor.Messaging exposing (..)

import CommandEditor.Codec exposing (..)
import CommandEditor.Msg as Msg exposing (..)
import Belch exposing (..)


portMessageToMsg : PortMessage -> Msg
portMessageToMsg message =
    case message.typeName of
        "LoadAgentModel" ->
            case decodeAgentModel message.payload of
                Ok value ->
                    Load value

                Err error ->
                    Error error

        x ->
            Error ("Received unknown message: " ++ toString message)
