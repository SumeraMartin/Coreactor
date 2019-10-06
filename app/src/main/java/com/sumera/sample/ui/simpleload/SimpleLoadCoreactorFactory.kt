package com.sumera.sample.ui.simpleload

import com.sumera.coreactor.CoreactorFactory
import com.sumera.sample.interactors.LoadSimpleDataInteractor

class SimpleLoadCoreactorFactory : CoreactorFactory<SimpleLoadCoreactor>() {

    override val coreactor = SimpleLoadCoreactor(LoadSimpleDataInteractor())

    override val coreactorClass = SimpleLoadCoreactor::class
}
