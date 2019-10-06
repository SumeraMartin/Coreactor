package com.sumera.sample.ui.events.contract

import com.sumera.coreactor.contract.event.Event
import com.sumera.coreactor.contract.event.EventBehaviour

data class EventsShowcaseEvent(
    val message: String,
    override val behaviour: EventBehaviour
) : Event<EventsShowcaseState>()
