module ColumnEditor.Model exposing (..)


import Dict


--TODO: watch http://krisajenkins.github.io/Types_As_A_Design_Tool/#/sec-title-slide

type alias Model =
   { agentModel : Maybe AgentModel
   , editing : Bool
   , error : Maybe String
   , editedColumns : List Column
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
initialModel = Model Nothing False Nothing [] ""


