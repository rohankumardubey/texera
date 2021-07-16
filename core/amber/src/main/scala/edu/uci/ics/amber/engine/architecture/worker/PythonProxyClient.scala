package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.sendsemantics.datatransferpolicy.{
  DataSendingPolicy,
  OneToOnePolicy,
  RoundRobinPolicy
}
import edu.uci.ics.amber.engine.architecture.sendsemantics.datatransferpolicy2
import edu.uci.ics.amber.engine.architecture.worker.PythonProxyClient.communicate
import edu.uci.ics.amber.engine.architecture.worker.WorkerBatchInternalQueue.{
  ControlElement,
  ControlElementV2,
  DataElement
}
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.AddOutputPolicyHandler.AddOutputPolicy
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.PauseHandler.PauseWorker
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.QueryStatisticsHandler.QueryStatistics
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.ResumeHandler.ResumeWorker
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.StartHandler.StartWorker
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.UpdateInputLinkingHandler.UpdateInputLinking
import edu.uci.ics.amber.engine.common.ambermessage.{ControlPayload, DataFrame, EndOfUpstream}
import edu.uci.ics.amber.engine.common.ambermessage2.WorkflowControlMessage
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnPayload}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.{IOperatorExecutor, ambermessage2}
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import org.apache.arrow.flight._
import org.apache.arrow.memory.{BufferAllocator, RootAllocator}
import org.apache.arrow.vector.VectorSchemaRoot

import java.nio.charset.StandardCharsets
import scala.collection.mutable

object MSG extends Enumeration {
  type MSGType = Value
  val HEALTH_CHECK: Value = Value
}

object PythonProxyClient {

  private def communicate(client: FlightClient, message: String): Array[Byte] =
    client.doAction(new Action(message)).next.getBody
}

case class PythonProxyClient(portNumber: Int, operator: IOperatorExecutor)
    extends Runnable
    with AutoCloseable
    with WorkerBatchInternalQueue {

  val allocator: BufferAllocator =
    new RootAllocator().newChildAllocator("flight-server", 0, Long.MaxValue);
  val location: Location = Location.forGrpcInsecure("localhost", portNumber)

  private val MAX_TRY_COUNT: Int = 3
  private val WAIT_TIME_MS = 500
  var schemaRoot: VectorSchemaRoot = _
  private var flightClient: FlightClient = _

  override def run(): Unit = {
    establishConnection()
    mainLoop()
  }

  def establishConnection(): Unit = {
    var connected = false
    var tryCount = 0
    while (!connected && tryCount < MAX_TRY_COUNT) {
      try {
        println("trying to connect to " + location)
        flightClient = FlightClient.builder(allocator, location).build()
        connected =
          new String(communicate(flightClient, "health_check"), StandardCharsets.UTF_8) == "ack"
        if (!connected) Thread.sleep(WAIT_TIME_MS)
      } catch {
        case e: FlightRuntimeException =>
          System.out.println("Flight CLIENT:\tNot connected to the server in this try.")
          flightClient.close()
          Thread.sleep(WAIT_TIME_MS)
          tryCount += 1
      }
      if (tryCount == MAX_TRY_COUNT)
        throw new RuntimeException(
          "Exceeded try limit of " + MAX_TRY_COUNT + " when connecting to Flight Server!"
        )
    }
  }

  def mainLoop(): Unit = {
    while (true) {

      getElement match {
        case DataElement(dataPayload, from) =>
          dataPayload match {
            case DataFrame(frame) =>
              val tuples = mutable.Queue(frame.map((t: ITuple) => t.asInstanceOf[Tuple]): _*)
              writeArrowStream(flightClient, tuples, 100, from)
            case EndOfUpstream() =>
              val q = mutable.Queue(
                Tuple
                  .newBuilder(
                    edu.uci.ics.texera.workflow.common.tuple.schema.Schema.newBuilder().build()
                  )
                  .build()
              )
              writeArrowStream(flightClient, q, 100, from)
          }
        case ControlElement(cmd, from) =>
          sendControl(cmd, from)
        case ControlElementV2(cmd, from) =>
          sendControl(cmd.asInstanceOf[ambermessage2.ControlInvocation], from)

      }
    }

  }

  def sendControl(cmd: ControlPayload, from: ActorVirtualIdentity): Unit = {
    cmd match {
      case ControlInvocation(commandID: Long, command: ControlCommand[_]) =>
        println(" JAVA send command " + commandID + " " + command)
        command match {
          case AddOutputPolicy(policy: DataSendingPolicy) =>
            var protobufPolicy: datatransferpolicy2.DataSendingPolicy = null
            policy match {
              case _: OneToOnePolicy =>
                protobufPolicy = datatransferpolicy2.OneToOnePolicy(
                  Option(policy.policyTag),
                  policy.batchSize,
                  policy.receivers
                )

              case _: RoundRobinPolicy =>
                protobufPolicy = datatransferpolicy2.RoundRobinPolicy(
                  Option(policy.policyTag),
                  policy.batchSize,
                  policy.receivers
                )
              case _ => throw new UnsupportedOperationException("not supported data policy")
            }
            send(from, commandID, promisehandler2.AddOutputPolicy(protobufPolicy))

          case UpdateInputLinking(identifier, inputLink) =>
            send(
              from,
              commandID,
              promisehandler2.UpdateInputLinking(
                identifier = identifier,
                inputLink = Option(inputLink)
              )
            )
          case QueryStatistics() =>
            send(from, commandID, promisehandler2.QueryStatistics())
          case PauseWorker() =>
            send(from, commandID, promisehandler2.PauseWorker())
          case ResumeWorker() =>
            send(from, commandID, promisehandler2.ResumeWorker())
          case StartWorker() =>
            send(from, commandID, promisehandler2.StartWorker())
        }
      case ReturnPayload(originalCommandID, _) =>
        println("JAVA receive return payload " + originalCommandID )
    }
  }

  def sendControl(cmd: ambermessage2.ControlInvocation, from: ActorVirtualIdentity): Unit =
    send(from, cmd.commandID, cmd.command)

  def send(
      from: ActorVirtualIdentity,
      commandID: Long,
      commandV2: promisehandler2.ControlCommand
  ): Result = {
    val controlMessage =
      toWorkflowControlMessage2(from, commandID, commandV2)
    val action: Action = new Action("control", controlMessage.toByteArray)
    flightClient.doAction(action).next()
  }

  def toWorkflowControlMessage2(
      from: ActorVirtualIdentity,
      commandID: Long,
      controlCommand: promisehandler2.ControlCommand
  ): WorkflowControlMessage = {
    toWorkflowControlMessage2(from, toControlInvocation2(commandID, controlCommand))
  }

  def toWorkflowControlMessage2(
      from: ActorVirtualIdentity,
      controlPayload: ambermessage2.ControlPayload
  ): WorkflowControlMessage = {
    ambermessage2.WorkflowControlMessage(
      from = from,
      sequenceNumber = 0L,
      payload = controlPayload
    )
  }

  def toControlInvocation2(
      commandID: Long,
      controlCommand: promisehandler2.ControlCommand
  ): ambermessage2.ControlInvocation = {
    ambermessage2.ControlInvocation(commandID = commandID, command = controlCommand)
  }

  override def close(): Unit = {

    val action: Action = new Action("shutdown", "".getBytes)
    flightClient.doAction(action) // do not expect reply
    flightClient.close()
  }

  private def writeArrowStream(
      client: FlightClient,
      values: mutable.Queue[Tuple],
      chunkSize: Int = 100,
      from: ActorVirtualIdentity
  ): Unit = {
    if (values.nonEmpty) {
      val cachedTuple = values.front
      val schema = cachedTuple.getSchema
      val arrowSchema = ArrowUtils.fromTexeraSchema(schema)
      val flightListener = new SyncPutListener

      val schemaRoot = VectorSchemaRoot.create(arrowSchema, allocator)
      val writer =
        client.startPut(
          FlightDescriptor.command(from.asMessage.toByteArray),
          schemaRoot,
          flightListener
        )

      try {
        while (values.nonEmpty) {
          schemaRoot.allocateNew()
          while (schemaRoot.getRowCount < chunkSize && values.nonEmpty)
            ArrowUtils.appendTexeraTuple(values.dequeue(), schemaRoot)
          writer.putNext()
          schemaRoot.clear()
        }
        writer.completed()
        flightListener.getResult()
        flightListener.close()
        schemaRoot.clear()

      } catch {
        case e: Exception =>
          e.printStackTrace()
      }
    }

  }

}
