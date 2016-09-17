module ColumnEditor.Util exposing (indexOf)

--TIP: borrowed from - http://stackoverflow.com/questions/34252924/position-of-element-in-list
indexOf : a -> List a -> Maybe Int
indexOf el list =
  let
    indexOf' list' index =
      case list' of
        [] ->
          Nothing
        (x::xs) ->
          if x == el then
            Just index
          else
            indexOf' xs (index + 1)
  in
    indexOf' list 0