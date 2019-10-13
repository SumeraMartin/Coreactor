package com.sumera.coreactor.lifecycle

import androidx.lifecycle.Lifecycle
import com.sumera.coreactor.error.CoreactorException

enum class LifecycleState {
    INITIAL, ON_ATTACH, ON_CREATE, ON_START, ON_RESUME, ON_PAUSE, ON_STOP, ON_DESTROY, ON_DETACH;

    val isInitialState: Boolean get() {
        return this == INITIAL
    }

    val isAttachState: Boolean get() {
        return this == ON_ATTACH
    }

    val isCreateState: Boolean get() {
        return this == ON_CREATE
    }

    val isStartState: Boolean get() {
        return this == ON_START
    }

    val isResumeState: Boolean get() {
        return this == ON_RESUME
    }

    val isPauseState: Boolean get() {
        return this == ON_PAUSE
    }

    val isStopState: Boolean get() {
        return this == ON_STOP
    }

    val isDestroyState: Boolean get() {
        return this == ON_DESTROY
    }

    val isDetachState: Boolean get() {
        return this == ON_DETACH
    }

    val isInCreatedState: Boolean get() {
        return when (this) {
            ON_CREATE, ON_START, ON_RESUME, ON_PAUSE, ON_STOP -> true
            else -> false
        }
    }

    val isInStartedState: Boolean get() {
        return when (this) {
            ON_START, ON_RESUME, ON_PAUSE -> true
            else -> false
        }
    }

    val isInResumedState: Boolean get() {
        return when (this) {
            ON_RESUME -> true
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
