package edu.uci.ics.texera.web.service

import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{
  WorkflowCompleted,
  WorkflowStatusUpdate
}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.texera.Utils
import edu.uci.ics.texera.web.{SubscriptionManager, WebsocketOutput, WorkflowStateStore}
import edu.uci.ics.texera.web.model.websocket.event.{
  OperatorStatistics,
  OperatorStatisticsUpdateEvent,
  TexeraWebSocketEvent
}
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.{ABORTED, COMPLETED}
import rx.lang.scala.{Subject, Subscription}

class JobStatsService(
    client: AmberClient,
    stateStore: WorkflowStateStore,
    wsOutput: WebsocketOutput
) extends SubscriptionManager {

  registerCallbacks()

  addSubscription(stateStore.statsStore.onChanged((oldState, newState) => {
    // Update operator stats if any operator updates its stat
    if (newState.operatorInfo.toSet != oldState.operatorInfo.toSet) {
      wsOutput.onNext(
        OperatorStatisticsUpdateEvent(newState.operatorInfo.collect {
          case x =>
            val stats = x._2
            val res = OperatorStatistics(
              Utils.aggregatedStateToString(stats.state),
              stats.inputCount,
              stats.outputCount
            )
            (x._1, res)
        })
      )
    }
  }))

  private[this] def registerCallbacks(): Unit = {
    registerCallbackOnWorkflowStatusUpdate()
    registerCallbackOnWorkflowComplete()
    registerCallbackOnFatalError()
  }

  private[this] def registerCallbackOnWorkflowStatusUpdate(): Unit = {
    addSubscription(
      client
        .registerCallback[WorkflowStatusUpdate]((evt: WorkflowStatusUpdate) => {
          stateStore.statsStore.updateState { jobInfo =>
            jobInfo.withOperatorInfo(evt.operatorStatistics)
          }
        })
    )
  }

  private[this] def registerCallbackOnWorkflowComplete(): Unit = {
    addSubscription(
      client
        .registerCallback[WorkflowCompleted]((evt: WorkflowCompleted) => {
          client.shutdown()
          stateStore.jobStateStore.updateState(jobInfo => jobInfo.withState(COMPLETED))
        })
    )
  }

  private[this] def registerCallbackOnFatalError(): Unit = {
    addSubscription(
      client
        .registerCallback[FatalError]((evt: FatalError) => {
          client.shutdown()
          stateStore.jobStateStore.updateState { jobInfo =>
            jobInfo.withState(ABORTED).withError(evt.e.getLocalizedMessage)
          }
        })
    )
  }
}
