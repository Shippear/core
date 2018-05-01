package model


case class User(userName: String,
                firstName: String,
                lastName: String,
                dni: String,
                contactInfo: ContactInfo,
                photoUrl: String,
                addresses: Seq[Address],
                order: Option[Order],
                paymentMethods: Seq[PaymentMethod])