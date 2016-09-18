module ColumnEditor.Model exposing (..)


import Dict


--TODO: watch http://krisajenkins.github.io/Types_As_A_Design_Tool/#/sec-title-slide

type alias Model =
   { agentModel : Maybe AgentModel
   , editing : Bool
   , error : Maybe String
   , command : String
   }


type alias Column =
  { name : String
  , selected : Bool
  , system : Bool
  }


type alias AgentModel =
   { columns : List Column
   }


initialModel : Model
initialModel = Model Nothing False Nothing ""


--TODO:
-- feedback on command
-- start to populate board, gonna need some json for that
-- up arrow for history etc
-- one shown, click on an issue to get context sensitive
-- store timestamp and who on create
-- need to know if a command worked, either a left or right or something
-- we will always want the board back (in some way)
-- it would be good if help could be useful too ...
  -- using different Responses to Elm, HelpResponse, BoardResponse etc
