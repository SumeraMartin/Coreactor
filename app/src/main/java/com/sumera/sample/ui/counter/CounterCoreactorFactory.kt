package com.sumera.sample.ui.counter

import com.sumera.coreactor.CoreactorFactory

class CounterCoreactorFactory : CoreactorFactory<CounterCoreactor>() {

    override val coreactor = CounterCoreactor()

    override val coreactorClass = CounterCoreactor::class
}