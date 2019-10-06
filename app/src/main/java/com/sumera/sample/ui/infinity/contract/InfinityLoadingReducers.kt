package com.sumera.sample.ui.infinity.contract

import com.sumera.coreactor.contract.reducer.Reducer
import com.sumera.sample.data.Item
import com.sumera.sample.tools.extensions.replace

object ShowLoading : Reducer<InfinityLoadingState>() {
    override fun reduce(oldState: InfinityLoadingState): InfinityLoadingState {
        return oldState.copy(
            isLoading = true,
            error = null,
            content = emptyList(),
            nextItemsState = NextItemsState(
                isLoading = false,
                error = null
            )
        )
    }
}
data class ShowError(private val error: Throwable) : Reducer<InfinityLoadingState>() {
    override fun reduce(oldState: InfinityLoadingState): InfinityLoadingState {
        return oldState.copy(
            isLoading = false,
            error = error
        )
    }
}
data class ShowContent(private val content: List<ItemWrapper>) : Reducer<InfinityLoadingState>() {
    override fun reduce(oldState: InfinityLoadingState): InfinityLoadingState {
        return oldState.copy(
            isLoading = false,
            error = null,
            content = content
        )
    }
}
object ShowNextLoading : Reducer<InfinityLoadingState>() {
    override fun reduce(oldState: InfinityLoadingState): InfinityLoadingState {
        return oldState.copy(
            nextItemsState = NextItemsState(
                isLoading = true,
                error = null
            )
        )
    }
}
data class ShowNextError(private val error: Throwable) : Reducer<InfinityLoadingState>() {
    override fun reduce(oldState: InfinityLoadingState): InfinityLoadingState {
        return oldState.copy(
            nextItemsState = NextItemsState(
                isLoading = false,
                error = error
            )
        )
    }
}
data class ShowNextContent(private val nextContent: List<ItemWrapper>) : Reducer<InfinityLoadingState>() {
    override fun reduce(oldState: InfinityLoadingState): InfinityLoadingState {
        return oldState.copy(
            content = oldState.content + nextContent,
            nextItemsState = oldState.nextItemsState.copy(isLoading = false, error = null)
        )
    }
}
data class ShowFavoriteItemLoading(private val item: ItemWrapper) : Reducer<InfinityLoadingState>() {
    override fun reduce(oldState: InfinityLoadingState): InfinityLoadingState {
        val newItemWrapper = item.copy(isSavingNewFavoriteState = true, errorSavingFavoriteState = null)
        val newContent = oldState.content.replace(newItemWrapper) { it.item.id == newItemWrapper.item.id }
        return oldState.copy(content = newContent)
    }
}
data class ShowFavoriteItemError(private val item: ItemWrapper, private val error: Throwable) : Reducer<InfinityLoadingState>() {
    override fun reduce(oldState: InfinityLoadingState): InfinityLoadingState {
        val newItemWrapper = item.copy(isSavingNewFavoriteState = false, errorSavingFavoriteState = error)
        val newContent = oldState.content.replace(newItemWrapper) { it.item.id == newItemWrapper.item.id }
        return oldState.copy(content = newContent)
    }
}
data class ShowUpdatedItem(private val item: Item) : Reducer<InfinityLoadingState>() {
    override fun reduce(oldState: InfinityLoadingState): InfinityLoadingState {
        val newItemWrapper = ItemWrapper(item = item, isSavingNewFavoriteState = false, errorSavingFavoriteState = null)
        val newContent = oldState.content.replace(newItemWrapper) { it.item.id == newItemWrapper.item.id }
        return oldState.copy(content = newContent)
    }
}
