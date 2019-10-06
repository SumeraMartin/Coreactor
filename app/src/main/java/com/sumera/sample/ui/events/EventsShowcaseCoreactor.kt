package com.sumera.sample.ui.events

import com.sumera.coreactor.Coreactor
import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.event.EventBehaviour
import com.sumera.coreactor.lifecycle.LifecycleState
import com.sumera.sample.ui.events.contract.EventsShowcaseEvent
import com.sumera.sample.ui.events.contract.EventsShowcaseState
import com.sumera.sample.ui.events.contract.OnDispatchEveryTimeClicked
import com.sumera.sample.ui.events.contract.OnDispatchToStartedOrThrowAwayClicked
import com.sumera.sample.ui.events.contract.OnDispatchToStartedOrWaitClicked
import com.sumera.sample.ui.events.contract.OnEventsDelayChanged
import kotlinx.coroutines.delay

class EventsShowcaseCoreactor : Coreactor<EventsShowcaseState>() {

    override fun createInitialState() = EventsShowcaseState(
        eventsDelay = 1000
    )

    override fun onLifecycleAction(state: LifecycleState) = coreactorFlow {
        when (state) {
            LifecycleState.ON_ATTACH -> {
                emitFrom { EventsShowcaseEvent("ON_ATTACH", EventBehaviour.DISPATCH_TO_STARTED_OR_WAIT) }
            }
        }
    }

    override fun onAction(action: Action<EventsShowcaseState>) = coreactorFlow {
        when (action) {
            OnDispatchToStartedOrWaitClicked -> {
                delay(state.eventsDelay)
                emitFrom { EventsShowcaseEvent("DISPATCH_TO_STARTED_OR_WAIT", EventBehaviour.DISPATCH_TO_STARTED_OR_WAIT) }
            }
            OnDispatchToStartedOrThrowAwayClicked -> {
                delay(state.eventsDelay)
                emitFrom { EventsShowcaseEvent("DISPATCH_TO_STARTED_OR_THROW_AWAY", EventBehaviour.DISPATCH_TO_STARTED_OR_THROW_AWAY) }
            }
            OnDispatchEveryTimeClicked -> {
                delay(state.eventsDelay)
                emitFrom { EventsShowcaseEvent("DISPATCH_EVERY_TIME", EventBehaviour.DISPATCH_EVERY_TIME) }
            }
            is OnEventsDelayChanged -> {
                emitReducer { state -> state.copy(eventsDelay = action.newEventsDelay) }
            }
        }
    }
}