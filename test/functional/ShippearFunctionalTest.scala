package functional

import embbebedmongo.MongoTest
import model.internal.OrderState._
import model.internal._
import model.request.{AuxRequest, CarrierRating}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.test.Helpers.{await, _}
import service.Exception.ShippearException
import service.{OrderService, UserService}

class ShippearFunctionalTest extends MongoTest with GuiceOneServerPerSuite with ModelData {

  val orderService = app.injector.instanceOf(classOf[OrderService])
  val userService = app.injector.instanceOf(classOf[UserService])

  "Shippear Functional Test" should {

    "have full order flow consistency with an auxiliar request" in {

      // 1. Creating users and orders
      await(userService.create(marcelo))
      await(userService.create(lucas))

      //Applicant Marcelo
      //Participant Lucas
      val orderWithoutCarrier = await(orderService.createOrder(newOrder))

      var order = await(orderService.findById(orderWithoutCarrier._id))
      var userMarcelo = await(userService.findById(marcelo._id))
      var userLucas = await(userService.findById(lucas._id))


      // 2. The participant accepts the order
      await(orderService.confirmParticipant(order._id))

      order = await(orderService.findById(order._id))
      userLucas = await(userService.findById(lucas._id))
      userMarcelo = await(userService.findById(marcelo._id))

      toState(order.state) mustEqual PENDING_CARRIER
      var orderLucas = userLucas.orders.get.head
      var orderMarcelo = userMarcelo.orders.get.head

      orderLucas mustBe order
      orderMarcelo mustBe order

      //---------

      // 3. Assigning carrier German
      await(userService.create(german))

      val assignCarrier = AssignCarrier(orderWithoutCarrier._id, german._id)
      order = await(orderService.assignCarrier(assignCarrier))

      toState(order.state) mustBe PENDING_PICKUP
      order.applicant.id mustBe marcelo._id
      order.participant.id mustBe lucas._id
      order.carrier.get.id mustBe german._id
      order.qrCode.isDefined mustBe true

      userMarcelo = await(userService.findById(marcelo._id))
      userLucas = await(userService.findById(lucas._id))
      var userGerman = await(userService.findById(german._id))

      orderMarcelo = userMarcelo.orders.get.head
      orderLucas = userLucas.orders.get.head
      var orderGerman = userGerman.orders.get.head

      toState(orderMarcelo.state) mustBe PENDING_PICKUP
      toState(orderLucas.state) mustBe PENDING_PICKUP
      toState(orderGerman.state) mustBe PENDING_PICKUP

      //---------

      // 4. Verification QR code of CARRIER -> STATE = ON_TRAVEL
      val validateCarrier = OrderToValidate(order._id, german._id, UserType.CARRIER)
      val validationCarrier = await(orderService.validateQrCode(validateCarrier))
      validationCarrier mustBe true

      val orderTravel = await(orderService.findById(order._id))
      toState(orderTravel.state) mustBe ON_TRAVEL

      val userMarceloTravel = await(userService.findById(marcelo._id))
      val userLucasTravel = await(userService.findById(lucas._id))
      val userGermanTravel = await(userService.findById(german._id))

      orderMarcelo = userMarceloTravel.orders.get.head
      orderLucas = userLucasTravel.orders.get.head
      orderGerman = userGermanTravel.orders.get.head

      toState(orderMarcelo.state) mustBe ON_TRAVEL
      toState(orderLucas.state) mustBe ON_TRAVEL
      toState(orderGerman.state) mustBe ON_TRAVEL


      //---------

      // 5. Carrier German requires aux
      val minimalAddress = MinimalAddress(Geolocation(-43, 43), "some street")
      val auxRequest = AuxRequest(order._id, german._id, minimalAddress)

      val auxOrder = await(orderService.auxRequest(auxRequest))
      toState(auxOrder.state) mustBe PENDING_AUX
      auxOrder.route.auxOrigin mustBe Some(minimalAddress)
      auxOrder.historicCarriers mustBe Some(List(auxOrder.carrier.get))

      userMarcelo = await(userService.findById(marcelo._id))
      userLucas = await(userService.findById(lucas._id))
      userGerman = await(userService.findById(german._id))

      orderMarcelo = userMarcelo.orders.get.head
      orderLucas = userLucas.orders.get.head
      orderGerman = userGerman.orders.get.head

      toState(orderMarcelo.state) mustBe PENDING_AUX
      toState(orderLucas.state) mustBe PENDING_AUX
      toState(orderGerman.state) mustBe PENDING_AUX


      //---------

      // 6. Assigning new carrier Roman

      await(userService.create(roman))

      val assignCarrierRoman = AssignCarrier(auxOrder._id, roman._id)
      order = await(orderService.assignCarrier(assignCarrierRoman))

      toState(order.state) mustBe PENDING_PICKUP
      order.applicant.id mustBe marcelo._id
      order.participant.id mustBe lucas._id
      order.carrier.get.id mustBe roman._id

      userMarcelo = await(userService.findById(marcelo._id))
      userLucas = await(userService.findById(lucas._id))
      var userRoman = await(userService.findById(roman._id))
      userGerman = await(userService.findById(german._id))

      orderMarcelo = userMarcelo.orders.get.head
      orderLucas = userLucas.orders.get.head
      orderGerman = userGerman.orders.get.head
      var orderRoman = userRoman.orders.get.head

      toState(orderMarcelo.state) mustBe PENDING_PICKUP
      toState(orderLucas.state) mustBe PENDING_PICKUP
      toState(orderRoman.state) mustBe PENDING_PICKUP

      //Previous carrier German
      toState(orderGerman.state) mustBe PENDING_AUX


      //---------

      // 7. Verification QR code of new CARRIER Roman
      val validateCarrierRoman = OrderToValidate(order._id, roman._id, UserType.CARRIER)
      val validationCarrierRoman = await(orderService.validateQrCode(validateCarrierRoman))
      validationCarrierRoman mustBe true

      val orderTravelRoman = await(orderService.findById(order._id))
      toState(orderTravelRoman.state) mustBe ON_TRAVEL

      val userMarceloTravelAgain = await(userService.findById(marcelo._id))
      val userLucasTravelAgain = await(userService.findById(lucas._id))
      val userGermanCanceled = await(userService.findById(german._id))
      val userRomanTravel = await(userService.findById(roman._id))

      orderMarcelo = userMarceloTravelAgain.orders.get.head
      orderLucas = userLucasTravelAgain.orders.get.head
      orderGerman = userGermanCanceled.orders.get.head
      val orderRomanTravel = userRomanTravel.orders.get.head

      toState(orderMarcelo.state) mustBe ON_TRAVEL
      toState(orderLucas.state) mustBe ON_TRAVEL
      toState(orderRomanTravel.state) mustBe ON_TRAVEL

      // Previous carrier German with a cancelled order
      toState(orderGerman.state) mustBe CANCELLED
      orderGerman.finalizedDate.isDefined mustBe true

      // 5. Order delivered
      // Validating QR code of APPLICANT
      val validateApplicant = OrderToValidate(order._id, marcelo._id, UserType.APPLICANT)
      val validationApplicant = await(orderService.validateQrCode(validateApplicant))
      validationApplicant mustBe true

      order = await(orderService.findById(order._id))
      userMarcelo = await(userService.findById(marcelo._id))
      userLucas = await(userService.findById(lucas._id))
      userRoman = await(userService.findById(roman._id))

      orderMarcelo = userMarcelo.orders.get.head
      orderLucas = userLucas.orders.get.head
      orderRoman = userRoman.orders.get.head

      toState(orderMarcelo.state) mustBe DELIVERED
      toState(orderLucas.state) mustBe DELIVERED
      toState(orderRoman.state) mustBe DELIVERED

      order.finalizedDate.isDefined mustBe true
      orderMarcelo.finalizedDate.isDefined mustBe true
      orderLucas.finalizedDate.isDefined mustBe true
      orderRoman.finalizedDate.isDefined mustBe true

      //6. Rating the carrier
      val rating = CarrierRating(order._id, 4)
      await(orderService.rateCarrier(rating))

      userRoman = await(userService.findById(roman._id))
      userRoman.scoring.get mustBe 4

      intercept[ShippearException]{
        await(orderService.rateCarrier(rating))
      }
    }


    "have full order flow consistency in a happy path" in  {

      // 1. Creating users and orders
      await(userService.create(marcelo))
      await(userService.create(lucas))
      val orderWithoutCarrier = await(orderService.createOrder(newOrder))

      var order = await(orderService.findById(orderWithoutCarrier._id))
      var userMarcelo = await(userService.findById(marcelo._id))
      var userLucas = await(userService.findById(lucas._id))

      order.price mustBe 100.5
      order.carrierEarning.get mustBe 90.45


      // Checking consistency between them
      val marceloOrders = userMarcelo.orders.get
      marceloOrders.size mustBe 1
      marceloOrders must contain(order)

      val lucasOrders = userLucas.orders.get
      lucasOrders.size mustBe 1
      lucasOrders must contain(order)

      toState(order.state) mustEqual PENDING_PARTICIPANT

      //---------

      // Updating an user with a new address
      val marceloNewAddress = marcelo.copy(addresses = Seq(boedoAddress))
      await(userService.update(marceloNewAddress))
      userMarcelo = await(userService.findById(marcelo._id))
      userMarcelo.addresses.head mustBe boedoAddress

      //---------

      // Updating an user without a public address
      val boedoAddressPrivate = boedoAddress.copy(public = false)
      val marceloNewAddressFail = marcelo.copy(addresses = Seq(boedoAddressPrivate))

      intercept[ShippearException]{
        await(userService.update(marceloNewAddressFail))
      }

      //---------

      // 2. The participant accepts the order
      await(orderService.confirmParticipant(order._id))

      order = await(orderService.findById(order._id))
      userLucas = await(userService.findById(lucas._id))
      userMarcelo = await(userService.findById(marcelo._id))

      toState(order.state) mustEqual PENDING_CARRIER
      var orderLucas = userLucas.orders.get.head
      var orderMarcelo = userMarcelo.orders.get.head

      orderLucas mustBe order
      orderMarcelo mustBe order

      //---------

      // 3. Assigning carrier German
      await(userService.create(german))

      val assignCarrier = AssignCarrier(orderWithoutCarrier._id, german._id)
      order = await(orderService.assignCarrier(assignCarrier))

      toState(order.state) mustBe PENDING_PICKUP
      order.applicant.id mustBe marcelo._id
      order.participant.id mustBe lucas._id
      order.carrier.get.id mustBe german._id
      order.qrCode.isDefined mustBe true

      userMarcelo = await(userService.findById(marcelo._id))
      userLucas = await(userService.findById(lucas._id))
      var userGerman = await(userService.findById(german._id))

      orderMarcelo = userMarcelo.orders.get.head
      orderLucas = userLucas.orders.get.head
      var orderGerman = userGerman.orders.get.head

      toState(orderMarcelo.state) mustBe PENDING_PICKUP
      toState(orderLucas.state) mustBe PENDING_PICKUP
      toState(orderGerman.state) mustBe PENDING_PICKUP

      // 4. Verification code of CARRIER -> STATE = ON_TRAVEL
      val validateCarrier = OrderToValidate(order._id, german._id, UserType.CARRIER)
      val validationCarrier = await(orderService.validateQrCode(validateCarrier))
      validationCarrier mustBe true

      val orderTravel = await(orderService.findById(order._id))
      toState(orderTravel.state) mustBe ON_TRAVEL

      val userMarceloTravel = await(userService.findById(marcelo._id))
      val userLucasTravel = await(userService.findById(lucas._id))
      val userGermanTravel = await(userService.findById(german._id))

      orderMarcelo = userMarceloTravel.orders.get.head
      orderLucas = userLucasTravel.orders.get.head
      orderGerman = userGermanTravel.orders.get.head

      toState(orderMarcelo.state) mustBe ON_TRAVEL
      toState(orderLucas.state) mustBe ON_TRAVEL
      toState(orderGerman.state) mustBe ON_TRAVEL

      // Fail cases
      val validationFail = OrderToValidate(order._id, nazareno._id, UserType.CARRIER)
      await(orderService.validateQrCode(validationFail)) mustBe false
      await(orderService.validateQrCode(validationFail.copy(userType = UserType.PARTICIPANT))) mustBe false
      await(orderService.validateQrCode(validationFail.copy(userType = UserType.APPLICANT))) mustBe false


      // 5. Order delivered
      // Validating QR code of APPLICANT
      val validateApplicant = OrderToValidate(order._id, marcelo._id, UserType.APPLICANT)
      val validationApplicant = await(orderService.validateQrCode(validateApplicant))
      validationApplicant mustBe true

      order = await(orderService.findById(order._id))
      userMarcelo = await(userService.findById(marcelo._id))
      userLucas = await(userService.findById(lucas._id))
      userGerman = await(userService.findById(german._id))

      orderMarcelo = userMarcelo.orders.get.head
      orderLucas = userLucas.orders.get.head
      orderGerman = userGerman.orders.get.head

      toState(orderMarcelo.state) mustBe DELIVERED
      toState(orderLucas.state) mustBe DELIVERED
      toState(orderGerman.state) mustBe DELIVERED

      order.finalizedDate.isDefined mustBe true
      orderMarcelo.finalizedDate.isDefined mustBe true
      orderLucas.finalizedDate.isDefined mustBe true
      orderGerman.finalizedDate.isDefined mustBe true

      //6. Rating the carrier
      val rating = CarrierRating(order._id, 4)
      await(orderService.rateCarrier(rating))

      userGerman = await(userService.findById(german._id))
      userGerman.scoring.get mustBe 4

      intercept[ShippearException]{
        await(orderService.rateCarrier(rating))
      }


    }


  }



}
