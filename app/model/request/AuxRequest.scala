package model.request

import model.internal.MinimalAddress

case class AuxRequest(orderId: String, carrierId: String, auxAddress: MinimalAddress)
