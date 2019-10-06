package com.sumera.sample.ui.infinity

import com.sumera.coreactor.CoreactorFactory
import com.sumera.sample.interactors.GetItemsInteractor
import com.sumera.sample.interactors.SetFavoriteItemStateInteractor

class InfinityLoadingCoreactorFactory : CoreactorFactory<InfinityLoadingCoreactor>() {

    override val coreactor = InfinityLoadingCoreactor(GetItemsInteractor(), SetFavoriteItemStateInteractor())

    override val coreactorClass = InfinityLoadingCoreactor::class
}