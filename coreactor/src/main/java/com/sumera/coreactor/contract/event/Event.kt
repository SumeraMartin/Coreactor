package com.sumera.coreactor.contract.event

import com.sumera.coreactor.contract.EventOrReducer
import com.sumera.coreactor.contract.state.State
import com.sumera.coreactor.contract.reducer.Reducer
import com.sumera.coreactor.internal.Either

abstract class Event<STATE : State> : EventOrReducer<STATE> {

    open val behaviour = EventBehaviour.DISPATCH_EVERY_TIME

    override val toEither: Either<Reducer<STATE>, Event<STATE>>
        get() = Either.Right(this)
}
