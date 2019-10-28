package com.sumera.sample.ui.main

import com.sumera.coreactor.Coreactor
import com.sumera.coreactor.contract.action.Action
import com.sumera.sample.ui.main.contract.CounterClicked
import com.sumera.sample.ui.main.contract.EventsShowcaseClicked
import com.sumera.sample.ui.main.contract.InfinityLoadingClicked
import com.sumera.sample.ui.main.contract.MainState
import com.sumera.sample.ui.main.contract.NavigateToCounter
import com.sumera.sample.ui.main.contract.NavigateToEventsShowcase
import com.sumera.sample.ui.main.contract.NavigateToInfinityLoading
import com.sumera.sample.ui.main.contract.NavigateToSimpleLoad
import com.sumera.sample.ui.main.contract.SimpleLoadClicked

class MainCoreactor : Coreactor<MainState>() {

    override fun createInitialState() = MainState

    override fun onAction(action: Action<MainState>) {
        when (action) {
            SimpleLoadClicked -> {
                emit(NavigateToSimpleLoad)
            }
            InfinityLoadingClicked -> {
                emit(NavigateToInfinityLoading)
            }
            EventsShowcaseClicked -> {
                emit(NavigateToEventsShowcase)
            }
            CounterClicked -> {
                emit(NavigateToCounter)
            }
        }
    }
}
