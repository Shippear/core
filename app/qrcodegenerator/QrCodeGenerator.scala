package qrcodegenerator

import java.io.File

import net.glxn.qrgen.core.image.ImageType
import net.glxn.qrgen.javase.QRCode

class QrCodeGenerator {
  def generateQrImageFile(idOrder : String): File = QRCode.from(idOrder).to(ImageType.JPG).file()
  def generateQrImage(idOrder : String): QRCode = QRCode.from(idOrder).to(ImageType.JPG)
}
