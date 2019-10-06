package com.sumera.sample.ui.infinity.contract

import com.sumera.coreactor.contract.state.State
import com.sumera.sample.data.Item

data class ItemWrapper(
    val item: Item,
    val isSavingNewFavoriteState: Boolean,
    val errorSavingFavoriteState: Throwable?
)

data class NextItemsState(
    val isLoading: Boolean,
    val error: Throwable?
)

data class InfinityLoadingState(
    val isLoading: Boolean,
    val error: Throwable?,
    val content: List<ItemWrapper>,
    val nextItemsState: NextItemsState
) : State