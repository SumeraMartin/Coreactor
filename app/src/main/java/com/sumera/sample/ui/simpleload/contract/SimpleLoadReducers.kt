package com.sumera.sample.ui.simpleload.contract

import com.sumera.coreactor.contract.reducer.Reducer

object ShowLoadingState : Reducer<SimpleLoadState>() {
    override fun reduce(oldState: SimpleLoadState): SimpleLoadState {
        return oldState.copy(
            isInitialState = false,
            data = null,
            isLoading = true,
            error = null
        )
    }
}

data class ShowErrorState(private val error: Throwable) : Reducer<SimpleLoadState>() {
    override fun reduce(oldState: SimpleLoadState): SimpleLoadState {
        return oldState.copy(
            data = null,
            isLoading = false,
            error = error
        )
    }
}

data class ShowDataState(private val data: String) : Reducer<SimpleLoadState>() {
    override fun reduce(oldState: SimpleLoadState): SimpleLoadState {
        return oldState.copy(
            data = data,
            isLoading = false,
            error = null
        )
    }
}
