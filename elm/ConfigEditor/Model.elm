module ConfigEditor.Model exposing (..)

import Dict


--TODO: watch http://krisajenkins.github.io/Types_As_A_Design_Tool/#/sec-title-slide


type alias Model =
    { agentModel : Maybe AgentModel
    , editing : Bool
    , error : Maybe String
    , command : String
    }


type alias AgentModel =
    { config : String
    }


initialModel : Model
initialModel =
    Model Nothing False Nothing ""



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
-- use auth (email) to get the list of tokens the user is allowed access to
-- then have a dropdown for the user to choose, next to command, changes it changes the rim instance
-- make status be based on index
