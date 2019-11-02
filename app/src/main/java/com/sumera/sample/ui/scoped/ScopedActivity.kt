package com.sumera.sample.ui.scoped

import android.content.Context
import android.content.Intent
import com.sumera.coreactor.CoreactorActivity
import com.sumera.coreactor.CoreactorView
import com.sumera.coreactor.contract.event.Event
import com.sumera.sample.ui.scoped.contract.ScopedState
import kotlinx.android.synthetic.main.activity_scoped.*

class ScopedActivity : CoreactorActivity<ScopedState>(), CoreactorView<ScopedState> {

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, ScopedActivity::class.java)
        }
    }

    override fun layoutRes() = com.sumera.sample.R.layout.activity_scoped

    override val coreactorFactory = ScopedCoreactorFactory()

    override val coreactorView = this

    override fun onState(state: ScopedState) {
        scoped_createdValue.text = state.timeInCreatedStateFormatted
        scoped_startedValue.text = state.timeInStartedStateFormatted
    }

    override fun onEvent(event: Event<ScopedState>) {
        // NoOp
    }
}
