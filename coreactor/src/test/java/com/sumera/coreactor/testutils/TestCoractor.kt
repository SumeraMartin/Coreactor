package com.sumera.coreactor.testutils

import com.sumera.coreactor.Coreactor
import com.sumera.coreactor.contract.state.State

abstract class TestableCoreactor<STATE : State> : Coreactor<STATE>() {
    fun callOnCleared() {
        onCleared()
    }
}
