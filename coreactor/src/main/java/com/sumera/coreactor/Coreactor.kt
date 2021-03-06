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
import com.sumera.coreactor.interceptor.implementation.SimpleInterceptor
import com.sumera.coreactor.internal.Either
import com.sumera.coreactor.internal.assert.requireMainThread
import com.sumera.coreactor.lifecycle.LifecycleState
import com.sumera.coreactor.log.CoreactorLogger
import com.sumera.coreactor.log.implementation.NoOpLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

abstract class Coreactor<STATE : State> : ViewModel(), LifecycleObserver, CoroutineScope by MainScope() {

    protected val state: STATE get() {
        return stateHandler.currentState
    }

    protected val lifecycleState: LifecycleState get() {
        return lifecycleStateHandler.currentLifecycleState
    }

    protected fun openActionSubscription(): ReceiveChannel<Action<STATE>> {
        return actionHandler.openActionSubscription()
    }

    protected fun openLifecycleSubscription(): ReceiveChannel<LifecycleState> {
        return lifecycleStateHandler.openLifecycleSubscription()
    }

    protected fun openEventSubscription(): ReceiveChannel<Event<STATE>> {
        return eventHandler.openEventSubscription()
    }

    protected fun openReducerSubscription(): ReceiveChannel<Reducer<STATE>> {
        return reducerHandler.openReducerSubscription()
    }

    protected fun openStateSubscription(): ReceiveChannel<STATE> {
        return stateHandler.openStateSubscription()
    }

    protected open val logger: CoreactorLogger<STATE> = NoOpLogger()

    protected open val interceptor: CoreactorInterceptor<STATE> = SimpleInterceptor()

    private val viewHandler = ViewHandler()

    private val eventHandler = EventHandler()

    private val stateHandler = StateHandler()

    private val actionHandler = ActionHandler()

    private val reducerHandler = ReducerHandler()

    private val lifecycleStateHandler = LifecycleStateHandler()

    private val scopedJobsDispatcher = ScopedJobsDispatcher()

    abstract fun createInitialState(): STATE

    abstract fun onAction(action: Action<STATE>)

    //region Public methods
    fun attachView(coreactorView: CoreactorView<STATE>) {
        requireMainThread("attachView")

        viewHandler.setView(coreactorView)

        if (lifecycleState.isInitialState) {
            stateHandler.setInitialState(createInitialState())
            lifecycleStateHandler.dispatchLifecycleState(LifecycleState.ON_ATTACH)
        }
    }

    fun sendAction(action: Action<STATE>) {
        requireMainThread("sendAction")

        if (lifecycleState.isInitialState) {
            throw CoreactorException("sendAction shouldn't be called before attachView")
        }
        if (lifecycleState.isDetachState) {
            throw CoreactorException("sendAction shouldn't be called after detachView(true)")
        }
        actionHandler.dispatchAction(action)
    }
    //endregion

    //region Overrides
    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    protected fun onAny(@Suppress("UNUSED_PARAMETER") source: LifecycleOwner, event: Lifecycle.Event) {
        val lifecycleState = LifecycleState.fromLifecycle(event)
        lifecycleStateHandler.dispatchLifecycleState(lifecycleState)
    }

    final override fun onCleared() {
        lifecycleStateHandler.dispatchLifecycleState(LifecycleState.ON_DETACH)
        cancelActiveCoroutines()
    }
    //endregion

    //region Protected methods
    protected open fun onState(state: STATE) {
        // NoOp
    }

    protected open fun onLifecycleState(state: LifecycleState) {
        // NoOp
    }

    protected fun launchWhenResumed(block: suspend () -> Unit) {
        requireMainThread("launchWhenResumed")
        scopedJobsDispatcher.startOrWaitUntilResumedState(block)
    }

    protected fun launchWhenStarted(block: suspend () -> Unit) {
        requireMainThread("launchWhenStarted")
        scopedJobsDispatcher.startOrWaitUntilStartedState(block)
    }

    protected fun launchWhenCreated(block: suspend () -> Unit) {
        requireMainThread("launchWhenCreated")
        scopedJobsDispatcher.startOrWaitUntilCreatedState(block)
    }

    protected fun onLifecycleException(error: Throwable) {
        throw error
    }

    protected fun emit(block: () -> EventOrReducer<STATE>) {
        emit(block())
    }

    protected fun emit(eventOrReducer: EventOrReducer<STATE>) {
        requireMainThread("emit")
        dispatchEventOrReducer(eventOrReducer)
    }

    protected fun emitReducer(reducerBlock: (STATE) -> STATE) = apply {
        emit(reducer(reducerBlock))
    }

    protected fun coreactorFlow(block: suspend FlowCollector<EventOrReducer<STATE>>.() -> Unit): Flow<EventOrReducer<STATE>> {
        return flow(block)
    }

    protected fun reducer(reducerBlock: (STATE) -> STATE): ActionReducer<STATE> {
        return ActionReducer { state -> reducerBlock(state) }
    }

    protected suspend fun waitUntilState(predicate: (STATE) -> Boolean): STATE {
        return waitUntil(openStateSubscription(), predicate)
    }

    protected suspend inline fun <reified EXPECTED_REDUCER : Reducer<STATE>> waitUntilReducerType(): EXPECTED_REDUCER {
        return waitUntil(openReducerSubscription()) { it is EXPECTED_REDUCER } as? EXPECTED_REDUCER ?: throw CoreactorException()
    }

    protected suspend fun waitUntilReducer(predicate: (Reducer<STATE>) -> Boolean): Reducer<STATE> {
        return waitUntil(openReducerSubscription(), predicate)
    }

    protected suspend inline fun <reified EXPECTED_EVENT : Event<STATE>> waitUntilEventType(): EXPECTED_EVENT {
        return waitUntilEvent { it is EXPECTED_EVENT } as? EXPECTED_EVENT ?: throw CoreactorException()
    }

    protected suspend fun waitUntilEvent(predicate: (Event<STATE>) -> Boolean): Event<STATE> {
        return waitUntil(openEventSubscription(), predicate)
    }

    protected suspend inline fun <reified EXPECTED_ACTION : Action<STATE>> waitUntilActionType(): EXPECTED_ACTION {
        return waitUntilAction { it is EXPECTED_ACTION } as? EXPECTED_ACTION ?: throw CoreactorException()
    }

    protected suspend fun waitUntilAction(predicate: (Action<STATE>) -> Boolean): Action<STATE> {
        return waitUntil(openActionSubscription(), predicate)
    }

    protected suspend fun waitUntilLifecycle(lifecycleState: LifecycleState): LifecycleState {
        return waitUntilLifecycle { it == lifecycleState }
    }

    protected suspend fun waitUntilLifecycle(predicate: (LifecycleState) -> Boolean): LifecycleState {
        return waitUntil(openLifecycleSubscription(), predicate)
    }

    protected suspend fun <VALUE> waitUntil(subscription: ReceiveChannel<VALUE>, predicate: (VALUE) -> Boolean): VALUE {
        requireMainThread("waitUntil")
        while (true) {
            val value = subscription.receive()
            if (predicate(value)) {
                subscription.cancel()
                return value
            }
        }
    }
    //endregion

    //region Private methods
    private fun cancelActiveCoroutines() {
        cancel("The coreactor is being destroyed.")
    }

    private fun dispatchEventOrReducer(eventOrReducer: EventOrReducer<STATE>) {
        when (val either = eventOrReducer.toEither) {
            is Either.Left -> {
                reducerHandler.dispatchReducer(either.value)
            }
            is Either.Right -> {
                eventHandler.dispatchEvent(either.value)
            }
        }
    }

    private fun <E> SendChannel<E>.sendInLaunchBlock(value: E) {
        launch { send(value) }
    }
    //endregion

    //region Handlers
    private inner class ViewHandler {

        private var view: CoreactorView<STATE>? = null

        fun setView(newView: CoreactorView<STATE>) {
            if (view != null) {
                throw CoreactorException("View is already set. Make sure that you call attachView only once.")
            }
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

        private val eventChannelInternal = BroadcastChannel<Event<STATE>>(1)

        private val eventsWaitingForCreatedState = mutableListOf<Event<STATE>>()

        private val eventsWaitingForStartedState = mutableListOf<Event<STATE>>()

        fun openEventSubscription(): ReceiveChannel<Event<STATE>> {
            return eventChannelInternal.openSubscription()
        }

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
            val interceptedEvent = interceptor.onInterceptEvent(event) ?: return
            logger.onEventEmitted(interceptedEvent)

            when (EventDispatchState.from(lifecycleStateHandler.currentLifecycleState, interceptedEvent.behaviour)) {
                EventDispatchState.DISPATCH_NOW -> {
                    dispatchNow(interceptedEvent)
                }
                EventDispatchState.DISPATCH_LATER_TO_STARTED_VIEW -> {
                    dispatchLaterWhenStarted(interceptedEvent)
                }
                EventDispatchState.DISPATCH_LATER_TO_CREATED_VIEW -> {
                    dispatchLaterWhenCreated(interceptedEvent)
                }
                EventDispatchState.THROW_AWAY -> {
                    throwAway(interceptedEvent)
                }
            }
        }

        private fun dispatchNow(event: Event<STATE>) {
            logger.onEventDispatchedToView(event)
            eventChannelInternal.sendInLaunchBlock(event)
            viewHandler.getViewOrThrow().onEvent(event)
        }

        private fun dispatchLaterWhenStarted(event: Event<STATE>) {
            logger.onEventWaitingForStartedView(event)
            eventsWaitingForStartedState.add(event)
        }

        private fun dispatchLaterWhenCreated(event: Event<STATE>) {
            logger.onEventWaitingForCreatedView(event)
            eventsWaitingForCreatedState.add(event)
        }

        private fun throwAway(event: Event<STATE>) {
            logger.onEventThrownAway(event)
        }
    }

    private inner class StateHandler {

        val currentState: STATE get() {
            return currentStateInternal ?: throw CoreactorException("State is not initialized yet")
        }

        private var currentStateInternal: STATE? = null

        private val stateChannelInternal = ConflatedBroadcastChannel<STATE>()

        fun openStateSubscription(): ReceiveChannel<STATE> {
            return stateChannelInternal.openSubscription()
        }

        fun getStateOrNull(): STATE? {
            return currentStateInternal
        }

        fun getStateOrThrow(): STATE {
            return currentStateInternal ?: throw CoreactorException()
        }

        fun setInitialState(state: STATE) {
            val interceptedState = interceptor.onInterceptState(state) ?: return
            dispatchStateIfPossible(interceptedState)
        }

        fun dispatchStateIfPossible(state: STATE) {
            val interceptedState = interceptor.onInterceptState(state) ?: return
            if (lifecycleState.isInStartedState) {
                setCurrentState(interceptedState)
                dispatchStateToViewNow(interceptedState)
            } else {
                setCurrentState(interceptedState)
            }
        }

        fun dispatchStateWaitingForStartedState() {
            val state = getStateOrNull()
            if (state != null) {
                dispatchStateToViewNow(state)
            }
        }

        private fun setCurrentState(state: STATE) {
            logger.onNewStateReceived(state)
            currentStateInternal = state
            stateChannelInternal.sendInLaunchBlock(state)
            onState(state)
        }

        private fun dispatchStateToViewNow(state: STATE) {
            logger.onStateDispatchedToView(state)
            viewHandler.getViewOrThrow().onState(state)
        }
    }

    private inner class LifecycleStateHandler {

        var currentLifecycleState: LifecycleState = LifecycleState.INITIAL
            private set

        private val lifecycleStateChannelInternal = ConflatedBroadcastChannel<LifecycleState>()

        fun openLifecycleSubscription(): ReceiveChannel<LifecycleState> {
            return lifecycleStateChannelInternal.openSubscription()
        }

        fun dispatchLifecycleState(lifecycleState: LifecycleState) {
            interceptor.onLifecycleStateChanged(lifecycleState)
            logger.onLifecycle(lifecycleState)

            currentLifecycleState = lifecycleState
            lifecycleStateChannelInternal.sendInLaunchBlock(lifecycleState)
            onLifecycleState(lifecycleState)
            scopedJobsDispatcher.onLifecycleState(lifecycleState)

            when {
                currentLifecycleState.isCreateState -> {
                    eventHandler.dispatchEventsWaitingForCreatedState()
                }
                currentLifecycleState.isStartState -> {
                    stateHandler.dispatchStateWaitingForStartedState()
                    eventHandler.dispatchEventsWaitingForStartedState()
                }
                currentLifecycleState.isDestroyState -> {
                    viewHandler.unsetView()
                }
            }
        }
    }

    private inner class ReducerHandler {

        private val reducerChannelInternal = BroadcastChannel<Reducer<STATE>>(1)

        fun openReducerSubscription(): ReceiveChannel<Reducer<STATE>> {
            return reducerChannelInternal.openSubscription()
        }

        fun dispatchReducer(reducer: Reducer<STATE>) {
            val interceptedReducer = interceptor.onInterceptReducer(reducer) ?: return

            val oldState = stateHandler.getStateOrThrow()
            val newState = interceptedReducer.reduce(oldState)

            logger.onReducer(oldState, interceptedReducer, newState)
            reducerChannelInternal.sendInLaunchBlock(interceptedReducer)
            stateHandler.dispatchStateIfPossible(newState)
        }
    }

    private inner class ActionHandler {

        private val actionChannelInternal = BroadcastChannel<Action<STATE>>(1)

        fun openActionSubscription(): ReceiveChannel<Action<STATE>> {
            return actionChannelInternal.openSubscription()
        }

        fun dispatchAction(action: Action<STATE>) {
            val interceptedAction = interceptor.onInterceptAction(action)
            if (interceptedAction != null) {
                logger.onAction(interceptedAction)
                actionChannelInternal.sendInLaunchBlock(interceptedAction)
                onAction(interceptedAction)
            }
        }
    }

    private inner class ScopedJobsDispatcher {

        private val jobsWaitingForCreatedState = mutableListOf<Job>()

        private val jobsWaitingForStartedState = mutableListOf<Job>()

        private val jobsWaitingForResumedState = mutableListOf<Job>()

        private val jobsRunningUntilPausedState = mutableListOf<Job>()

        private val jobsRunningUntilStoppedState = mutableListOf<Job>()

        private val jobsRunningUntilDestroyedState = mutableListOf<Job>()

        fun startOrWaitUntilResumedState(block: suspend () -> Unit) {
            val job = toLazyJob(block)
            if (lifecycleState.isInResumedState) {
                startJobAndAddToRunningJobs(job, cancelWhen = LifecycleState.ON_PAUSE)
            } else {
                waitUntilExpectedState(job, startWhen = LifecycleState.ON_RESUME)
            }
        }

        fun startOrWaitUntilStartedState(block: suspend () -> Unit) {
            val job = toLazyJob(block)
            if (lifecycleState.isInStartedState) {
                startJobAndAddToRunningJobs(job, cancelWhen = LifecycleState.ON_STOP)
            } else {
                waitUntilExpectedState(job, startWhen = LifecycleState.ON_START)
            }
        }

        fun startOrWaitUntilCreatedState(block: suspend () -> Unit) {
            val job = toLazyJob(block)
            if (lifecycleState.isInCreatedState) {
                startJobAndAddToRunningJobs(job, cancelWhen = LifecycleState.ON_DESTROY)
            } else {
                waitUntilExpectedState(job, startWhen = LifecycleState.ON_CREATE)
            }
        }

        fun onLifecycleState(state: LifecycleState) {
            when (state) {
                LifecycleState.ON_CREATE -> {
                    startAllWaitingJobs(forState = LifecycleState.ON_CREATE, cancelWhen = LifecycleState.ON_DESTROY)
                }
                LifecycleState.ON_START -> {
                    startAllWaitingJobs(forState = LifecycleState.ON_START, cancelWhen = LifecycleState.ON_STOP)
                }
                LifecycleState.ON_RESUME -> {
                    startAllWaitingJobs(forState = LifecycleState.ON_RESUME, cancelWhen = LifecycleState.ON_PAUSE)
                }
                LifecycleState.ON_PAUSE -> {
                    cancelAllRunningJobs(forState = LifecycleState.ON_PAUSE)
                }
                LifecycleState.ON_STOP -> {
                    cancelAllRunningJobs(forState = LifecycleState.ON_STOP)
                }
                LifecycleState.ON_DESTROY -> {
                    cancelAllRunningJobs(forState = LifecycleState.ON_DESTROY)
                }
                else -> {
                    // NoOp
                }
            }
        }

        private fun startAllWaitingJobs(forState: LifecycleState, cancelWhen: LifecycleState) {
            getWaitingJobsListFor(forState).apply {
                forEach { job ->
                    startJobAndAddToRunningJobs(job, cancelWhen)
                }
                clear()
            }
        }

        private fun cancelAllRunningJobs(forState: LifecycleState) {
            getRunningJobsListFor(forState).apply {
                forEach { job ->
                    job.cancel(CancellationException("Coreactor has been stopped"))
                }
                clear()
            }
        }

        private fun startJobAndAddToRunningJobs(job: Job, cancelWhen: LifecycleState) {
            job.start()
            getRunningJobsListFor(cancelWhen).add(job)
        }

        private fun waitUntilExpectedState(job: Job, startWhen: LifecycleState) {
            getWaitingJobsListFor(startWhen).add(job)
        }

        private fun getWaitingJobsListFor(lifecycleState: LifecycleState): MutableList<Job> {
            return when (lifecycleState) {
                LifecycleState.ON_CREATE -> jobsWaitingForCreatedState
                LifecycleState.ON_START -> jobsWaitingForStartedState
                LifecycleState.ON_RESUME -> jobsWaitingForResumedState
                else -> throw CoreactorException("Unexpected lifecycle state: $lifecycleState")
            }
        }

        private fun getRunningJobsListFor(lifecycleState: LifecycleState): MutableList<Job> {
            return when (lifecycleState) {
                LifecycleState.ON_PAUSE -> jobsRunningUntilPausedState
                LifecycleState.ON_STOP -> jobsRunningUntilStoppedState
                LifecycleState.ON_DESTROY -> jobsRunningUntilDestroyedState
                else -> throw CoreactorException("Unexpected lifecycle state: $lifecycleState")
            }
        }

        private fun toLazyJob(block: suspend () -> Unit): Job {
            return launch(start = CoroutineStart.LAZY) { block() }
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
