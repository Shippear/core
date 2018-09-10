package notification.email

import java.io.File

import com.cloudinary.Cloudinary
import common.{ConfigReader, Logging}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class CloudinaryWrapper extends ConfigReader with Logging {

  private val config: CloudinaryConfig = envConfiguration.getConfig("cloudinary").as[CloudinaryConfig]
  private var active = config.activated

  private val url = config.url.getOrElse("")

  private val cloudinary = new Cloudinary(url)

  def activated(state: Boolean): Boolean = {
    active = state
    active
  }

  def upload(image: File): String = {
    if(active) {
      val url = Try(cloudinary.uploader().upload(image, Map.empty.asJava)) match {
        case Success(response) => response.get("url").asInstanceOf[String]
        case Failure(ex) => error("Error uploading file", ex); ""
      }
      url
    }
    else
      ""
  }

}
