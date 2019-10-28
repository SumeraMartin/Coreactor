package com.sumera.sample.ui.counter

import com.sumera.coreactor.Coreactor
import com.sumera.coreactor.contract.action.Action

class CounterCoreactor : Coreactor<CounterState>() {

    override fun createInitialState(): CounterState {
        return CounterState(counter = 0)
    }

    override fun onAction(action: Action<CounterState>) {
        when (action) {
            OnIncrementClicked -> {
                emit(IncrementReducer)
            }
            OnDecrementClicked -> {
                emit(DecrementReducer)
            }
        }

        if (state.counter % 5 == 0 && state.counter != 0) {
            emit(ShowToast("Counter is divisible by 5"))
        }
    }
}