package com.sumera.coreactor.interceptor.implementation

import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.event.Event
import com.sumera.coreactor.contract.reducer.Reducer
import com.sumera.coreactor.contract.state.State
import com.sumera.coreactor.interceptor.CoreactorInterceptor
import com.sumera.coreactor.lifecycle.LifecycleState

class NoOpInterceptor<STATE : State> : CoreactorInterceptor<STATE> {

    override fun onInterceptState(state: STATE): STATE {
        return state
    }

    override fun onInterceptAction(action: Action<STATE>): Action<STATE> {
        return action
    }

    override fun onInterceptLifecycleAction(lifecycleState: LifecycleState): LifecycleState {
        return lifecycleState
    }

    override fun onInterceptReducer(reducer: Reducer<STATE>): Reducer<STATE> {
        return reducer
    }

    override fun onInterceptEvent(event: Event<STATE>): Event<STATE> {
        return event
    }
}
