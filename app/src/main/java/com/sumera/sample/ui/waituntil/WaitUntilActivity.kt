package com.sumera.sample.ui.waituntil

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.sumera.coreactor.CoreactorActivity
import com.sumera.coreactor.CoreactorView
import com.sumera.coreactor.contract.event.Event
import com.sumera.sample.R
import com.sumera.sample.ui.waituntil.contract.ConfirmAction
import com.sumera.sample.ui.waituntil.contract.FirstButtonClicked
import com.sumera.sample.ui.waituntil.contract.SecondButtonClicked
import com.sumera.sample.ui.waituntil.contract.ThirdButtonClicked
import com.sumera.sample.ui.waituntil.contract.WaitUntilState
import kotlinx.android.synthetic.main.activity_wait_until.*

class WaitUntilActivity : CoreactorActivity<WaitUntilState>(), CoreactorView<WaitUntilState> {

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, WaitUntilActivity::class.java)
        }
    }

    override fun layoutRes() = R.layout.activity_wait_until

    override val coreactorFactory = WaitUntilCoreactorFactory()

    override val coreactorView = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        waitUntil_first.setOnClickListener {
            sendAction { FirstButtonClicked }
        }
        waitUntil_second.setOnClickListener {
            sendAction { SecondButtonClicked }
        }
        waitUntil_third.setOnClickListener {
            sendAction { ThirdButtonClicked }
        }
        waitUntil_confirmYes.setOnClickListener {
            sendAction { ConfirmAction(confirmed = true) }
        }
        waitUntil_confirmNo.setOnClickListener {
            sendAction { ConfirmAction(confirmed = false) }
        }
    }

    override fun onState(state: WaitUntilState) {
        waitUntil_first.isEnabled = state.isFirstButtonEnabled
        waitUntil_second.isEnabled = state.isSecondButtonEnabled
        waitUntil_third.isEnabled = state.isThirdButtonEnabled
        waitUntil_confirmTitle.isEnabled = state.isConfirmEnabled
        waitUntil_confirmYes.isEnabled = state.isConfirmEnabled
        waitUntil_confirmNo.isEnabled = state.isConfirmEnabled
        waitUntil_finished.visibility = if (state.isCompleted) View.VISIBLE else View.INVISIBLE
    }

    override fun onEvent(event: Event<WaitUntilState>) {
        // NoOp
    }
}
