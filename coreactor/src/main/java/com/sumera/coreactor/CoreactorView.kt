package com.sumera.coreactor

import com.sumera.coreactor.contract.state.State
import com.sumera.coreactor.contract.event.Event

interface CoreactorView<STATE : State> {

    fun onState(state: STATE)

    fun onEvent(event: Event<STATE>)
}
