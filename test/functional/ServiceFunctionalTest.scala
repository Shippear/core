package functional

import embbebedmongo.MongoTest
import model.internal.OrderState._
import model.internal.{AssignCarrier, OrderState, OrderToValidate, UserType}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.test.Helpers.{await, _}
import service.Exception.ShippearException
import service.{OrderService, UserService}

class ServiceFunctionalTest extends MongoTest with GuiceOneServerPerSuite with ModelData {


  val orderService = app.injector.instanceOf(classOf[OrderService])
  val userService = app.injector.instanceOf(classOf[UserService])

  "Functional Test" should {

    "have full order flow consistency" in {

      // 1. Creating users and orders
      await(userService.create(marcelo))
      await(userService.create(lucas))
      await(orderService.createOrder(orderWithoutCarrier))

      var order = await(orderService.findById(orderWithoutCarrier._id))
      var userMarcelo = await(userService.findById(marcelo._id))
      var userLucas = await(userService.findById(lucas._id))

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
        userService.update(marceloNewAddressFail)
      }

      //---------

      //TODO lo mejor seria armar un servicio para esto?
      // 2. The participant accepts the order
      order = order.copy(state = OrderState.PENDING_CARRIER)
      await(orderService.update(order))

      order = await(orderService.findById(order._id))
      userLucas = await(userService.findById(lucas._id))
      userMarcelo = await(userService.findById(marcelo._id))

      toState(order.state) mustEqual OrderState.PENDING_CARRIER
      var orderLucas = userLucas.orders.get.head
      var orderMarcelo = userMarcelo.orders.get.head

      orderLucas mustBe order
      orderMarcelo mustBe order

      //---------

      //TODO agregar validacion de que al asignar el carrier, la orden tenga state PENDING_CARRIER
      // 3. Assigning carrier German
      await(userService.create(german))

      val assignCarrier = AssignCarrier(orderWithoutCarrier._id, german._id)
      order = await(orderService.assignCarrier(assignCarrier))

      toState(order.state) mustBe OrderState.PENDING_PICKUP
      order.applicantId mustBe marcelo._id
      order.participantId mustBe lucas._id
      order.carrierId.get mustBe german._id
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

      // TODO al verificar el QR, si lo hace el CARRIER, entonces tiene que cambiar el estado a ON_TRAVEL
      // 4. Verification code

      val validateCarrier = OrderToValidate(order._id, german._id, UserType.CARRIER)
      val validationCarrier = await(orderService.validateQrCode(validateCarrier))
      validationCarrier mustBe true

      val validateApplicant = OrderToValidate(order._id, marcelo._id, UserType.APPLICANT)
      val validationApplicant = await(orderService.validateQrCode(validateApplicant))
      validationApplicant mustBe true

      val validateParticipant = OrderToValidate(order._id, lucas._id, UserType.PARTICIPANT)
      val validationParticipant = await(orderService.validateQrCode(validateParticipant))
      validationParticipant mustBe true


      // Fail cases
      val validationFail = OrderToValidate(order._id, nazareno._id, UserType.CARRIER)
      await(orderService.validateQrCode(validationFail)) mustBe false
      await(orderService.validateQrCode(validationFail.copy(userType = UserType.PARTICIPANT))) mustBe false
      await(orderService.validateQrCode(validationFail.copy(userType = UserType.APPLICANT))) mustBe false


      //TODO este update no deberia estar mas si se hace lo de arriba!
      await(orderService.update(order.copy(state = OrderState.ON_TRAVEL)))


      //TODO Hacer un servcio para esto?, Habria que tener en cuenta al actualizar el "score"
      // 5. Order delivered
      order = order.copy(state = OrderState.DELIVERED)
      await(orderService.update(order))

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




    }

  }



}
