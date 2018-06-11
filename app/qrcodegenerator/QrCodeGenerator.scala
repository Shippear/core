package qrcodegenerator

import java.io.File

import net.glxn.qrgen.core.image.ImageType
import net.glxn.qrgen.javase.QRCode

class QrCodeGenerator {
  def generateQrImage(idOrder : String): QRCode = QRCode.from(idOrder).to(ImageType.JPG)
  def generateQrImageFile(idOrder : String): File = this.generateQrImage(idOrder : String).file()

}
