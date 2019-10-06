package com.sumera.sample.ui.infinity.contract

import com.sumera.coreactor.contract.action.Action

object OnRetryLoading : Action<InfinityLoadingState>()

object OnRetryLoadingNext : Action<InfinityLoadingState>()

object OnBottomReached : Action<InfinityLoadingState>()

data class OnItemFavoriteClicked(val item: ItemWrapper) : Action<InfinityLoadingState>()
