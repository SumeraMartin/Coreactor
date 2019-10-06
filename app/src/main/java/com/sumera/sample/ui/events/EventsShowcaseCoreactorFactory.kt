package com.sumera.sample.ui.events

import com.sumera.coreactor.CoreactorFactory

class EventsShowcaseCoreactorFactory : CoreactorFactory<EventsShowcaseCoreactor>() {

    override val coreactor = EventsShowcaseCoreactor()

    override val coreactorClass = EventsShowcaseCoreactor::class
}