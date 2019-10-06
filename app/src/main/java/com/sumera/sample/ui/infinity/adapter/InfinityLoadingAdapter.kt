package com.sumera.sample.ui.infinity.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sumera.sample.ui.infinity.contract.ItemWrapper
import com.sumera.sample.ui.infinity.contract.NextItemsState

class InfinityLoadingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val FOOTER_SIZE = 1
        const val ITEM_TYPE = 100
        const val NEXT_TYPE = 101
    }

    var onItemClickListener: (ItemWrapper) -> Unit = {}

    var onRetryLoadingClickLister: () -> Unit = {}

    var onLastItemIsShown: () -> Unit = {}

    var data: List<ItemWrapper> = emptyList()
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    var state: NextItemsState = NextItemsState(isLoading = false, error = null)
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == NEXT_TYPE) {
            LoadingNextViewHolder.create(parent)
        } else {
            ItemViewHolder.create(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LoadingNextViewHolder -> {
                holder.bind(state, onRetryLoadingClickLister)
                checkIfLastItemShownListenerShouldBeNotified()
            }
            is ItemViewHolder -> {
                holder.bind(data[position], onItemClickListener)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= data.size) {
            NEXT_TYPE
        } else {
            ITEM_TYPE
        }
    }

    override fun getItemCount(): Int {
        return data.size + FOOTER_SIZE
    }

    private fun checkIfLastItemShownListenerShouldBeNotified() {
        if (data.isNotEmpty()) {
            onLastItemIsShown()
        }
    }
}
