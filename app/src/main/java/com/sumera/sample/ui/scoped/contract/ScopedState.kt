package com.sumera.sample.ui.scoped.contract

import com.sumera.coreactor.contract.state.State

data class ScopedState(
    val initialTime: Long,
    val timeInCreatedState: Double,
    val timeInStartedState: Double
) : State {

    val timeInCreatedStateFormatted: String get() {
        return String.format("%.1f", timeInCreatedState)
    }

    val timeInStartedStateFormatted: String get() {
        return String.format("%.1f", timeInStartedState)
    }
}
