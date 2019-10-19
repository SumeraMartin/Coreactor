package com.sumera.coreactor

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProviders
import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.state.State
import com.sumera.coreactor.error.CoreactorException
import com.sumera.coreactor.internal.ViewFinishingDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class CoreactorDelegate<STATE : State> : CoroutineScope by MainScope() {

    private var coreactor: Coreactor<STATE>? = null

    fun initCoreactor(coreactorOwner: CoreactorOwner<STATE>, lifecycle: Lifecycle, fragment: Fragment): Coreactor<STATE> {
        val factory = coreactorOwner.coreactorFactory
        val coreactorClass = factory.coreactorClass.java
        return ViewModelProviders.of(fragment, factory).get(coreactorClass).apply {
            coreactor = this
            attachView(coreactorOwner.coreactorView)
            lifecycle.addObserver(this)
        }
    }

    fun initCoreactor(coreactorOwner: CoreactorOwner<STATE>, lifecycle: Lifecycle, activity: FragmentActivity): Coreactor<STATE> {
        val factory = coreactorOwner.coreactorFactory
        val coreactorClass = factory.coreactorClass.java
        return ViewModelProviders.of(activity, factory).get(coreactorClass).apply {
            coreactor = this
            attachView(coreactorOwner.coreactorView)
            lifecycle.addObserver(this)
        }
    }

    fun sendAction(action: Action<STATE>) = launch {
        coreactor?.sendAction(action) ?: throwUninitialized()
    }

    fun destroyCoreactor(fragment: Fragment) {
        coreactor?.detachView(ViewFinishingDetector.isFinishing(fragment)) ?: throwUninitialized()
    }

    fun destroyCoreactor(activity: FragmentActivity) {
        coreactor?.detachView(ViewFinishingDetector.isFinishing(activity)) ?: throwUninitialized()
    }

    private fun throwUninitialized(): Nothing {
        throw CoreactorException("Coreactor is not initialized")
    }
}
