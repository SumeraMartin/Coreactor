package com.sumera.coreactor.testutils.testfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sumera.coreactor.CoreactorFragment
import com.sumera.coreactor.CoreactorView
import com.sumera.coreactor.R
import com.sumera.coreactor.contract.action.Action
import io.mockk.mockk

class TestCounterFragment : CoreactorFragment<TestCounterState>() {

    val mockView: CoreactorView<TestCounterState> = mockk(relaxUnitFun = true)

    override val coreactorFactory = TestCounterCoreactorFactory()

    override val coreactorView = mockView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.test_view, container, false)
    }

    fun action(action: Action<TestCounterState>) {
        sendAction(action)
    }
}
