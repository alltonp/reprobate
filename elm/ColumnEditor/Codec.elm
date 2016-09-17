module ColumnEditor.Codec exposing (decodeAgentModel, columnsEncoder)


import Json.Encode as JsonEncode exposing (encode)
import Json.Decode as JsonDecode exposing (Decoder, (:=), succeed, fail, andThen)
import ColumnEditor.Model exposing (..)


decodeAgentModel : String -> Result String AgentModel
decodeAgentModel serialised =
  JsonDecode.decodeString agentModelDecoder serialised


agentModelDecoder : Decoder AgentModel
agentModelDecoder =
  JsonDecode.object1 AgentModel
    ("columns" := JsonDecode.list columnDecoder)


columnDecoder : Decoder Column
columnDecoder =
  JsonDecode.object3 Column
    (JsonDecode.at ["name"] JsonDecode.string)
    ("selected" := stringBoolDecoder)
    ("system" := stringBoolDecoder)


--TODO: put this somewhere common between components
stringBoolDecoder : Decoder Bool
stringBoolDecoder =
  JsonDecode.string `andThen` \val ->
    case val of
      "true" -> succeed True
      "false" -> succeed False
      _ -> fail <| "Expecting \"true\" or \"false\" but found " ++ val


columnsEncoder : List Column -> String
columnsEncoder columns =
  JsonEncode.encode 0 (columnsEncoderValue columns)


columnsEncoderValue : List Column -> JsonEncode.Value
columnsEncoderValue columns =
    JsonEncode.object
        [ ("columns", JsonEncode.list (List.map (\t -> columnEncoderValue t) columns))
        ]


columnEncoderValue : Column -> JsonEncode.Value
columnEncoderValue column =
    JsonEncode.object
        [ ("name", JsonEncode.string column.name)
        , ("selected", JsonEncode.bool column.selected)
        , ("system", JsonEncode.bool column.system)
        ]
