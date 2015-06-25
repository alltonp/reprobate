package app.restlike.common

//TIP: http://tldp.org/HOWTO/Bash-Prompt-HOWTO/x329.html
//but some of the names are wrong, light often means bold
//OR;
//http://misc.flogisoft.com/bash/tip_colors_and_formatting
//TIP: 2=dim, 1=bold, 0=normal
object Colours {
  private val RED ="\033[0;31m"
  private val GREEN = "\033[0;32m"
  private val YELLOW ="\033[0;33m"
  private val BLUE = "\033[0;34m"
  private val MAGENTA = "\033[0;35m"
  private val CYAN = "\033[0;36m"
  private val LIGHTGREY ="\033[0;37m"

  private val DULLCYAN = "\033[2;36m"
  private val DULLGREEN = "\033[2;38m"
  private val DULLYELLOW = "\033[2;33m"
//  private val DARKGREY ="\033[1;30m"
//  private val LIGHTMAGENTA ="\033[0;95m"
  //TIP: 38 and 5 = this is special from the colour chart of 256
//  private val ORANGE ="\033[0;38;5;208m"
//  private val WHITE ="\033[1;37m"

  private val CUSTOMRED ="\033[0;38;5;9m"
  private val CUSTOMBLUE ="\033[0;38;5;33m"
  private val CUSTOMGREEN ="\033[0;38;5;40m"
  private val CUSTOMGREY ="\033[0;38;5;244m"
  private val CUSTOMIVORY ="\033[0;38;5;187m"
  private val CUSTOMORANGE ="\033[0;38;5;208m"
  private val CUSTOMMAGENTA ="\033[0;38;5;5m"
  private val CUSTOMYELLOW ="\033[0;38;5;227m"

  private val END ="\033[0m"

  def red(value: String) = s"$CUSTOMRED$value$END"
  def blue(value: String) = s"$BLUE$value$END"
  def cyan(value: String) = s"$CYAN$value$END"
  def lightGrey(value: String) = s"$LIGHTGREY$value$END"

//  def darkGrey(value: String) = s"$DARKGREY$value$END"

  def dullCyan(value: String) = s"$DULLCYAN$value$END"
  def dullMagenta(value: String) = s"$MAGENTA$value$END"
  def dullGreen(value: String) = s"$DULLGREEN$value$END"
  def dullOrange(value: String) = s"$YELLOW$value$END"
  def dullYellow(value: String) = s"$DULLYELLOW$value$END"
//  def brightMagenta(value: String) = s"$LIGHTMAGENTA$value$END"

  def customBlue(value: String) = s"$CUSTOMBLUE$value$END"
  def customGrey(value: String) = s"$CUSTOMGREY$value$END"
  def customIvory(value: String) = s"$CUSTOMIVORY$value$END"
  def customMagenta(value: String) = s"$CUSTOMMAGENTA$value$END"
  def customOrange(value: String) = s"$CUSTOMORANGE$value$END"
  def customYellow(value: String) = s"$CUSTOMYELLOW$value$END"
  def customGreen(value: String) = s"$CUSTOMGREEN$value$END"

  //  def white(value: String) = s"$WHITE$value$END"
}
