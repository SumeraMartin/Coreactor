package com.sumera.sample.ui.waituntil.contract

import com.sumera.coreactor.contract.reducer.Reducer

object SetInitialStateReducer : Reducer<WaitUntilState>() {
    override fun reduce(oldState: WaitUntilState): WaitUntilState {
        return oldState.copy(
            isFirstButtonEnabled = true,
            isSecondButtonEnabled = false,
            isThirdButtonEnabled = false,
            isConfirmEnabled = false,
            isCompleted = false
        )
    }
}

object EnableSecondButtonReducer : Reducer<WaitUntilState>() {
    override fun reduce(oldState: WaitUntilState): WaitUntilState {
        return oldState.copy(
            isFirstButtonEnabled = false,
            isSecondButtonEnabled = true,
            isThirdButtonEnabled = false,
            isConfirmEnabled = false,
            isCompleted = false
        )
    }
}

object EnableThirdButtonReducer : Reducer<WaitUntilState>() {
    override fun reduce(oldState: WaitUntilState): WaitUntilState {
        return oldState.copy(
            isFirstButtonEnabled = false,
            isSecondButtonEnabled = false,
            isThirdButtonEnabled = true,
            isConfirmEnabled = false,
            isCompleted = false
        )
    }
}

object EnableConfirmButtonsReducer : Reducer<WaitUntilState>() {
    override fun reduce(oldState: WaitUntilState): WaitUntilState {
        return oldState.copy(
            isFirstButtonEnabled = false,
            isSecondButtonEnabled = false,
            isThirdButtonEnabled = false,
            isConfirmEnabled = true,
            isCompleted = false
        )
    }
}

object SetCompletedStateReducer : Reducer<WaitUntilState>() {
    override fun reduce(oldState: WaitUntilState): WaitUntilState {
        return oldState.copy(
            isFirstButtonEnabled = false,
            isSecondButtonEnabled = false,
            isThirdButtonEnabled = false,
            isConfirmEnabled = false,
            isCompleted = true
        )
    }
}
