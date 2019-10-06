package com.sumera.sample.ui.simpleload.contract

import com.sumera.coreactor.contract.state.State

data class SimpleLoadState(
    val isInitialState: Boolean,
    val data: String?,
    val isLoading: Boolean,
    val error: Throwable?
) : State
