package app.restlike.common

//TIP: http://tldp.org/HOWTO/Bash-Prompt-HOWTO/x329.html
//but some of the names are wrong, light often means bold
//OR;
//http://misc.flogisoft.com/bash/tip_colors_and_formatting
object Colours {
  private val BLUE = "\033[0;34m"
  private val CYAN = "\033[0;36m"
  private val GREEN = "\033[1;92m"
  private val DARKGREY ="\033[1;30m"
  private val LIGHTGREY ="\033[0;37m"
  private val LIGHTMAGENTA ="\033[0;95m"
  private val MAGENTA = "\033[0;35m"
  private val ORANGE ="\033[0;38;5;208m"
  private val RED ="\033[0;31m"
  private val WHITE ="\033[1;37m"
  private val END ="\033[0m"

  def blue(value: String) = s"$BLUE$value$END"
  def cyan(value: String) = s"$CYAN$value$END"
  def darkGrey(value: String) = s"$DARKGREY$value$END"
  def lightGrey(value: String) = s"$LIGHTGREY$value$END"
  def brightMagenta(value: String) = s"$LIGHTMAGENTA$value$END"
  def orange(value: String) = s"$ORANGE$value$END"
  def dullMagenta(value: String) = s"$MAGENTA$value$END"
  def red(value: String) = s"$RED$value$END"
  def white(value: String) = s"$WHITE$value$END"
}
