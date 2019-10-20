package com.sumera.coreactor.testutils.testactivity

import com.sumera.coreactor.CoreactorFactory

class TestCounterCoreactorFactory : CoreactorFactory<TestCounterCoreactor>() {

    override val coreactor = TestCounterCoreactor()

    override val coreactorClass = TestCounterCoreactor::class
}
