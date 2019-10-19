package com.sumera.sample.ui.counter

import com.sumera.coreactor.contract.reducer.Reducer

object IncrementReducer : Reducer<CounterState>() {
    override fun reduce(oldState: CounterState): CounterState {
        return oldState.copy(counter = oldState.counter + 1)
    }
}

object DecrementReducer : Reducer<CounterState>() {
    override fun reduce(oldState: CounterState): CounterState {
        return oldState.copy(counter = oldState.counter - 1)
    }
}