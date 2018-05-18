package task

import akka.actor.ActorSystem
import javax.inject.Inject

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class TaskManager @Inject()(actorSystem: ActorSystem)(implicit ec: ExecutionContext) {

  def addTask(task: RepetitveTask): Unit = {
    actorSystem.scheduler.schedule(task.initialDelay, task.interval) {
      task.function()
    }
  }

}

trait RepetitveTask {
  def initialDelay: FiniteDuration

  def interval: FiniteDuration

  def function: () => Unit
}

case class ShippearTask(initialDelay: FiniteDuration,
                        interval: FiniteDuration,
                        function: () => Unit) extends RepetitveTask

trait RepetitveAsyncTask {
  protected def taskManager: TaskManager

  protected def initialDelay: FiniteDuration

  protected def interval: FiniteDuration

  protected def doAsync(): Unit

  protected def asyncTask: RepetitveTask = ShippearTask(initialDelay, interval, () => doAsync())

  taskManager.addTask(asyncTask)
}