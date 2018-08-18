package functional

import model.internal.TransportType.TransportType
import model.internal.OperationType._
import model.internal.price.enum.Size._
import model.internal.price.enum.Weight.MEDIUM
import model.internal._
import model.request.OrderCreation
import com.github.nscala_time.time.Imports.DateTime
import common.DateTimeNow._

trait ModelData {

  //------------Cities-------------
  val almagro = City(1, "Almagro")
  val parquePatricios = City(2, "Parque Patricios")
  val balvanera = City(3, "Balvanera")
  val saavedra = City(4, "Saavedra")
  val boedo = City(5, "Boedo")

  //---------Generics------------------
  val geolocation = Geolocation(132, -123)
  val contactInfo = ContactInfo("email@email.com", "phone")

  //--------Payment Methods------------
  val visa = PaymentMethod("ownerName", "123", Some("cardCode"), Some("bankCode"), "02/20", "securityCode", Some("VISA"))
  val masterCard = PaymentMethod("ownerName", "123", Some("cardCode"), Some("bankCode"), "02/20", "securityCode", Some("MASTER CARD"))
  val visaDebito = PaymentMethod("ownerName", "123", Some("cardCode"), Some("bankCode"), "02/20", "securityCode", Some("VISA DEBITO"))


  //--------Addresses---------
  val almagroAddress = Address(geolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), almagro, public = true, None, None)
  val balvaneraAddress = Address(geolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), balvanera, public = true, None, None)
  val saavedraAddress = Address(geolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), saavedra, public = true, None, None)
  val boedoAddress = Address(geolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), boedo, public = true, None, None)
  val parquePatriciosAddress = Address(geolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), parquePatricios, public = true, None, None)


  //------------Users------------------
  val id_1 = "1"
  val id_2 = "2"
  val id_3 = "3"
  val id_4 = "4"
  val id_5 = "5"

  val birthDate = rightNowTime

  val marcelo = User(id_1, "oneSignal", "marcelo.l", "marcelo", "l", "36121311", birthDate,
    contactInfo, "photoUrl", Seq(almagroAddress), None, Some(Seq(visa, visaDebito)), None, None, None)

  val lucas = User(id_2, "oneSignal", "lucas.c", "lucas", "c", "36121312", birthDate,
    contactInfo, "photoUrl", Seq(parquePatriciosAddress), None, Some(Seq(masterCard)), None, None, None)

  val german = User(id_3, "oneSignal", "german.l", "german", "l", "36121313", birthDate,
    contactInfo, "photoUrl", Seq(balvaneraAddress), None, Some(Seq(visa)), None, None, None)

  val roman = User(id_4, "oneSignal", "roman.l", "roman", "g", "36121314", birthDate,
    contactInfo, "photoUrl", Seq(boedoAddress), None, Some(Seq(visaDebito)), None, None, None)

  val nazareno = User(id_5, "oneSignal", "nazareno.l", "nazareno", "l", "36121315", birthDate,
    contactInfo, "photoUrl", Seq(saavedraAddress), None, Some(Seq(visa, masterCard)), None, None, None)


  //--------------Orders---------------
  val orderId_1 = "1"
  val orderId_2 = "2"
  val orderId_3 = "3"
  val orderId_4 = "4"
  val orderId_5 = "5"


  val almagroToSaavedra = Route(almagroAddress, saavedraAddress)

  val today = DateTime.now().plusMinutes(10)
  val yesterday = today.minusDays(1)
  val tomorrow = today.plusDays(1)
  val afterTomorow = today.plusDays(2)

  //2 Hours = 7200 seconds
  val durationTrip = 7200

  val supportedTransports: List[TransportType] = TransportType.values.toList

  val newOrder = OrderCreation(None, marcelo._id, lucas._id, "description",
    SENDER, SMALL, MEDIUM, supportedTransports,
    almagroToSaavedra, today,
    tomorrow, None, None, visa, 100.5, durationTrip)

}








