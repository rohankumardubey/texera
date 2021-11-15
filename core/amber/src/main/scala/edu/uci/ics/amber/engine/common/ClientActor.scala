package edu.uci.ics.amber.engine.common

import akka.actor.{Actor, ActorRef}
import edu.uci.ics.amber.engine.architecture.controller.{Controller, ControllerConfig, Workflow}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{
  NetworkAck,
  NetworkMessage
}
import edu.uci.ics.amber.engine.common.ClientActor.{
  ClosureRequest,
  CommandRequest,
  InitializeRequest,
  ObservableRequest
}
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowControlMessage
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

import scala.collection.mutable

object ClientActor {
  case class InitializeRequest(workflow: Workflow, controllerConfig: ControllerConfig)
  case class CommandRequest[T](controlCommand: ControlCommand[T])
  case class ObservableRequest(pf: PartialFunction[Any, Unit])
  case class ClosureRequest[T](closure: () => T)
}

class ClientActor extends Actor {
  var controller: ActorRef = _
  var controlId = 0L
  val senderMap = new mutable.LongMap[ActorRef]()
  var handlers: PartialFunction[Any, Unit] = PartialFunction.empty

  override def receive: Receive = {
    case InitializeRequest(workflow, controllerConfig) =>
      assert(controller == null)
      controller = context.actorOf(Controller.props(workflow, controllerConfig))
      sender ! ()
    case ClosureRequest(closure) =>
      try {
        sender ! closure()
      } catch {
        case e: Throwable =>
          sender ! e
      }
    case CommandRequest(controlCommand) =>
      controller ! ControlInvocation(controlId, controlCommand)
      senderMap(controlId) = sender
      controlId += 1
    case req: ObservableRequest =>
      handlers = req.pf orElse handlers
      sender ! scala.runtime.BoxedUnit.UNIT
    case NetworkMessage(
          mId,
          _ @WorkflowControlMessage(_, _, _ @ReturnInvocation(originalCommandID, controlReturn))
        ) =>
      sender ! NetworkAck(mId)
      if (handlers.isDefinedAt(controlReturn)) {
        handlers(controlReturn)
      }
      if (senderMap.contains(originalCommandID)) {
        senderMap(originalCommandID) ! controlReturn
        senderMap.remove(originalCommandID)
      }
    case NetworkMessage(mId, _ @WorkflowControlMessage(_, _, _ @ControlInvocation(_, command))) =>
      sender ! NetworkAck(mId)
      if (handlers.isDefinedAt(command)) {
        handlers(command)
      }
    case other =>
      println(other) //skip
  }
}
