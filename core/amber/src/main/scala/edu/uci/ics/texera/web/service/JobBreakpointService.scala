package edu.uci.ics.texera.web.service

import edu.uci.ics.amber.engine.architecture.breakpoint.globalbreakpoint.{
  ConditionalGlobalBreakpoint,
  CountGlobalBreakpoint
}
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.BreakpointTriggered
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.AssignBreakpointHandler.AssignGlobalBreakpoint
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.texera.web.{SubscriptionManager, WebsocketOutput, WorkflowStateStore}
import edu.uci.ics.texera.web.model.websocket.event.{BreakpointTriggeredEvent, TexeraWebSocketEvent}
import edu.uci.ics.texera.web.model.websocket.event.python.PythonPrintTriggeredEvent
import edu.uci.ics.texera.web.workflowruntimestate.{
  BreakpointEvent,
  OperatorBreakpoints,
  OperatorRuntimeStats,
  PythonOperatorInfo
}
import edu.uci.ics.texera.web.workflowruntimestate.BreakpointEvent.BreakpointTuple
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.PAUSED
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.workflow.{
  Breakpoint,
  BreakpointCondition,
  ConditionBreakpoint,
  CountBreakpoint
}
import rx.lang.scala.Subject

class JobBreakpointService(
    client: AmberClient,
    stateStore: WorkflowStateStore,
    wsOutput: WebsocketOutput
) extends SubscriptionManager {

  registerCallbackOnBreakpoint()

  addSubscription(stateStore.breakpointStore.onChanged((oldState, newState) => {
    newState.operatorInfo.foreach {
      case (opId, info) =>
        val oldInfo = oldState.operatorInfo.getOrElse(opId, new OperatorBreakpoints())
        if (
          info.unresolvedBreakpoints.nonEmpty && info.unresolvedBreakpoints != oldInfo.unresolvedBreakpoints
        ) {
          wsOutput.onNext(BreakpointTriggeredEvent(info.unresolvedBreakpoints, opId))
        }
    }
  }))

  /** *
    *  Callback Functions to register upon construction
    */
  private[this] def registerCallbackOnBreakpoint(): Unit = {
    addSubscription(
      client
        .registerCallback[BreakpointTriggered]((evt: BreakpointTriggered) => {
          stateStore.breakpointStore.updateState { jobInfo =>
            val breakpointEvts = evt.report
              .filter(_._1._2 != null)
              .map { elem =>
                val actorPath = elem._1._1.toString
                val faultedTuple = elem._1._2
                val tupleList =
                  if (faultedTuple.tuple != null) {
                    faultedTuple.tuple.toArray().filter(v => v != null).map(v => v.toString).toList
                  } else {
                    List.empty
                  }
                BreakpointEvent(
                  actorPath,
                  Some(BreakpointTuple(faultedTuple.id, faultedTuple.isInput, tupleList)),
                  elem._2
                )
              }
              .toArray
            val newInfo = jobInfo.operatorInfo
              .getOrElse(evt.operatorID, OperatorBreakpoints())
              .withUnresolvedBreakpoints(breakpointEvts)
            jobInfo.addOperatorInfo((evt.operatorID, newInfo))
          }
          stateStore.jobStateStore.updateState { oldState =>
            oldState.withState(PAUSED)
          }
        })
    )
  }

  def clearTriggeredBreakpoints(): Unit = {
    stateStore.breakpointStore.updateState { jobInfo =>
      jobInfo.withOperatorInfo(
        jobInfo.operatorInfo.map(pair => pair._1 -> pair._2.withUnresolvedBreakpoints(Seq.empty))
      )
    }
  }

  def addBreakpoint(operatorID: String, breakpoint: Breakpoint): Unit = {
    val breakpointID = "breakpoint-" + operatorID + "-" + System.currentTimeMillis()
    breakpoint match {
      case conditionBp: ConditionBreakpoint =>
        val column = conditionBp.column
        val predicate: Tuple => Boolean = conditionBp.condition match {
          case BreakpointCondition.EQ =>
            tuple => {
              tuple.getField(column).toString.trim == conditionBp.value
            }
          case BreakpointCondition.LT =>
            tuple => tuple.getField(column).toString.trim < conditionBp.value
          case BreakpointCondition.LE =>
            tuple => tuple.getField(column).toString.trim <= conditionBp.value
          case BreakpointCondition.GT =>
            tuple => tuple.getField(column).toString.trim > conditionBp.value
          case BreakpointCondition.GE =>
            tuple => tuple.getField(column).toString.trim >= conditionBp.value
          case BreakpointCondition.NE =>
            tuple => tuple.getField(column).toString.trim != conditionBp.value
          case BreakpointCondition.CONTAINS =>
            tuple => tuple.getField(column).toString.trim.contains(conditionBp.value)
          case BreakpointCondition.NOT_CONTAINS =>
            tuple => !tuple.getField(column).toString.trim.contains(conditionBp.value)
        }

        client.sendSync(
          AssignGlobalBreakpoint(
            new ConditionalGlobalBreakpoint(
              breakpointID,
              tuple => {
                val texeraTuple = tuple.asInstanceOf[Tuple]
                predicate.apply(texeraTuple)
              }
            ),
            operatorID
          )
        )
      case countBp: CountBreakpoint =>
        client.sendSync(
          AssignGlobalBreakpoint(new CountGlobalBreakpoint(breakpointID, countBp.count), operatorID)
        )
    }
  }

}