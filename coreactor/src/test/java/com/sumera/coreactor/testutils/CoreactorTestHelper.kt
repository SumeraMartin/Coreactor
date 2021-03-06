package com.sumera.coreactor.testutils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import com.sumera.coreactor.CoreactorView
import com.sumera.coreactor.contract.state.State

class CoreactorTestHelper<STATE : State>(
    private val coreactor: TestableCoreactor<STATE>,
    private val view: CoreactorView<STATE>,
    private val lifecycleRegistry: LifecycleRegistry
) {

    fun attach() {
        coreactor.attachView(view)
        lifecycleRegistry.addObserver(coreactor)
    }

    fun detachWithFinishing() {
        coreactor.callOnCleared()
    }

    fun detachWithoutFinishing() {
        // Do nothing
    }

    fun fromOnAttachToOnCreate() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    fun fromOnAttachToOnStart() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    fun fromOnAttachToOnResume() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun fromOnCreateToOnStart() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    fun fromOnStartToOnStop() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    fun fromOnStartToOnResume() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun fromOnResumeToOnPause() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    fun fromOnResumeToOnStop() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    fun fromOnResumeToOnDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}
