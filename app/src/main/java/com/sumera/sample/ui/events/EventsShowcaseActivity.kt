package com.sumera.sample.ui.events

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.sumera.coreactor.CoreactorActivity
import com.sumera.coreactor.CoreactorView
import com.sumera.coreactor.contract.event.Event
import com.sumera.sample.R
import com.sumera.sample.ui.events.contract.EventsShowcaseEvent
import com.sumera.sample.ui.events.contract.EventsShowcaseState
import com.sumera.sample.ui.events.contract.OnDispatchEveryTimeClicked
import com.sumera.sample.ui.events.contract.OnDispatchToStartedOrThrowAwayClicked
import com.sumera.sample.ui.events.contract.OnDispatchToStartedOrWaitClicked
import com.sumera.sample.ui.events.contract.OnEventsDelayChanged
import kotlinx.android.synthetic.main.activity_events_showcase.*

class EventShowcaseActivity : CoreactorActivity<EventsShowcaseState>(), CoreactorView<EventsShowcaseState> {

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, EventShowcaseActivity::class.java)
        }
    }

    override fun layoutRes() = R.layout.activity_events_showcase

    override val coreactorFactory = EventsShowcaseCoreactorFactory()

    override val coreactorView = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        eventsShowcase_dispatchToStartedOrWait.setOnClickListener {
            sendAction(OnDispatchToStartedOrWaitClicked)
        }
        eventsShowcase_dispatchToStartedOrThrowAway.setOnClickListener {
            sendAction(OnDispatchToStartedOrThrowAwayClicked)
        }
        eventsShowcase_dispatchEveryTime.setOnClickListener {
            sendAction(OnDispatchEveryTimeClicked)
        }
        eventsShowcase_delaySettings.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.eventsShowcase_delayNone -> sendAction(OnEventsDelayChanged(0))
                R.id.eventsShowcase_delayOneSecond -> sendAction(OnEventsDelayChanged(1000))
                R.id.eventsShowcase_delayThreeSeconds -> sendAction(OnEventsDelayChanged(3000))
                R.id.eventsShowcase_delayFiveSeconds -> sendAction(OnEventsDelayChanged(5000))
            }
        }
    }

    override fun onState(state: EventsShowcaseState) {
        // Nothing to do here
    }

    override fun onEvent(event: Event<EventsShowcaseState>) {
        when (event) {
            is EventsShowcaseEvent -> {
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}


