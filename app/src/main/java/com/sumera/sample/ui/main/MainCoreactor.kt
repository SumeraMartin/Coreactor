package com.sumera.sample.ui.main

import com.sumera.coreactor.Coreactor
import com.sumera.coreactor.contract.action.Action
import com.sumera.sample.ui.main.contract.MainState
import com.sumera.sample.ui.main.contract.NavigateToSimpleLoad
import com.sumera.sample.ui.main.contract.SimpleLoadClicked

class MainCoreactor : Coreactor<MainState>() {

    override fun createInitialState() = MainState

    override fun onAction(action: Action<MainState>) = coreactorFlow {
        when (action) {
            SimpleLoadClicked -> {
                emit(NavigateToSimpleLoad)
            }
        }
    }
}
