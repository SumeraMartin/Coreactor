package com.sumera.coreactor.testutils

import com.sumera.coreactor.CoreactorView
import com.sumera.coreactor.contract.event.Event

class TestView : CoreactorView<TestState> {

    val stateList = mutableListOf<TestState>()

    val eventList = mutableListOf<Event<TestState>>()

    override fun onState(state: TestState) {
        stateList.add(state)
    }

    override fun onEvent(event: Event<TestState>) {
        eventList.add(event)
    }
}
