package com.sumera.sample.ui.waituntil.contract

import com.sumera.coreactor.contract.action.Action

object FirstButtonClicked : Action<WaitUntilState>

object SecondButtonClicked : Action<WaitUntilState>

object ThirdButtonClicked : Action<WaitUntilState>

data class ConfirmAction(val confirmed: Boolean) : Action<WaitUntilState>
