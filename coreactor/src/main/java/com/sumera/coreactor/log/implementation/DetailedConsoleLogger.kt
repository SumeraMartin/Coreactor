package com.sumera.coreactor.log.implementation

import android.util.Log
import com.sumera.coreactor.LifecycleState
import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.reducer.Reducer
import com.sumera.coreactor.contract.state.State
import com.sumera.coreactor.contract.event.Event
import com.sumera.coreactor.log.CoreactorLogger

class DetailedConsoleLogger<STATE : State>(private val tag: String) : CoreactorLogger<STATE> {

    override fun onNewStateReceived(state: STATE) {
        log("State received: $state")
    }

    override fun onStateDispatchedToView(state: STATE) {
        log("State dispatched: $state")
    }

    override fun onAction(action: Action<STATE>) {
        log("Action: $action")
    }

    override fun onLifecycle(lifecycleState: LifecycleState) {
        log("Lifecycle: $lifecycleState")
    }

    override fun onReducer(oldState: STATE, reducer: Reducer<STATE>, newState: STATE) {
        log("Reducer <<<<<<")
        log("Reducer old state: $oldState")
        log("Reducer: $reducer")
        log("Reducer new state: $newState")
        log("Reducer >>>>>>")
    }

    override fun onEventEmitted(event: Event<STATE>) {
        log("Event emitted: $event")
    }

    override fun onEventDispatchedToView(event: Event<STATE>) {
        log("Event dispatched: $event")
    }

    override fun onEventThrownAway(event: Event<STATE>) {
        log("Event thrown away: $event")
    }

    override fun onEventWaitingForStartedView(event: Event<STATE>) {
        log("Event waiting for started view: $event")
    }

    override fun onEventWaitingForCreatedView(event: Event<STATE>) {
        log("Event waiting for created view: $event")
    }

    private fun log(message: String) {
        Log.d(tag, message)
    }
}
