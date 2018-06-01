package qrcodegenerator

import common.Logging
import dao.embbebedmongo.MongoTest
import org.scalatestplus.play.PlaySpec


class QrCodeGeneratorTest extends PlaySpec with MongoTest with Logging{
  "QR Code Generator" should{
    val qrCodeGenerator = new QrCodeGenerator
    val idOrderTest = "379886"

  "Generate a JPG Image" in{
     val result = qrCodeGenerator.generateQrImageFile(idOrderTest)

     error(s"El archvio QR está en: $result")
    }

  }

}
