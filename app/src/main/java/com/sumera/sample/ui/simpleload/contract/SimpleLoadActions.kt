package com.sumera.sample.ui.simpleload.contract

import com.sumera.coreactor.contract.action.Action

object StartLoadData : Action<SimpleLoadState>()

object RetryLoadData : Action<SimpleLoadState>()
