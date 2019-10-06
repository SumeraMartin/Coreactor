package com.sumera.coreactor.log.implementation

import com.sumera.coreactor.LifecycleState
import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.reducer.Reducer
import com.sumera.coreactor.contract.state.State
import com.sumera.coreactor.contract.event.Event
import com.sumera.coreactor.log.CoreactorLogger

open class NoOpLogger<STATE : State> : CoreactorLogger<STATE> {

    override fun onNewStateReceived(state: STATE) {
        // NoOp
    }

    override fun onStateDispatchedToView(state: STATE) {
        // NoOp
    }

    override fun onAction(action: Action<STATE>) {
        // NoOp
    }

    override fun onLifecycle(lifecycleState: LifecycleState) {
        // NoOp
    }

    override fun onReducer(oldState: STATE, reducer: Reducer<STATE>, newState: STATE) {
        // NoOp
    }

    override fun onEventEmitted(event: Event<STATE>) {
        // NoOp
    }

    override fun onEventDispatchedToView(event: Event<STATE>) {
        // NoOp
    }

    override fun onEventThrownAway(event: Event<STATE>) {
        // NoOp
    }

    override fun onEventWaitingForStartedView(event: Event<STATE>) {
        // NoOp
    }

    override fun onEventWaitingForCreatedView(event: Event<STATE>) {
        // NoOp
    }
}
