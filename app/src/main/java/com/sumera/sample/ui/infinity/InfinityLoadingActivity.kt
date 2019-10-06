package com.sumera.sample.ui.infinity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.sumera.coreactor.CoreactorActivity
import com.sumera.coreactor.CoreactorView
import com.sumera.coreactor.contract.event.Event
import com.sumera.sample.ui.infinity.adapter.InfinityLoadingAdapter
import com.sumera.sample.ui.infinity.contract.InfinityLoadingState
import com.sumera.sample.ui.infinity.contract.OnBottomReached
import com.sumera.sample.ui.infinity.contract.OnItemFavoriteClicked
import com.sumera.sample.ui.infinity.contract.OnRetryLoadingNext
import kotlinx.android.synthetic.main.activity_infinity.*

class InfinityLoadingActivity : CoreactorActivity<InfinityLoadingState>(), CoreactorView<InfinityLoadingState> {

    override val coreactorFactory = InfinityLoadingCoreactorFactory()

    override val coreactorView = this

    override fun layoutRes() = com.sumera.sample.R.layout.activity_infinity

    private val adapter: InfinityLoadingAdapter = InfinityLoadingAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        infinity_recycler.layoutManager = LinearLayoutManager(this)
        infinity_recycler.adapter = adapter
        adapter.onItemClickListener = { sendAction(OnItemFavoriteClicked(it)) }
        adapter.onRetryLoadingClickLister = { sendAction(OnRetryLoadingNext) }
        adapter.onLastItemIsShown = { sendAction(OnBottomReached) }
    }

    override fun onState(state: InfinityLoadingState) {
        setPlaceholderState(state)
        setRecyclerState(state)
    }

    override fun onEvent(event: Event<InfinityLoadingState>) {
        // No events
    }

    private fun setPlaceholderState(state: InfinityLoadingState) {
        val text = when {
            state.isLoading -> "Loading"
            state.error != null -> "Error"
            else -> ""
        }
        infinity_placeholder.text = text
        infinity_placeholder.visibility = if (text.isEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun setRecyclerState(state: InfinityLoadingState) {
        adapter.state = state.nextItemsState
        adapter.data = state.content
    }
}
