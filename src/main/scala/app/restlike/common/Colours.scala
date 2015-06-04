package app.restlike.common

//TIP: http://tldp.org/HOWTO/Bash-Prompt-HOWTO/x329.html
//but some of the names are wrong, light often means bold
//OR;
//http://misc.flogisoft.com/bash/tip_colors_and_formatting
//TIP: 2=dim, 1=bold, 0=normal
object Colours {
  private val BLUE = "\033[0;34m"
  private val CYAN = "\033[0;36m"
  private val DULLCYAN = "\033[2;36m"
  private val GREEN = "\033[1;92m"
  private val DARKGREY ="\033[1;30m"
  private val LIGHTGREY ="\033[0;37m"
  private val LIGHTMAGENTA ="\033[0;95m"
  private val MAGENTA = "\033[0;35m"
  //TIP: 38 and 5 = this is special from the colour chart of 256
  private val ORANGE ="\033[0;38;5;208m"
  private val RED ="\033[0;31m"
  private val WHITE ="\033[1;37m"
  private val YELLOW ="\033[0;33m"
  private val END ="\033[0m"

  def blue(value: String) = s"$BLUE$value$END"
  def cyan(value: String) = s"$CYAN$value$END"
  def darkGrey(value: String) = s"$DARKGREY$value$END"
  def dullMagenta(value: String) = s"$MAGENTA$value$END"
  def dullCyan(value: String) = s"$DULLCYAN$value$END"
  def dullYellow(value: String) = s"$YELLOW$value$END"
  def lightGrey(value: String) = s"$LIGHTGREY$value$END"
  def brightMagenta(value: String) = s"$LIGHTMAGENTA$value$END"
  def orange(value: String) = s"$ORANGE$value$END"
  def red(value: String) = s"$RED$value$END"
  def white(value: String) = s"$WHITE$value$END"
}
