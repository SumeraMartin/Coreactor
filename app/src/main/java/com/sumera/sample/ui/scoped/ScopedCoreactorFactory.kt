package com.sumera.sample.ui.scoped

import com.sumera.coreactor.CoreactorFactory
import com.sumera.sample.tools.Timer

class ScopedCoreactorFactory : CoreactorFactory<ScopedCoreactor>() {

    override val coreactor = ScopedCoreactor(Timer())

    override val coreactorClass = ScopedCoreactor::class
}
