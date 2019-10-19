package com.sumera.sample.ui.counter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.sumera.coreactor.CoreactorActivity
import com.sumera.coreactor.CoreactorView
import com.sumera.coreactor.contract.event.Event
import com.sumera.sample.R
import kotlinx.android.synthetic.main.activity_counter.*

class CounterActivity : CoreactorActivity<CounterState>(), CoreactorView<CounterState> {

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, CounterActivity::class.java)
        }
    }

    override fun layoutRes() = R.layout.activity_counter

    override val coreactorFactory = CounterCoreactorFactory()

    override val coreactorView = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        counterView_incrementButton.setOnClickListener {
            sendAction(OnIncrementClicked)
        }

        counterView_decrementButton.setOnClickListener {
            sendAction(OnDecrementClicked)
        }
    }

    override fun onState(state: CounterState) {
        counterView_counterValue.text = state.counter.toString()
    }

    override fun onEvent(event: Event<CounterState>) {
        when (event) {
            is ShowToast -> {
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}