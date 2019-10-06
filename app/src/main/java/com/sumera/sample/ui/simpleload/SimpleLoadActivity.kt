package com.sumera.sample.ui.simpleload

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.sumera.coreactor.CoreactorActivity
import com.sumera.coreactor.CoreactorView
import com.sumera.coreactor.contract.event.Event
import com.sumera.sample.R
import com.sumera.sample.tools.extensions.isVisible
import com.sumera.sample.ui.simpleload.contract.RetryLoadData
import com.sumera.sample.ui.simpleload.contract.ShowToast
import com.sumera.sample.ui.simpleload.contract.SimpleLoadState
import com.sumera.sample.ui.simpleload.contract.StartLoadData
import kotlinx.android.synthetic.main.activity_simple_load.*

class SimpleLoadActivity : CoreactorActivity<SimpleLoadState>(), CoreactorView<SimpleLoadState> {

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, SimpleLoadActivity::class.java)
        }
    }

    override fun layoutRes() = R.layout.activity_simple_load

    override val coreactorFactory = SimpleLoadCoreactorFactory()

    override val coreactorView = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        simpleLoad_startLoad.setOnClickListener {
            sendAction { StartLoadData }
        }
        simpleLoad_retryLoad.setOnClickListener {
            sendAction { RetryLoadData }
        }
    }

    override fun onState(state: SimpleLoadState) {
        simpleLoad_startLoad.isVisible = state.isInitialState
        simpleLoad_loading.isVisible = state.isLoading
        simpleLoad_retryLoad.isVisible = state.error != null
        simpleLoad_data.isVisible = state.data != null
        simpleLoad_data.text = state.data ?: ""
    }

    override fun onEvent(event: Event<SimpleLoadState>) {
        when (event) {
            is ShowToast -> {
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}