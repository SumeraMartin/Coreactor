package com.sumera.sample.ui.waituntil

import com.sumera.coreactor.CoreactorFactory

class WaitUntilCoreactorFactory : CoreactorFactory<WaitUntilCoreactor>() {

    override val coreactor = WaitUntilCoreactor()

    override val coreactorClass = WaitUntilCoreactor::class
}
