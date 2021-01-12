package edu.uci.ics.texera.web.model.event

import com.fasterxml.jackson.databind.node.ObjectNode
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.WorkflowCompleted
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.workflow.WorkflowCompiler
import edu.uci.ics.texera.workflow.operators.visualization.VisualizationOperator

import scala.collection.mutable

object OperatorResult {
  def getChartType(operatorID: String, workflowCompiler: WorkflowCompiler): Option[String] = {
    // add chartType to result
    val precedentOpID =
      workflowCompiler.workflowInfo.links.find(link => link.destination == operatorID).get.origin
    val precedentOp =
      workflowCompiler.workflowInfo.operators.find(op => op.operatorID == precedentOpID).get
    precedentOp match {
      case operator: VisualizationOperator => Option.apply(operator.chartType())
      case _                               => Option.empty
    }
  }
}

case class OperatorResult(
    operatorID: String,
    table: List[ObjectNode],
    chartType: Option[String],
    totalRowCount: Int
)

object WorkflowCompletedEvent {
  val defaultPageSize = 10

  // transform results in amber tuple format to the format accepted by frontend
  def apply(
      workflowCompleted: WorkflowCompleted,
      workflowCompiler: WorkflowCompiler
  ): WorkflowCompletedEvent = {
    val resultList = new mutable.MutableList[OperatorResult]
    workflowCompleted.result.foreach(pair => {
      val operatorID = pair._1
      val chartType = OperatorResult.getChartType(operatorID, workflowCompiler)
      val table = chartType match {
        case Some(_) =>
          pair._2.map(tuple => tuple.asInstanceOf[Tuple].asKeyValuePairJson())
        case None =>
          pair._2
            .slice(0, defaultPageSize)
            .map(tuple => tuple.asInstanceOf[Tuple].asKeyValuePairJson())
      }
      resultList += OperatorResult(operatorID, table, chartType, pair._2.length)
    })
    WorkflowCompletedEvent(resultList.toList)
  }
}

case class WorkflowCompletedEvent(result: List[OperatorResult]) extends TexeraWebSocketEvent
