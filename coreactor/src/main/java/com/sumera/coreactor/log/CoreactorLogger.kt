package com.sumera.coreactor.log

import com.sumera.coreactor.LifecycleState
import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.reducer.Reducer
import com.sumera.coreactor.contract.state.State
import com.sumera.coreactor.contract.event.Event

interface CoreactorLogger<STATE : State> {

    fun onNewStateReceived(state: STATE)

    fun onStateDispatchedToView(state: STATE)

    fun onAction(action: Action<STATE>)

    fun onLifecycle(lifecycleState: LifecycleState)

    fun onReducer(oldState: STATE, reducer: Reducer<STATE>, newState: STATE)

    fun onEventEmitted(event: Event<STATE>)

    fun onEventDispatchedToView(event: Event<STATE>)

    fun onEventThrownAway(event: Event<STATE>)

    fun onEventWaitingForStartedView(event: Event<STATE>)

    fun onEventWaitingForCreatedView(event: Event<STATE>)
}
