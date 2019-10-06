package com.sumera.coreactor.interceptor

import com.sumera.coreactor.LifecycleState
import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.reducer.Reducer
import com.sumera.coreactor.contract.state.State
import com.sumera.coreactor.contract.event.Event

interface CoreactorInterceptor<STATE : State> {

    fun onInterceptState(state: STATE): STATE

    fun onInterceptAction(action: Action<STATE>): Action<STATE>

    fun onInterceptLifecycleAction(lifecycleState: LifecycleState): LifecycleState

    fun onInterceptReducer(reducer: Reducer<STATE>): Reducer<STATE>

    fun onInterceptEvent(event: Event<STATE>): Event<STATE>
}
