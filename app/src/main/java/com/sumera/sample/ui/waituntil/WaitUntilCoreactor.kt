package com.sumera.sample.ui.waituntil

import com.sumera.coreactor.Coreactor
import com.sumera.coreactor.contract.action.Action
import com.sumera.sample.ui.waituntil.contract.ConfirmAction
import com.sumera.sample.ui.waituntil.contract.EnableConfirmButtonsReducer
import com.sumera.sample.ui.waituntil.contract.EnableSecondButtonReducer
import com.sumera.sample.ui.waituntil.contract.EnableThirdButtonReducer
import com.sumera.sample.ui.waituntil.contract.FirstButtonClicked
import com.sumera.sample.ui.waituntil.contract.SecondButtonClicked
import com.sumera.sample.ui.waituntil.contract.SetCompletedStateReducer
import com.sumera.sample.ui.waituntil.contract.SetInitialStateReducer
import com.sumera.sample.ui.waituntil.contract.ThirdButtonClicked
import com.sumera.sample.ui.waituntil.contract.WaitUntilState
import kotlinx.coroutines.launch

class WaitUntilCoreactor : Coreactor<WaitUntilState>() {

    override fun createInitialState() = WaitUntilState(
        isFirstButtonEnabled = true,
        isSecondButtonEnabled = false,
        isThirdButtonEnabled = false,
        isConfirmEnabled = false,
        isCompleted = false
    )

    override fun onAction(action: Action<WaitUntilState>) {
        when (action) {
            FirstButtonClicked -> launch {

                emit { EnableSecondButtonReducer }

                waitUntilAction { it is SecondButtonClicked }

                emit { EnableThirdButtonReducer }

                waitUntilAction { it is ThirdButtonClicked }

                emit { EnableConfirmButtonsReducer }

                val confirmAction = waitUntilActionType<ConfirmAction>()
                if (confirmAction.confirmed) {
                    emit { SetCompletedStateReducer }
                } else {
                    emit { SetInitialStateReducer }
                }
            }
        }
    }
}
