package com.sumera.coreactor.lifecycle

import androidx.lifecycle.Lifecycle
import com.sumera.coreactor.error.CoreactorException

enum class LifecycleState {
    INITIAL, ON_ATTACH, ON_CREATE, ON_START, ON_RESUME, ON_PAUSE, ON_STOP, ON_DESTROY, ON_DETACH;

    val isCreateState: Boolean get() {
        return this == ON_CREATE
    }

    val isStartState: Boolean get() {
        return this == ON_START
    }

    val isInStartedState: Boolean get() {
        return when (this) {
            ON_START, ON_RESUME, ON_PAUSE -> true
            else -> false
        }
    }

    val isInCreatedState: Boolean get() {
        return when (this) {
            ON_CREATE, ON_START, ON_RESUME, ON_PAUSE, ON_STOP -> true
            else -> false
        }
    }

    companion object {
        fun fromLifecycle(lifecycleEvent: Lifecycle.Event): LifecycleState {
            return  when (lifecycleEvent) {
                Lifecycle.Event.ON_CREATE -> ON_CREATE
                Lifecycle.Event.ON_START -> ON_START
                Lifecycle.Event.ON_RESUME -> ON_RESUME
                Lifecycle.Event.ON_PAUSE -> ON_PAUSE
                Lifecycle.Event.ON_STOP -> ON_STOP
                Lifecycle.Event.ON_DESTROY -> ON_DESTROY
                else -> throw CoreactorException("Unexpected lifecycle event $lifecycleEvent")
            }
        }
    }
}
