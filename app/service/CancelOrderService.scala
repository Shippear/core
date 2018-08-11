package service

import com.google.inject.Inject
import task.CancelOrdersTask

class CancelOrderService @Inject()(val cancelOrdersTask: CancelOrdersTask){

  def active(value: Boolean) = {
    cancelOrdersTask.setActivated_=(value)
    cancelOrdersTask.isActivated
  }
}
