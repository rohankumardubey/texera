package edu.uci.ics.texera.web

import java.time.{LocalDateTime, Duration => JDuration}
import akka.actor.Cancellable
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.RUNNING

import scala.concurrent.duration.{DurationInt, FiniteDuration}

class WorkflowLifecycleManager(wid: String, cleanUpTimeout: Int, cleanUpCallback: () => Unit)
    extends LazyLogging {
  private var userCount = 0
  private var cleanUpJob: Cancellable = Cancellable.alreadyCancelled

  private[this] def setCleanUpDeadline(status: WorkflowAggregatedState): Unit = {
    synchronized {
      if (userCount > 0 || status == RUNNING) {
        cleanUpJob.cancel()
        logger.info(
          s"[$wid] workflow state clean up postponed. current user count = $userCount, workflow status = $status"
        )
      } else {
        refreshDeadline()
      }
    }
  }

  private[this] def refreshDeadline(): Unit = {
    if (cleanUpJob.isCancelled || cleanUpJob.cancel()) {
      logger.info(
        s"[$wid] workflow state clean up will start at ${LocalDateTime.now().plus(JDuration.ofSeconds(cleanUpTimeout))}"
      )
      cleanUpJob = TexeraWebApplication.scheduleCallThroughActorSystem(cleanUpTimeout.seconds) {
        cleanUp()
      }
    }
  }

  private[this] def cleanUp(): Unit = {
    synchronized {
      if (userCount > 0) {
        // do nothing
        logger.info(s"[$wid] workflow state clean up failed. current user count = $userCount")
      } else {
        cleanUpJob.cancel()
        cleanUpCallback()
        logger.info(s"[$wid] workflow state clean up completed.")
      }
    }
  }

  def increaseUserCount(): Unit = {
    synchronized {
      userCount += 1
      cleanUpJob.cancel()
      logger.info(s"[$wid] workflow state clean up postponed. current user count = $userCount")
    }
  }

  def decreaseUserCount(currentWorkflowState: WorkflowAggregatedState): Unit = {
    synchronized {
      userCount -= 1
      if (userCount == 0 && currentWorkflowState != RUNNING) {
        refreshDeadline()
      } else {
        logger.info(s"[$wid] workflow state clean up postponed. current user count = $userCount")
      }
    }
  }

  def registerCleanUpOnStateChange(stateStore: WorkflowStateStore): Unit = {
    cleanUpJob.cancel()
    stateStore.jobStateStore.onChanged((_, newState) => setCleanUpDeadline(newState.state))
  }

}
