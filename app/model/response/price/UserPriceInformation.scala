package model.response.price

import model.internal.UserDataOrder

case class UserPriceInformation(user: UserDataOrder, routePriceInformation: List[RoutePriceResponse])
