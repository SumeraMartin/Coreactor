package com.sumera.sample.ui.main

import android.os.Bundle
import com.sumera.coreactor.CoreactorActivity
import com.sumera.coreactor.CoreactorView
import com.sumera.coreactor.contract.event.Event
import com.sumera.sample.R
import com.sumera.sample.ui.counter.CounterActivity
import com.sumera.sample.ui.events.EventShowcaseActivity
import com.sumera.sample.ui.infinity.InfinityLoadingActivity
import com.sumera.sample.ui.main.contract.CounterClicked
import com.sumera.sample.ui.main.contract.EventsShowcaseClicked
import com.sumera.sample.ui.main.contract.InfinityLoadingClicked
import com.sumera.sample.ui.main.contract.MainState
import com.sumera.sample.ui.main.contract.NavigateToCounter
import com.sumera.sample.ui.main.contract.NavigateToEventsShowcase
import com.sumera.sample.ui.main.contract.NavigateToInfinityLoading
import com.sumera.sample.ui.main.contract.NavigateToScoped
import com.sumera.sample.ui.main.contract.NavigateToSimpleLoad
import com.sumera.sample.ui.main.contract.ScopedClicked
import com.sumera.sample.ui.main.contract.SimpleLoadClicked
import com.sumera.sample.ui.scoped.ScopedActivity
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
        main_infinityLoading.setOnClickListener {
            sendAction { InfinityLoadingClicked }
        }
        main_eventsShowcase.setOnClickListener {
            sendAction { EventsShowcaseClicked }
        }
        main_counter.setOnClickListener {
            sendAction { CounterClicked }
        }
        main_scoped.setOnClickListener {
            sendAction { ScopedClicked }
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
            NavigateToInfinityLoading -> {
                startActivity(InfinityLoadingActivity.getIntent(this))
            }
            NavigateToEventsShowcase -> {
                startActivity(EventShowcaseActivity.getIntent(this))
            }
            NavigateToCounter -> {
                startActivity(CounterActivity.getIntent(this))
            }
            NavigateToScoped -> {
                startActivity(ScopedActivity.getIntent(this))
            }
        }
    }
}
