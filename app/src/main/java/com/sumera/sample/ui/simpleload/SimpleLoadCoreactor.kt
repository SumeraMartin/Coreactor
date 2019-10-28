package com.sumera.sample.ui.simpleload

import com.sumera.coreactor.Coreactor
import com.sumera.coreactor.contract.action.Action
import com.sumera.sample.interactors.LoadSimpleDataInteractor
import com.sumera.sample.ui.simpleload.contract.RetryLoadData
import com.sumera.sample.ui.simpleload.contract.ShowDataState
import com.sumera.sample.ui.simpleload.contract.ShowErrorState
import com.sumera.sample.ui.simpleload.contract.ShowLoadingState
import com.sumera.sample.ui.simpleload.contract.ShowToast
import com.sumera.sample.ui.simpleload.contract.SimpleLoadState
import com.sumera.sample.ui.simpleload.contract.StartLoadData
import kotlinx.coroutines.launch

class SimpleLoadCoreactor(
    private val loadSimpleDataInteractor: LoadSimpleDataInteractor
) : Coreactor<SimpleLoadState>() {

    override fun createInitialState() = SimpleLoadState(
        isInitialState = true,
        data = null,
        isLoading = false,
        error = null
    )

    override fun onAction(action: Action<SimpleLoadState>) {
        when (action) {
            StartLoadData -> {
                loadData()
            }
            RetryLoadData -> {
                loadData()
            }
        }
    }

    private fun loadData() {
        emit(ShowLoadingState)

        launch {
            loadSimpleDataInteractor.execute().unwrap(
                onValue = { value ->
                    emit(ShowDataState(value))
                    emit(ShowToast("Data loaded"))
                },
                onError = { error ->
                    emit(ShowErrorState(error))
                }
            )
        }
    }
}
