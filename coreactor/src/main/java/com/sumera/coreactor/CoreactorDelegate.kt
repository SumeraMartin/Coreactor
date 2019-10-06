package com.sumera.coreactor

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProviders
import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.state.State
import com.sumera.coreactor.internal.ViewFinishingDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class CoreactorDelegate<STATE : State> : CoroutineScope by MainScope() {

    private lateinit var coreactor: Coreactor<STATE>

    fun initCoreactor(coreactorOwner: CoreactorOwner<STATE>, lifecycle: Lifecycle, fragment: Fragment): Coreactor<STATE> {
        val factory = coreactorOwner.coreactorFactory
        val coreactor = ViewModelProviders.of(fragment, factory).get(factory.coreactorClass.java)
        coreactor.attachView(coreactorOwner.coreactorView)
        lifecycle.addObserver(coreactor)
        return coreactor
    }

    fun initCoreactor(coreactorOwner: CoreactorOwner<STATE>, lifecycle: Lifecycle, activity: FragmentActivity): Coreactor<STATE> {
        val factory = coreactorOwner.coreactorFactory
        val coreactor = ViewModelProviders.of(activity, factory).get(factory.coreactorClass.java)
        coreactor.attachView(coreactorOwner.coreactorView)
        lifecycle.addObserver(coreactor)
        return coreactor
    }

    fun sendAction(action: Action<STATE>) = launch {
        coreactor.sendAction(action)
    }

    fun destroyCoreactor(fragment: Fragment) {
        coreactor.detachView(ViewFinishingDetector.isFinishing(fragment))
    }

    fun destroyCoreactor(activity: FragmentActivity) {
        coreactor.detachView(ViewFinishingDetector.isFinishing(activity))
    }
}