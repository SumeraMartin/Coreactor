package com.sumera.sample.ui.events.contract

import com.sumera.coreactor.contract.action.Action

object OnDispatchToStartedOrWaitClicked : Action<EventsShowcaseState>

object OnDispatchToStartedOrThrowAwayClicked : Action<EventsShowcaseState>

object OnDispatchEveryTimeClicked : Action<EventsShowcaseState>

data class OnEventsDelayChanged(val newEventsDelay: Long) : Action<EventsShowcaseState>