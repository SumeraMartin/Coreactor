package com.sumera.sample.ui.main

import com.sumera.coreactor.CoreactorFactory

class MainCoreactorFactory : CoreactorFactory<MainCoreactor>() {

    override val coreactorClass = MainCoreactor::class

    override val coreactor = MainCoreactor()
}
