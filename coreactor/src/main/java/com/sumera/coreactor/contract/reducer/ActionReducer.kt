package com.sumera.coreactor.contract.reducer

import com.sumera.coreactor.contract.state.State

data class ActionReducer<STATE: State>(private val action: (STATE) -> STATE) : Reducer<STATE>() {

    override fun reduce(oldState: STATE): STATE {
        return action(oldState)
    }
}

fun <STATE : State> reducer(reducerAction: (STATE) -> STATE): ActionReducer<STATE> {
    return ActionReducer { state -> reducerAction(state) }
}
