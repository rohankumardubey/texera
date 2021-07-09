"""
 if (stateManager.confirmState(Running(), Ready())) {
      pauseManager.pause()
      dataProcessor.disableDataQueue()
      stateManager.transitTo(Paused())
    }
    stateManager.getCurrentState
"""
from core.architecture.handlers.handler import Handler
from core.architecture.manager.context import Context
from edu.uci.ics.amber.engine.architecture.worker import PauseWorker, WorkerStateInfo
from edu.uci.ics.amber.engine.common import Running, Ready, Paused


class PauseWorkerHandler(Handler):
    def __call__(self, context: Context, command: PauseWorker, *args, **kwargs):
        if context.state_manager.confirm_states([Running(), Ready()]):
            context.pause_manager.pause()
            # dataProcessor.disableDataQueue()
            context.state_manager.transit_to(Paused())
        state = context.state_manager.get_current_state()
        return WorkerStateInfo(state)
