package service

import com.google.inject.Inject
import dao.OrderDAO
import model.Order

class OrderService @Inject()(val dao: OrderDAO) extends Service[Order]