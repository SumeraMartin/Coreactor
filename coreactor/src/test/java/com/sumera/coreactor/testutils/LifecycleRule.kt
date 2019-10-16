package com.sumera.coreactor.testutils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class LifecycleRule {

    lateinit var lifecycle: LifecycleRegistry

    fun setUp() {
        lifecycle = LifecycleRegistry(LifecycleOwner {
            object : Lifecycle() {
                override fun addObserver(observer: LifecycleObserver) {
                    TODO("Not used")
                }

                override fun removeObserver(observer: LifecycleObserver) {
                    TODO("Not used")
                }

                override fun getCurrentState(): State {
                    TODO("Not used")
                }
            }
        })
    }

    fun handleLifecycleEvents(vararg lifecycleEvents: Lifecycle.Event) {
        lifecycleEvents.forEach { lifecycleEvent ->
            lifecycle.handleLifecycleEvent(lifecycleEvent)
        }
    }

    fun tearDown() {
        // NoOp
    }
}
