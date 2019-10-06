package com.sumera.coreactor

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import com.sumera.coreactor.contract.EventOrReducer
import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.event.Event
import com.sumera.coreactor.contract.event.EventBehaviour
import com.sumera.coreactor.contract.reducer.ActionReducer
import com.sumera.coreactor.contract.reducer.Reducer
import com.sumera.coreactor.contract.state.State
import com.sumera.coreactor.error.CoreactorException
import com.sumera.coreactor.interceptor.CoreactorInterceptor
import com.sumera.coreactor.internal.Either
import com.sumera.coreactor.lifecycle.LifecycleState
import com.sumera.coreactor.log.CoreactorLogger
import com.sumera.coreactor.log.implementation.NoOpLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

abstract class Coreactor<STATE : State> : ViewModel(), LifecycleObserver, CoroutineScope by MainScope() {

    protected val state: STATE get() {
        return stateHandler.currentState
    }

    protected val lifecycleState: LifecycleState get() {
        return lifecycleStateHandler.currentLifecycleState
    }

    protected val actionChannel: ReceiveChannel<Action<STATE>> get() {
        return actionHandler.actionChannel
    }

    protected val lifecycleChannel: ReceiveChannel<LifecycleState> get() {
        return lifecycleStateHandler.lifecycleChannel
    }

    protected val eventChannel: ReceiveChannel<Event<STATE>> get() {
        return eventHandler.eventChannel
    }

    protected val reducerChannel: ReceiveChannel<Reducer<STATE>> get() {
        return reducerHandler.reducerChannel
    }

    protected val stateChannel: ReceiveChannel<STATE> get() {
        return stateHandler.stateChannel
    }

    protected open val logger: CoreactorLogger<STATE> = NoOpLogger()

    protected open val interceptor: CoreactorInterceptor<STATE>? = null

    private var isNewlyCreated = true

    private val viewHandler = ViewHandler()

    private val eventHandler = EventHandler()

    private val stateHandler = StateHandler()

    private val actionHandler = ActionHandler()

    private val reducerHandler = ReducerHandler()

    private val lifecycleStateHandler = LifecycleStateHandler()

    abstract fun createInitialState(): STATE

    abstract fun onAction(action: Action<STATE>): Flow<EventOrReducer<STATE>>

    //region Public methods
    fun attachView(coreactorView: CoreactorView<STATE>) {
        viewHandler.setView(coreactorView)

        if (isNewlyCreated) {
            stateHandler.setInitialState(createInitialState())
            lifecycleStateHandler.dispatchLifecycleState(LifecycleState.ON_ATTACH)
            isNewlyCreated = false
        }
    }

    fun detachView(isFinishing: Boolean) {
        if (isFinishing) {
            lifecycleStateHandler.dispatchLifecycleState(LifecycleState.ON_DETACH)
            cancelActiveCoroutines()
        }
        viewHandler.unsetView()
    }

    fun sendAction(action: Action<STATE>) = launch {
        actionHandler.dispatchAction(action)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    protected fun onAny(@Suppress("UNUSED_PARAMETER") source: LifecycleOwner, event: Lifecycle.Event) {
        val lifecycleState = LifecycleState.fromLifecycle(event)
        lifecycleStateHandler.dispatchLifecycleState(lifecycleState)
    }

    protected open fun onLifecycleAction(state: LifecycleState): Flow<EventOrReducer<STATE>> {
        return emptyFlow()
    }
    //endregion

    //region Protected extensions
    protected fun coreactorFlow(block: suspend FlowCollector<EventOrReducer<STATE>>.() -> Unit): Flow<EventOrReducer<STATE>> {
        return flow(block)
    }

    protected fun reducer(reducerAction: (STATE) -> STATE): ActionReducer<STATE> {
        return ActionReducer { state -> reducerAction(state) }
    }

    protected suspend fun FlowCollector<EventOrReducer<STATE>>.emitReducer(reducerAction: (STATE) -> STATE) = apply {
        emit(reducer(reducerAction))
    }

    protected suspend fun FlowCollector<EventOrReducer<STATE>>.emitFrom(emitAction: () -> EventOrReducer<STATE>) {
        emit(emitAction())
    }

    protected suspend fun waitUntilState(state: STATE): STATE {
        return waitUntilState { it == state }
    }

    protected suspend fun waitUntilState(predicate: suspend (STATE) -> Boolean): STATE {
        var resultState: STATE? = null
        stateChannel.consumeAsFlow().filter(predicate).take(1).collect { state ->
            resultState = state
        }
        return resultState ?: throw CoreactorException()
    }

    protected suspend fun waitUntilReducer(action: Reducer<STATE>): Reducer<STATE> {
        return waitUntilReducer { it == action }
    }

    protected suspend fun waitUntilReducer(predicate: suspend (Reducer<STATE>) -> Boolean): Reducer<STATE> {
        var resultReducer: Reducer<STATE>? = null
        reducerChannel.consumeAsFlow().filter(predicate).take(1).collect { reducer ->
            resultReducer = reducer
        }
        return resultReducer ?: throw CoreactorException()
    }

    protected suspend fun waitUntilEvent(event: Event<STATE>): Event<STATE> {
        return waitUntilEvent { it == event }
    }

    protected suspend fun waitUntilEvent(predicate: suspend (Event<STATE>) -> Boolean): Event<STATE> {
        var resultEvent: Event<STATE>? = null
        eventChannel.consumeAsFlow().filter(predicate).take(1).collect { event ->
            resultEvent = event
        }
        return resultEvent ?: throw CoreactorException()
    }

    protected suspend fun waitUntilAction(action: Action<STATE>): Action<STATE> {
        return waitUntilAction { it == action }
    }

    protected suspend fun waitUntilAction(predicate: suspend (Action<STATE>) -> Boolean): Action<STATE> {
        var resultAction: Action<STATE>? = null
        actionChannel.consumeAsFlow().filter(predicate).take(1).collect { action ->
            resultAction = action
        }
        return resultAction ?: throw CoreactorException()
    }

    protected suspend fun waitUntilLifecycle(state: LifecycleState) {
        waitUntilLifecycle { it == state }
    }

    protected suspend fun waitUntilLifecycle(predicate: suspend (LifecycleState) -> Boolean) {
        lifecycleChannel.consumeAsFlow().filter(predicate).take(1).collect()
    }
    //endregion

    //region Private methods
    private fun cancelActiveCoroutines() {
        cancel("View attached to this coreactor will be destroyed")
    }

    private suspend fun Flow<EventOrReducer<STATE>>.collectEventOrReducerFlow() {
        collect { eitherEventOrReducer ->
            when (val eventOrReducer = eitherEventOrReducer.toEither) {
                is Either.Left -> {
                    reducerHandler.dispatchReducer(eventOrReducer.value)
                }
                is Either.Right -> {
                    eventHandler.dispatchEvent(eventOrReducer.value)
                }
            }
        }
    }

    private fun <E> BroadcastChannel<E>.sendBlocking(value: E) {
        runBlocking { send(value) }
    }
    //endregion

    //region Handlers
    private inner class ViewHandler {

        private var view: CoreactorView<STATE>? = null

        fun setView(newView: CoreactorView<STATE>) {
            view = newView
        }

        fun unsetView() {
            view = null
        }

        fun getViewOrThrow(): CoreactorView<STATE> {
            return view ?: throw CoreactorException()
        }
    }

    private inner class EventHandler {

        val eventChannel: ReceiveChannel<Event<STATE>> get() {
            return eventChannelInternal.openSubscription()
        }

        private val eventChannelInternal = BroadcastChannel<Event<STATE>>(1)

        private val eventsWaitingForCreatedState = mutableListOf<Event<STATE>>()

        private val eventsWaitingForStartedState = mutableListOf<Event<STATE>>()

        fun dispatchEventsWaitingForCreatedState() {
            eventsWaitingForCreatedState.apply {
                forEach { event -> dispatchEvent(event) }
                clear()
            }
        }

        fun dispatchEventsWaitingForStartedState() {
            eventsWaitingForStartedState.apply {
                forEach { event -> dispatchEvent(event) }
                clear()
            }
        }

        fun dispatchEvent(event: Event<STATE>) {
            logger.onEventEmitted(event)

            when (EventDispatchState.from(lifecycleStateHandler.currentLifecycleState, event.behaviour)) {
                EventDispatchState.DISPATCH_NOW -> {
                    dispatchNow(event)
                }
                EventDispatchState.DISPATCH_LATER_TO_STARTED_VIEW -> {
                    dispatchLaterWhenStarted(event)
                }
                EventDispatchState.DISPATCH_LATER_TO_CREATED_VIEW -> {
                    dispatchLaterWhenCreated(event)
                }
                EventDispatchState.THROW_AWAY -> {
                    throwAway(event)
                }
            }
        }

        private fun dispatchNow(event: Event<STATE>) {
            eventChannelInternal.sendBlocking(event)
            viewHandler.getViewOrThrow().onEvent(event)
            logger.onEventDispatchedToView(event)
        }

        private fun dispatchLaterWhenStarted(event: Event<STATE>) {
            eventsWaitingForStartedState.add(event)
            logger.onEventWaitingForStartedView(event)
        }

        private fun dispatchLaterWhenCreated(event: Event<STATE>) {
            eventsWaitingForCreatedState.add(event)
            logger.onEventWaitingForCreatedView(event)
        }

        private fun throwAway(event: Event<STATE>) {
            logger.onEventThrownAway(event)
        }
    }

    private inner class StateHandler {

        val stateChannel: ReceiveChannel<STATE> get() {
            return stateChannelInternal.openSubscription()
        }

        val currentState: STATE get() {
            return currentStateInternal ?: throw CoreactorException("State is not initialized yet")
        }

        private var currentStateInternal: STATE? = null

        private val stateChannelInternal = BroadcastChannel<STATE>(1)

        fun getStateOrThrow(): STATE {
            return currentStateInternal ?: throw CoreactorException()
        }

        fun setInitialState(state: STATE) {
            setCurrentState(state)
        }

        fun dispatchStateWaitingForStartedState() {
            dispatchStateToViewNow(currentState)
        }

        fun dispatchStateIfPossible(state: STATE) {
            if (lifecycleState.isInStartedState) {
                setCurrentState(state)
                dispatchStateToViewNow(state)
            } else {
                setCurrentState(state)
            }
        }

        private fun setCurrentState(state: STATE) {
            currentStateInternal = state
            stateChannelInternal.sendBlocking(state)

            logger.onNewStateReceived(state)
        }

        private fun dispatchStateToViewNow(state: STATE) {
            viewHandler.getViewOrThrow().onState(state)
            logger.onStateDispatchedToView(state)
        }
    }

    private inner class LifecycleStateHandler {

        val lifecycleChannel: ReceiveChannel<LifecycleState> get() {
            return lifecycleStateChannelInternal.openSubscription()
        }

        var currentLifecycleState: LifecycleState = LifecycleState.INITIAL

        private val lifecycleStateChannelInternal = BroadcastChannel<LifecycleState>(1)

        fun dispatchLifecycleState(lifecycleState: LifecycleState) = launch {
            logger.onLifecycle(lifecycleState)

            when {
                currentLifecycleState.isCreateState -> {
                    eventHandler.dispatchEventsWaitingForCreatedState()
                }
                currentLifecycleState.isStartState -> {
                    stateHandler.dispatchStateWaitingForStartedState()
                    eventHandler.dispatchEventsWaitingForStartedState()
                }
            }

            currentLifecycleState = lifecycleState
            lifecycleStateChannelInternal.sendBlocking(lifecycleState)

            onLifecycleAction(lifecycleState).collectEventOrReducerFlow()
        }
    }

    private inner class ReducerHandler {

        val reducerChannel: ReceiveChannel<Reducer<STATE>> get() {
            return reducerChannelInternal.openSubscription()
        }

        private val reducerChannelInternal = BroadcastChannel<Reducer<STATE>>(1)

        fun dispatchReducer(reducer: Reducer<STATE>) {
            val oldState = stateHandler.getStateOrThrow()
            val newState = reducer.reduce(oldState)

            reducerChannelInternal.sendBlocking(reducer)
            stateHandler.dispatchStateIfPossible(newState)

            logger.onReducer(oldState, reducer, newState)
        }
    }

    private inner class ActionHandler {

        val actionChannel: ReceiveChannel<Action<STATE>> get() {
            return actionChannelInternal.openSubscription()
        }

        private val actionChannelInternal = BroadcastChannel<Action<STATE>>(1)

        suspend fun dispatchAction(action: Action<STATE>) {
            logger.onAction(action)

            actionChannelInternal.sendBlocking(action)
            onAction(action).collectEventOrReducerFlow()
        }
    }

    private enum class EventDispatchState {
        DISPATCH_NOW, DISPATCH_LATER_TO_STARTED_VIEW, DISPATCH_LATER_TO_CREATED_VIEW, THROW_AWAY;

        companion object {
            fun from(lifecycleState: LifecycleState, eventBehaviour: EventBehaviour): EventDispatchState {
                return when (eventBehaviour) {
                    EventBehaviour.DISPATCH_TO_STARTED_OR_WAIT -> {
                        if (lifecycleState.isInStartedState) {
                            DISPATCH_NOW
                        } else {
                            DISPATCH_LATER_TO_STARTED_VIEW
                        }
                    }
                    EventBehaviour.DISPATCH_TO_STARTED_OR_THROW_AWAY -> {
                        if (lifecycleState.isInStartedState) {
                            DISPATCH_NOW
                        } else {
                            THROW_AWAY
                        }
                    }
                    EventBehaviour.DISPATCH_EVERY_TIME -> {
                        if (lifecycleState.isInCreatedState) {
                            DISPATCH_NOW
                        } else {
                            DISPATCH_LATER_TO_CREATED_VIEW
                        }
                    }
                }
            }
        }
    }
    //endregion
}
