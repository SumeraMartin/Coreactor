package com.sumera.sample.ui.infinity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sumera.sample.ui.infinity.contract.ItemWrapper
import kotlinx.android.synthetic.main.view_holder_item.view.*

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        fun create(parent: ViewGroup): ItemViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(com.sumera.sample.R.layout.view_holder_item, parent, false)
            return ItemViewHolder(view)
        }
    }

    fun bind(itemWrapper: ItemWrapper, onItemClickListener: (ItemWrapper) -> Unit) {
        itemView.itemViewHolder_title.text = itemWrapper.item.name
        itemView.itemViewHolder_favoriteState.text = when {
            itemWrapper.isSavingNewFavoriteState -> "Loading"
            itemWrapper.errorSavingFavoriteState != null -> "Error"
            itemWrapper.item.isFavorite -> "FAVORITE"
            !itemWrapper.item.isFavorite -> "not favorite"
            else -> ""
        }
        itemView.itemViewHolder_favoriteState.setOnClickListener {
            onItemClickListener(itemWrapper)
        }
    }
}
