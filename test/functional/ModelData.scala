package functional

import java.util.Date

import model.internal._

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
  val visa = PaymentMethod("ownerName", "123", "02/20", "securityCode", "VISA")
  val masterCard = PaymentMethod("ownerName", "123", "02/20", "securityCode", "MASTER CARD")
  val visaDebito = PaymentMethod("ownerName", "123", "02/20", "securityCode", "VISA DEBITO")


  //--------Addresses---------
  val almagroAddress = Address(geolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), almagro, public = true)
  val balvaneraAddress = Address(geolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), balvanera, public = true)
  val saavedraAddress = Address(geolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), saavedra, public = true)
  val boedoAddress = Address(geolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), boedo, public = true)
  val parquePatriciosAddress = Address(geolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), parquePatricios, public = true)


  //------------Users------------------
  val id_1 = "1"
  val id_2 = "2"
  val id_3 = "3"
  val id_4 = "4"
  val id_5 = "5"

  val marcelo = User(id_1, "oneSignal", "marcelo.l", "marcelo", "l", "36121311",
    contactInfo, "photoUrl", Seq(almagroAddress), None, Seq(visa, visaDebito), None, None, None)

  val lucas = User(id_2, "oneSignal", "lucas.c", "lucas", "c", "36121312",
    contactInfo, "photoUrl", Seq(parquePatriciosAddress), None, Seq(masterCard), None, None, None)

  val german = User(id_3, "oneSignal", "german.l", "german", "l", "36121313",
    contactInfo, "photoUrl", Seq(balvaneraAddress), None, Seq(visa), None, None, None)

  val roman = User(id_4, "oneSignal", "roman.l", "roman", "g", "36121314",
    contactInfo, "photoUrl", Seq(boedoAddress), None, Seq(visaDebito), None, None, None)

  val nazareno = User(id_5, "oneSignal", "nazareno.l", "nazareno", "l", "36121315",
    contactInfo, "photoUrl", Seq(saavedraAddress), None, Seq(visa, masterCard), None, None, None)


  //--------------Orders---------------
  val orderId_1 = "1"
  val orderId_2 = "2"
  val orderId_3 = "3"
  val orderId_4 = "4"
  val orderId_5 = "5"


  val almagroToSaavedra = Route(almagroAddress, saavedraAddress)

  val orderWithoutCarrier = Order(orderId_1, marcelo._id, lucas._id, None, "description",
      OrderState.PENDING_PARTICIPANT, OperationType.SENDER,
      almagroToSaavedra, new Date, new Date,
      Some(new Date), Some(new Date), None, None)

}








