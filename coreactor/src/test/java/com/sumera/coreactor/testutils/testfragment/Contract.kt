package com.sumera.coreactor.testutils.testfragment

import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.event.Event
import com.sumera.coreactor.contract.event.EventBehaviour
import com.sumera.coreactor.contract.reducer.Reducer
import com.sumera.coreactor.contract.state.State

data class TestCounterState(
    val counter: Int
) : State

object IncrementAction : Action<TestCounterState>

object DecrementAction : Action<TestCounterState>

object DelayedDecrementAction : Action<TestCounterState>

object MultipleDecrementAction : Action<TestCounterState>

data class SendEventAction(val message: String, val behavior: EventBehaviour) : Action<TestCounterState>

data class TestEvent(val message: String, private val behaviorInternal: EventBehaviour) : Event<TestCounterState>() {
    override val behaviour = behaviorInternal
}

object IncrementReducer : Reducer<TestCounterState>() {
    override fun reduce(oldState: TestCounterState): TestCounterState {
        return oldState.copy(counter = oldState.counter + 1)
    }
}

object DecrementReducer : Reducer<TestCounterState>() {
    override fun reduce(oldState: TestCounterState): TestCounterState {
        return oldState.copy(counter = oldState.counter - 1)
    }
}
