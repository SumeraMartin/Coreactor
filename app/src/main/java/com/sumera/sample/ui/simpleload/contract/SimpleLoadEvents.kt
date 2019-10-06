package com.sumera.sample.ui.simpleload.contract

import com.sumera.coreactor.contract.event.Event

data class ShowToast(val message: String) : Event<SimpleLoadState>()
