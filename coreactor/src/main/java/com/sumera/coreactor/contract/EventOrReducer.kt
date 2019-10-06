package com.sumera.coreactor.contract

import com.sumera.coreactor.contract.event.Event
import com.sumera.coreactor.contract.reducer.Reducer
import com.sumera.coreactor.contract.state.State
import com.sumera.coreactor.internal.Either

interface EventOrReducer<STATE : State> {

    val toEither: Either<Reducer<STATE>, Event<STATE>>
}
