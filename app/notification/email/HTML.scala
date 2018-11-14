package notification.email

object HTML {
  //Templates IDs of SendGrid
  val CREATED = "d-797ddf8d62b0424389b9f07c2801debc"
  val CONFIRMED_PARTICIPANT = "d-450393704fc940ce8b8ffd1260b25952"
  val WITH_CARRIER = "d-fc3273e9834448e7b2e9d63b93bfcf0a"
  val WITH_CARRIER_WITHOUT_QR = "d-ad04fdb089f84d23aa48aeb203d357d5"
  val FINALIZED = "d-f548a13e9841465c9c285e492aae7dde"
  val TRAVELLING = "d-7880a5139b7344be8f88be9e454d8103"
  val CANCELED = "d-b0c70f5487da4822b41d905fd8516e66"

  //Only for carrier
  val ASSIGNED = "d-df87f0c52e5b4bb88e1227e57ad0cf75"
  val CARRIER_FINALIZED = "d-ef51f9f3d2294ae49ff8d50f3f936838"
  val CARRIER_CANCELED = "d-fb770261b1c744389034c14bb6f13c6b"

  //End
  val END = "d-f0b6eb72433b49edae57b2a96785a780"
}
