package functional

import embbebedmongo.MongoTest
import model.internal.OrderState._
import model.internal.{AssignCarrier, OrderState, OrderToValidate, UserType}
import model.request.CarrierRating
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.test.Helpers.{await, _}
import service.Exception.ShippearException
import service.{OrderService, UserService}

class ShippearFunctionalTest extends MongoTest with GuiceOneServerPerSuite with ModelData {

  val orderService = app.injector.instanceOf(classOf[OrderService])
  val userService = app.injector.instanceOf(classOf[UserService])

  "Shippear Functional Test" should {

    "have full order flow consistency" in {

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

      toState(order.state) mustEqual OrderState.PENDING_PARTICIPANT

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

      toState(order.state) mustEqual OrderState.PENDING_CARRIER
      var orderLucas = userLucas.orders.get.head
      var orderMarcelo = userMarcelo.orders.get.head

      orderLucas mustBe order
      orderMarcelo mustBe order

      //---------

      // 3. Assigning carrier German
      await(userService.create(german))

      val assignCarrier = AssignCarrier(orderWithoutCarrier._id, german._id)
      order = await(orderService.assignCarrier(assignCarrier))

      toState(order.state) mustBe OrderState.PENDING_PICKUP
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

      toState(orderMarcelo.state) mustBe OrderState.PENDING_PICKUP
      toState(orderLucas.state) mustBe OrderState.PENDING_PICKUP
      toState(orderGerman.state) mustBe OrderState.PENDING_PICKUP

      // 4. Verification code of CARRIER -> STATE = ON_TRAVEL
      val validateCarrier = OrderToValidate(order._id, german._id, UserType.CARRIER)
      val validationCarrier = await(orderService.validateQrCode(validateCarrier))
      validationCarrier mustBe true

      val orderTravel = await(orderService.findById(order._id))
      toState(orderTravel.state) mustBe OrderState.ON_TRAVEL

      val userMarceloTravel = await(userService.findById(marcelo._id))
      val userLucasTravel = await(userService.findById(lucas._id))
      val userGermanTravel = await(userService.findById(german._id))

      orderMarcelo = userMarceloTravel.orders.get.head
      orderLucas = userLucasTravel.orders.get.head
      orderGerman = userGermanTravel.orders.get.head

      toState(orderMarcelo.state) mustBe OrderState.ON_TRAVEL
      toState(orderLucas.state) mustBe OrderState.ON_TRAVEL
      toState(orderGerman.state) mustBe OrderState.ON_TRAVEL

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

      toState(orderMarcelo.state) mustBe OrderState.DELIVERED
      toState(orderLucas.state) mustBe OrderState.DELIVERED
      toState(orderGerman.state) mustBe OrderState.DELIVERED

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
