package model.internal

case class Route(origin: Address,
                 destination: Address,
                 auxOrigin: Option[MinimalAddress])

