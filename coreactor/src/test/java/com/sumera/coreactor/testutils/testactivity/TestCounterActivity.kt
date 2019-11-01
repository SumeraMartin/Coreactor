package com.sumera.coreactor.testutils.testactivity

import com.sumera.coreactor.CoreactorActivity
import com.sumera.coreactor.CoreactorView
import com.sumera.coreactor.R
import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.testutils.testcoreactor.TestCounterCoreactorFactory
import com.sumera.coreactor.testutils.testcoreactor.TestCounterState
import io.mockk.mockk

class TestCounterActivity : CoreactorActivity<TestCounterState>() {

    override fun layoutRes() = R.layout.test_view

    val mockView: CoreactorView<TestCounterState> = mockk(relaxUnitFun = true)

    override val coreactorFactory = TestCounterCoreactorFactory()

    override val coreactorView = mockView

    fun action(action: Action<TestCounterState>) {
        sendAction(action)
    }
}
