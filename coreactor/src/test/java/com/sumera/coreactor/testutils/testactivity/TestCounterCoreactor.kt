package com.sumera.coreactor.testutils.testactivity

import com.sumera.coreactor.Coreactor
import com.sumera.coreactor.contract.action.Action
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TestCounterCoreactor : Coreactor<TestCounterState>() {

    override fun createInitialState(): TestCounterState {
        return TestCounterState(counter = 0)
    }

    override fun onAction(action: Action<TestCounterState>) {
        when (action) {
            IncrementAction -> {
                emit(IncrementReducer)
            }
            DecrementAction -> {
                emit(DecrementReducer)
            }
            DelayedDecrementAction -> {
                launch {
                    delay(1000)
                    emit(DecrementReducer)
                }
            }
            MultipleDecrementAction -> {
                emit(DecrementReducer)
                emit(DecrementReducer)
                emit(DecrementReducer)
            }
            is SendEventAction -> {
                emit(TestEvent(action.message, action.behavior))
            }
        }
    }
}
