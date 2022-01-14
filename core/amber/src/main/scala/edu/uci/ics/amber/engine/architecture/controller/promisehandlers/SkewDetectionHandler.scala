package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.SkewDetectionHandler.{
  ControllerInitiateSkewDetection,
  getSkewedAndHelperWorkersEligibleForFirstPhase,
  previousCallFinished
}
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.WorkerWorkloadInfo
import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.texera.workflow.operators.hashJoin.HashJoinOpExecConfig

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.{break, breakable}

object SkewDetectionHandler {
  var previousCallFinished = true

  var skewedToHelperWorkerHistory =
    new mutable.HashMap[ActorVirtualIdentity, ActorVirtualIdentity]()

  final case class ControllerInitiateSkewDetection(
      filterByWorkers: List[ActorVirtualIdentity] = List()
  ) extends ControlCommand[Unit]

  /**
    * worker is eligible for first phase if no mitigation has happened till now or it is in second phase right now.
    *
    * @param worker
    * @return
    */
  def isEligibleForSkewed(worker: ActorVirtualIdentity): Boolean = {
    !skewedToHelperWorkerHistory.values.toList.contains(
      worker
    )
  }

  /**
    * worker is eligible for free if it is being used in neither of the phases.
    *
    * @param worker
    * @return
    */
  def isEligibleForHelper(worker: ActorVirtualIdentity): Boolean = {
    !skewedToHelperWorkerHistory.keySet.contains(
      worker
    )
  }

  def passSkewTest(
      skewedWorkerCand: ActorVirtualIdentity,
      helperWorkerCand: ActorVirtualIdentity,
      loads: mutable.HashMap[ActorVirtualIdentity, WorkerWorkloadInfo],
      etaThreshold: Int,
      tauThreshold: Int
  ): Boolean = {
    if (
      loads(
        skewedWorkerCand
      ).dataInputWorkload / Constants.defaultBatchSize > etaThreshold && (loads(
        skewedWorkerCand
      ).dataInputWorkload / Constants.defaultBatchSize > tauThreshold + loads(
        helperWorkerCand
      ).dataInputWorkload / Constants.defaultBatchSize)
    ) {
      return true
    }
    false
  }

  /** *
    * returns an array of (skewedWorker, freeWorker, whether state replication has to be done)
    */
  def getSkewedAndHelperWorkersEligibleForFirstPhase(
      loads: mutable.HashMap[ActorVirtualIdentity, WorkerWorkloadInfo]
  ): ArrayBuffer[(ActorVirtualIdentity, ActorVirtualIdentity, Boolean)] = {
    val retPairs = new ArrayBuffer[(ActorVirtualIdentity, ActorVirtualIdentity, Boolean)]()
    // Get workers in increasing load
    val sortedWorkers = loads.keys.toList.sortBy(loads(_).dataInputWorkload)

    for (i <- sortedWorkers.size - 1 to 0 by -1) {
      if (isEligibleForSkewed(sortedWorkers(i))) {
        // worker has been previously paired with some worker and that worker will be used again.
        if (skewedToHelperWorkerHistory.keySet.contains(sortedWorkers(i))) {
          if (
            passSkewTest(
              sortedWorkers(i),
              skewedToHelperWorkerHistory(sortedWorkers(i)),
              loads,
              Constants.reshapeEtaThreshold,
              Constants.reshapeTauThreshold
            )
          ) {
            // build table has already been replicated
            retPairs.append(
              (sortedWorkers(i), skewedToHelperWorkerHistory(sortedWorkers(i)), false)
            )
          }
        } else if (i > 0) {
          breakable {
            for (j <- 0 to i - 1) {
              if (
                isEligibleForHelper(sortedWorkers(j)) && passSkewTest(
                  sortedWorkers(i),
                  sortedWorkers(j),
                  loads,
                  Constants.reshapeEtaThreshold,
                  Constants.reshapeTauThreshold
                )
              ) {
                retPairs.append((sortedWorkers(i), sortedWorkers(j), true))
                skewedToHelperWorkerHistory(sortedWorkers(i)) = sortedWorkers(j)
                break
              }
            }
          }
        }
      }
    }

    retPairs
  }

}

trait SkewDetectionHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler((msg: ControllerInitiateSkewDetection, sender) => {
    if (!previousCallFinished) {
      Future.Done
    } else {
      previousCallFinished = false

      workflow.getAllOperators.foreach(opConfig => {
        if (opConfig.isInstanceOf[HashJoinOpExecConfig[Any]]) {
          // Skew handling is only for hash-join operator for now
          val skewedAndHelperPairs =
            getSkewedAndHelperWorkersEligibleForFirstPhase(opConfig.workerToWorkloadInfo)
        }
      })
      previousCallFinished = true
      Future.Done
    }
  })
}
