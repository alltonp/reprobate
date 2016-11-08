module ConfigEditor.Codec exposing (decodeAgentModel)


import Json.Decode as JsonDecode exposing (Decoder, (:=), succeed, fail, andThen)
import ConfigEditor.Model exposing (..)


decodeAgentModel : String -> Result String AgentModel
decodeAgentModel serialised =
  JsonDecode.decodeString agentModelDecoder serialised


agentModelDecoder : Decoder AgentModel
agentModelDecoder =
  JsonDecode.object1 AgentModel
    (JsonDecode.at ["config"] JsonDecode.string)
