package app.model

import java.io.File
import scala.io.Source

object ProbeRegistry {
  private val file = new File("checks.csv")

  def load = {
    val counter = ProbeIdCounter()
    if (!file.exists()) writeToFile(template)
    Source.fromFile(file).getLines().filterNot(l => l.trim.isEmpty || l.startsWith("-")).map(Probe(counter.next, _)).toList
  }

  private def writeToFile(content: String) {
    val pw = new java.io.PrintWriter(file)
    try pw.write(content) finally pw.close()
  }

  private val template =
    """
      |-----Reprobate-----
      |---USAGE:
      |---{- to ignore}url,description,active,remedy,defcon
      |---TIP: the order in the file is the order on screen, so put most interesting at the top
      |---TIP: name probes so the messages yield the problem nicely ... e.g. app is dead (Prod)
      |---TIP: active - can be from (e.g. "8") or a range (e.g. "8-10)
      |---TIP: defcon - 5..1 ... where 1 is somewhat shouty ...
      |
      |--LOCAL--
      |http://localhost:8473,/demo/success,LOCAL,Success (Demo),,Call support,5
      |http://localhost:8473,/demo/flipflop,LOCAL,Flipflop (Demo),,Call support,5
      |-http://localhost:8473,/demo/oddevenminute,LOCAL,Failure on odd minute (Demo),,Call support,5
      |-http://localhost:8473,/demo/oddevenhour,LOCAL,Failure on odd hour (Demo),,Call support,5
      |-http://localhost:8473,/demo/failureafter/10,LOCAL,Failure after 10 probes executed (Demo),,Call support,5
      |-http://localhost:8473,/demo/failureafter/20,LOCAL,Failure after 20 probes executed (Demo),,Call support,5
      |http://localhost:8473,/demo/missing,LOCAL,Missing (Demo),,Call support,5
      |http://localhost:8473,/demo/slow/11,LOCAL,Timeout (Demo),,Call support,5
      |http://localhost:8473,/demo/failure,LOCAL,Failure @ defcon 5 (Demo),,Call support,5
      |http://localhost:8473,/demo/failure,LOCAL,Failure @ defcon 4 (Demo),,Call support,4
      |http://localhost:8473,/demo/failure,LOCAL,Failure @ defcon 3 (Demo),,Call support,3
      |http://localhost:8473,/demo/failure,LOCAL,Failure @ defcon 2 (Demo),,Call support,2
      |http://localhost:8473,/demo/failure,LOCAL,Failure @ defcon 1 (Demo),,Call support,1
      |http://localhost:8473,/demo/failure,LOCAL,Failure active between 10am and 1pm (Demo),10-13,Call support,5
      |http://localhost:8473,/demo/failure,LOCAL,Largely inactive (Demo),23-24,Call support,5
      |
    """.stripMargin
}