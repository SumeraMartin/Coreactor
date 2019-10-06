package com.sumera.sample.ui.infinity

import com.sumera.coreactor.Coreactor
import com.sumera.coreactor.CoreactorFlow
import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.lifecycle.LifecycleState
import com.sumera.sample.interactors.GetItemsInteractor
import com.sumera.sample.interactors.SetFavoriteItemStateInteractor
import com.sumera.sample.ui.infinity.contract.InfinityLoadingState
import com.sumera.sample.ui.infinity.contract.ItemWrapper
import com.sumera.sample.ui.infinity.contract.NextItemsState
import com.sumera.sample.ui.infinity.contract.OnBottomReached
import com.sumera.sample.ui.infinity.contract.OnItemFavoriteClicked
import com.sumera.sample.ui.infinity.contract.OnRetryLoading
import com.sumera.sample.ui.infinity.contract.OnRetryLoadingNext
import com.sumera.sample.ui.infinity.contract.ShowContent
import com.sumera.sample.ui.infinity.contract.ShowError
import com.sumera.sample.ui.infinity.contract.ShowFavoriteItemError
import com.sumera.sample.ui.infinity.contract.ShowFavoriteItemLoading
import com.sumera.sample.ui.infinity.contract.ShowLoading
import com.sumera.sample.ui.infinity.contract.ShowNextContent
import com.sumera.sample.ui.infinity.contract.ShowNextError
import com.sumera.sample.ui.infinity.contract.ShowNextLoading
import com.sumera.sample.ui.infinity.contract.ShowUpdatedItem

class InfinityLoadingCoreactor(
    private val getItemsInteractor: GetItemsInteractor,
    private val setFavoriteItemStateInteractor: SetFavoriteItemStateInteractor
) : Coreactor<InfinityLoadingState>() {

    override fun createInitialState() = InfinityLoadingState(
        isLoading = false,
        error = null,
        content = emptyList(),
        nextItemsState = NextItemsState(
            isLoading = false,
            error = null
        )
    )

    override fun onLifecycleAction(state: LifecycleState) = coreactorFlow {
        when (state) {
            LifecycleState.ON_START -> {
                loadInitialData()
            }
        }
    }

    override fun onAction(action: Action<InfinityLoadingState>) = coreactorFlow {
        when (action) {
            OnRetryLoading -> {
                loadInitialData()
            }
            OnRetryLoadingNext -> {
                loadNextData()
            }
            OnBottomReached -> {
                loadNextData()
            }
            is OnItemFavoriteClicked -> {
                setItemFavoriteState(action.item)
            }
        }
    }

    private suspend fun CoreactorFlow<InfinityLoadingState>.loadInitialData() {
        if (state.canLoadInitialData) {
            emit(ShowLoading)
            getItemsInteractor.execute(0).unwrap(
                onValue = { value ->
                    val wrappedItems = value.map { ItemWrapper(it, false, null) }
                    emit(ShowContent(wrappedItems))
                },
                onError = { error ->
                    emit(ShowError(error))
                }
            )
        }
    }

    private suspend fun CoreactorFlow<InfinityLoadingState>.loadNextData() {
        if (state.canLoadNextItems) {
            emit(ShowNextLoading)
            getItemsInteractor.execute(state.content.size).unwrap(
                onValue = { items ->
                    val wrappedItems = items.map { ItemWrapper(it, false, null) }
                    emit(ShowNextContent(wrappedItems))
                },
                onError = { error ->
                    emit(ShowNextError(error))
                }
            )
        }
    }

    private suspend fun CoreactorFlow<InfinityLoadingState>.setItemFavoriteState(itemWrapper: ItemWrapper) {
        if (!itemWrapper.isSavingNewFavoriteState) {
            emit(ShowFavoriteItemLoading(itemWrapper))
            val item = itemWrapper.item
            setFavoriteItemStateInteractor.execute(item, !item.isFavorite).unwrap(
                onValue = { updatedItem ->
                    emit(ShowUpdatedItem(updatedItem))
                },
                onError = { error ->
                    emit(ShowFavoriteItemError(itemWrapper, error))
                }
            )
        }
    }

    private val InfinityLoadingState.canLoadInitialData: Boolean get() {
        return content.isEmpty() && !isLoading && error == null
    }

    private val InfinityLoadingState.canLoadNextItems: Boolean get() {
        return !canLoadInitialData && !nextItemsState.isLoading && nextItemsState.error == null
    }
}