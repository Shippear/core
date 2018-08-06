package qrcodegenerator

import java.io.File

import net.glxn.qrgen.core.image.ImageType
import net.glxn.qrgen.javase.QRCode

class QrCodeGenerator {

  def generateQrImage(idOrder: String): QRCode = QRCode.from(idOrder).to(ImageType.PNG).withSize(400, 400)

  def generateQrImageFile(idOrder : String): File = this.generateQrImage(idOrder).file()

}

object QrCodeGenerator {
  implicit def toByteArray(qrImage: QRCode): Array[Byte] = qrImage.stream().toByteArray
}
