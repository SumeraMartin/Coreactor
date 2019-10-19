package com.sumera.sample.ui.counter

import com.sumera.coreactor.contract.event.Event

data class ShowToast(val message: String) : Event<CounterState>()
