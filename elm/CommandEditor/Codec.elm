module CommandEditor.Codec exposing (decodeAgentModel)


import Json.Decode as JsonDecode exposing (Decoder, (:=), succeed, fail, andThen)
import CommandEditor.Model exposing (..)


decodeAgentModel : String -> Result String AgentModel
decodeAgentModel serialised =
  JsonDecode.decodeString agentModelDecoder serialised


agentModelDecoder : Decoder AgentModel
agentModelDecoder =
  JsonDecode.object1 AgentModel
    (JsonDecode.at ["data"] JsonDecode.string)
