package com.sumera.sample.ui.main

import android.os.Bundle
import com.sumera.coreactor.CoreactorActivity
import com.sumera.coreactor.CoreactorView
import com.sumera.coreactor.contract.event.Event
import com.sumera.sample.R
import com.sumera.sample.ui.main.contract.MainState
import com.sumera.sample.ui.main.contract.NavigateToSimpleLoad
import com.sumera.sample.ui.main.contract.SimpleLoadClicked
import com.sumera.sample.ui.simpleload.SimpleLoadActivity
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : CoreactorActivity<MainState>(), CoreactorView<MainState> {

    override fun layoutRes() = R.layout.activity_main

    override val coreactorFactory = MainCoreactorFactory()

    override val coreactorView = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        main_simpleLoad.setOnClickListener {
            sendAction { SimpleLoadClicked }
        }
    }

    override fun onState(state: MainState) {
        // No state
    }

    override fun onEvent(event: Event<MainState>) {
        when (event) {
            NavigateToSimpleLoad -> {
                startActivity(SimpleLoadActivity.getIntent(this))
            }
        }
    }
}
