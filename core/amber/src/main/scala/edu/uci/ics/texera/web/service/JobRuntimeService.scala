package edu.uci.ics.texera.web.service

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.EvaluatePythonExpressionHandler.EvaluatePythonExpression
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.PauseHandler.PauseWorkflow
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.ResumeHandler.ResumeWorkflow
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.texera.Utils
import edu.uci.ics.texera.web.{
  SubscriptionManager,
  WebsocketInput,
  WebsocketOutput,
  WorkflowStateStore
}
import edu.uci.ics.texera.web.model.websocket.event.{
  TexeraWebSocketEvent,
  WorkflowExecutionErrorEvent,
  WorkflowStateEvent
}
import edu.uci.ics.texera.web.model.websocket.request.python.PythonExpressionEvaluateRequest
import edu.uci.ics.texera.web.model.websocket.request.{
  RemoveBreakpointRequest,
  SkipTupleRequest,
  WorkflowKillRequest,
  WorkflowPauseRequest,
  WorkflowResumeRequest
}
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState._

class JobRuntimeService(
    client: AmberClient,
    stateStore: WorkflowStateStore,
    wsInput: WebsocketInput,
    wsOutput: WebsocketOutput,
    breakpointService: JobBreakpointService
) extends SubscriptionManager
    with LazyLogging {

  addSubscription(stateStore.jobStateStore.onChanged((oldState, newState) => {
    // Update workflow state
    if (newState.state != oldState.state) {
      wsOutput.onNext(WorkflowStateEvent(Utils.aggregatedStateToString(newState.state)))
    }
    // Check if new error occurred
    if (newState.error != oldState.error) {
      wsOutput.onNext(WorkflowExecutionErrorEvent(newState.error))
    }
  }))

  //Receive skip tuple
  addSubscription(wsInput.subscribe((req: SkipTupleRequest, uidOpt) => {
    throw new RuntimeException("skipping tuple is temporarily disabled")
  }))

  // Receive Pause
  addSubscription(wsInput.subscribe((req: WorkflowPauseRequest, uidOpt) => {
    val f = client.sendAsync(PauseWorkflow())
    stateStore.jobStateStore.updateState(jobInfo => jobInfo.withState(PAUSING))
    f.onSuccess { _ =>
      stateStore.jobStateStore.updateState(jobInfo => jobInfo.withState(PAUSED))
    }
  }))

  // Receive Resume
  addSubscription(wsInput.subscribe((req: WorkflowResumeRequest, uidOpt) => {
    breakpointService.clearTriggeredBreakpoints()
    val f = client.sendAsync(ResumeWorkflow())
    stateStore.jobStateStore.updateState(jobInfo => jobInfo.withState(RESUMING))
    f.onSuccess { _ =>
      stateStore.jobStateStore.updateState(jobInfo => jobInfo.withState(RUNNING))
    }
  }))

  // Receive Kill
  addSubscription(wsInput.subscribe((req: WorkflowKillRequest, uidOpt) => {
    client.shutdown()
    stateStore.jobStateStore.updateState(jobInfo => jobInfo.withState(COMPLETED))
  }))

  // Receive evaluate python expression
  addSubscription(wsInput.subscribe((req: PythonExpressionEvaluateRequest, uidOpt) => {
    client.sendSync(EvaluatePythonExpression(req.expression, req.operatorId))
  }))

}
