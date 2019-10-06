package com.sumera.coreactor.contract.reducer

import com.sumera.coreactor.contract.EventOrReducer
import com.sumera.coreactor.contract.state.State
import com.sumera.coreactor.contract.event.Event
import com.sumera.coreactor.internal.Either

abstract class Reducer<STATE: State> : EventOrReducer<STATE> {

    abstract fun reduce(oldState: STATE): STATE

    override val toEither: Either<Reducer<STATE>, Event<STATE>>
        get() = Either.Left(this)
}
