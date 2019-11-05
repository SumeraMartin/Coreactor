package com.sumera.sample.ui.waituntil.contract

import com.sumera.coreactor.contract.state.State

data class WaitUntilState(
    val isFirstButtonEnabled: Boolean,
    val isSecondButtonEnabled: Boolean,
    val isThirdButtonEnabled: Boolean,
    val isConfirmEnabled: Boolean,
    val isCompleted: Boolean
) : State
