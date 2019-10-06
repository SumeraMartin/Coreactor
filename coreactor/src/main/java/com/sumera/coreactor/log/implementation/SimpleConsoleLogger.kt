package com.sumera.coreactor.log.implementation

import android.util.Log
import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.reducer.Reducer
import com.sumera.coreactor.contract.state.State
import com.sumera.coreactor.contract.event.Event

class SimpleConsoleLogger<STATE : State>(private val tag: String) : NoOpLogger<STATE>() {

    override fun onNewStateReceived(state: STATE) {
        log("State: $state")
    }

    override fun onAction(action: Action<STATE>) {
        log("Action: $action")
    }

    override fun onReducer(oldState: STATE, reducer: Reducer<STATE>, newState: STATE) {
        log("Reducer <<<<<<")
        log("Reducer old state: $oldState")
        log("Reducer: $reducer")
        log("Reducer new state: $newState")
        log("Reducer >>>>>>")
    }

    override fun onEventDispatchedToView(event: Event<STATE>) {
        log("Event: $event")
    }

    private fun log(message: String) {
        Log.d(tag, message)
    }
}
