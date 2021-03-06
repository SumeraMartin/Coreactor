package com.sumera.sample.ui.infinity

import com.sumera.coreactor.Coreactor
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
import kotlinx.coroutines.launch

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

    override fun onLifecycleState(state: LifecycleState) {
        when (state) {
            LifecycleState.ON_START -> {
                loadInitialDataWhenViewIsStarted()
            }
        }
    }

    override fun onAction(action: Action<InfinityLoadingState>) {
        when (action) {
            OnRetryLoading -> {
                loadInitialDataWhenRetryIsClicked()
            }
            OnRetryLoadingNext -> {
                loadNextDataWhenRetryIsClicked()
            }
            OnBottomReached -> {
                loadNextDataWhenReachedBottom()
            }
            is OnItemFavoriteClicked -> {
                setItemFavoriteState(action.item)
            }
        }
    }

    private fun loadInitialDataWhenViewIsStarted() {
        if (state.canLoadInitialDataWhenViewIsStarted) {
            loadInitialData()
        }
    }

    private fun loadInitialDataWhenRetryIsClicked() {
        if (state.canLoadInitialDataWhenRetryIsClicked) {
            loadInitialData()
        }
    }

    private fun loadInitialData() {
        emit(ShowLoading)

        launch {
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

    private fun loadNextDataWhenRetryIsClicked() {
        if (state.canLoadNextItemsWhenRetryIsClicked) {
            loadNextData()
        }
    }

    private fun loadNextDataWhenReachedBottom() {
        if (state.canLoadNextItemsWhenBottomIsReached) {
            loadNextData()
        }
    }

    private fun loadNextData() {
        emit(ShowNextLoading)

        launch {
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

    private fun setItemFavoriteState(itemWrapper: ItemWrapper) {
        if (!itemWrapper.isSavingNewFavoriteState) {
            emit(ShowFavoriteItemLoading(itemWrapper))
            val item = itemWrapper.item

            launch {
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
    }

    private val InfinityLoadingState.canLoadInitialDataWhenViewIsStarted: Boolean get() {
        return content.isEmpty() && !isLoading && error == null
    }

    private val InfinityLoadingState.canLoadInitialDataWhenRetryIsClicked: Boolean get() {
        return content.isEmpty() && !isLoading && error != null
    }

    private val InfinityLoadingState.canLoadNextItemsWhenRetryIsClicked: Boolean get() {
        return !isLoading && error == null && !nextItemsState.isLoading && nextItemsState.error != null
    }

    private val InfinityLoadingState.canLoadNextItemsWhenBottomIsReached: Boolean get() {
        return !isLoading && error == null && !nextItemsState.isLoading && nextItemsState.error == null
    }
}