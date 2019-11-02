package com.sumera.sample.ui.scoped

import com.sumera.coreactor.Coreactor
import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.lifecycle.LifecycleState
import com.sumera.sample.tools.Timer
import com.sumera.sample.ui.scoped.contract.ScopedState
import kotlinx.coroutines.delay

class ScopedCoreactor(private val timer: Timer) : Coreactor<ScopedState>() {

    private companion object {
        const val DELAY = 100L
        const val TIME_STEP = 0.1
    }

    override fun createInitialState() = ScopedState(
        initialTime = timer.currentTime,
        timeInCreatedState = 0.0,
        timeInStartedState = 0.0
    )

    override fun onLifecycleState(state: LifecycleState) {
        when (state) {
            LifecycleState.ON_CREATE -> launchWhenCreated {
                while(true) {
                    delay(DELAY)
                    emitReducer { state -> state.copy(timeInCreatedState = state.timeInCreatedState + TIME_STEP) }
                }
            }
            LifecycleState.ON_START -> launchWhenStarted {
                while(true) {
                    delay(DELAY)
                    emitReducer { state -> state.copy(timeInStartedState = state.timeInStartedState + TIME_STEP) }
                }
            }
        }
    }

    override fun onAction(action: Action<ScopedState>) {
        // NoOp
    }
}
