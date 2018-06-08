package onesignal.html


import scala.io.Source._

object HTML {

  private def stringFile(path: String) = {
    val file = fromFile(path, "UTF-8")
    val stringFile = file.mkString
    file.close

    stringFile
  }

  val Path = "app/onesignal/html"

  val TRAVELLING: String = stringFile(s"$Path/travelling.html")
  val CREATED = stringFile(s"$Path/created.html")
  val CANCELED = stringFile(s"$Path/canceled.html")
  val FINALIZED = stringFile(s"$Path/finalized.html")

}
