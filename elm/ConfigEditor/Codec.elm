module ConfigEditor.Codec exposing (decodeAgentModel)


import Json.Decode as JsonDecode exposing (Decoder, (:=), succeed, fail, andThen)
import ConfigEditor.Model exposing (..)


decodeAgentModel : String -> Result String AgentModel
decodeAgentModel serialised =
  JsonDecode.decodeString agentModelDecoder serialised


agentModelDecoder : Decoder AgentModel
agentModelDecoder =
  JsonDecode.object2 AgentModel
    ("columns" := JsonDecode.list columnDecoder)
    (JsonDecode.at ["config"] JsonDecode.string)


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
