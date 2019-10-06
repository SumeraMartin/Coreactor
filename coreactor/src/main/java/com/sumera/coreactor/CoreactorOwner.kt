package com.sumera.coreactor

import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.state.State

interface CoreactorOwner<STATE : State> {

    val coreactorFactory: CoreactorFactory<out Coreactor<STATE>>

    val coreactorView: CoreactorView<STATE>

    val coreactorDelegate: CoreactorDelegate<STATE>

    fun sendAction(actionCreator: () -> Action<STATE>) {
        coreactorDelegate.sendAction(actionCreator())
    }

    fun sendAction(vararg actions: Action<STATE>) {
        for (action in actions) {
            coreactorDelegate.sendAction(action)
        }
    }
}