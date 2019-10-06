package com.sumera.sample.ui.infinity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sumera.sample.ui.infinity.contract.NextItemsState
import kotlinx.android.synthetic.main.view_holder_next.view.*

class LoadingNextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        fun create(parent: ViewGroup): LoadingNextViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(com.sumera.sample.R.layout.view_holder_next, parent, false)
            return LoadingNextViewHolder(view)
        }
    }

    fun bind(state: NextItemsState, onRetryLoadingClickLister: () -> Unit) {
        when {
            state.isLoading -> {
                itemView.nextViewHolder_title.text = "Loading"
                itemView.visibility = View.VISIBLE
                itemView.setOnClickListener(null)
            }
            state.error != null -> {
                itemView.nextViewHolder_title.text = "Error"
                itemView.visibility = View.VISIBLE
                itemView.setOnClickListener { onRetryLoadingClickLister() }
            }
            else -> {
                itemView.nextViewHolder_title.text = ""
                itemView.visibility = View.GONE
                itemView.setOnClickListener(null)
            }
        }
    }
}
