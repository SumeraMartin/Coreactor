package com.sumera.coreactor

import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.event.Event
import com.sumera.coreactor.contract.event.EventBehaviour
import com.sumera.coreactor.contract.reducer.Reducer
import com.sumera.coreactor.error.CoreactorException
import com.sumera.coreactor.lifecycle.LifecycleState
import com.sumera.coreactor.log.CoreactorLogger
import com.sumera.coreactor.testutils.CoreactorTestHelper
import com.sumera.coreactor.testutils.CoroutineRule
import com.sumera.coreactor.testutils.LifecycleRule
import com.sumera.coreactor.testutils.TestState
import com.sumera.coreactor.testutils.TestView
import com.sumera.coreactor.testutils.TestableCoreactor
import com.sumera.coreactor.testutils.catch
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoreactorTest : Spek({

    val message = "message"

    val initialCounterValue = 100

    val defaultDelay = 1000L

    class TestException : RuntimeException()

    class IncrementAction : Action<TestState>

    class DelayedIncrementAction : Action<TestState>

    class ThrowExceptionAction : Action<TestState>

    class ThrowExceptionInCoroutineAction : Action<TestState>

    class InterceptedAction : Action<TestState>

    class CancelableAction : Action<TestState>

    class LaunchWhenCreatedAction : Action<TestState>

    class LaunchWhenStartedAction : Action<TestState>

    class LaunchWhenResumedAction : Action<TestState>

    data class TriggerEventAction(val message: String, val behaviour: EventBehaviour) : Action<TestState>

    data class MessageEvent(val msg: String, override val behaviour: EventBehaviour) : Event<TestState>()

    class TestEvent : Event<TestState>()

    class IncrementReducer : Reducer<TestState>() {
        override fun reduce(oldState: TestState): TestState {
            return oldState.copy(counter = oldState.counter + 1)
        }
    }

    class TestCoreactor(mockLogger: CoreactorLogger<TestState>) : TestableCoreactor<TestState>() {

        val actionList = mutableListOf<Action<TestState>>()

        var createInitialStateCalledCount = 0

        var wasCanceled = false

        override val logger = mockLogger

        override fun createInitialState(): TestState {
            createInitialStateCalledCount += 1
            return TestState(initialCounterValue)
        }

        override fun onAction(action: Action<TestState>) {
            actionList.add(action)

            if (action is IncrementAction) {
                emit(IncrementReducer())
            }
            if (action is DelayedIncrementAction) {
                launch {
                    delay(defaultDelay)
                    emit(IncrementReducer())
                }
            }
            if (action is TriggerEventAction) {
                emit(MessageEvent(action.message, action.behaviour))
            }
            if (action is ThrowExceptionAction) {
                throw TestException()
            }
            if (action is ThrowExceptionInCoroutineAction) {
                runBlocking {
                    throw TestException()
                }
            }
            if (action is CancelableAction) {
                launch {
                    try {
                        delay(Long.MAX_VALUE)
                    } catch (error: CancellationException) {
                        wasCanceled = true
                        throw error
                    }
                }
            }
            if (action is LaunchWhenCreatedAction) {
                launchWhenCreated {
                    try {
                        emit(TestEvent())
                        delay(1000)
                    } catch(e: CancellationException) {
                        wasCanceled = true
                    }
                }
            }
            if (action is LaunchWhenStartedAction) {
                launchWhenStarted {
                    try {
                        emit(TestEvent())
                        delay(1000)
                    } catch(e: CancellationException) {
                        wasCanceled = true
                    }
                }
            }
            if (action is LaunchWhenResumedAction) {
                launchWhenResumed {
                    try {
                        emit(TestEvent())
                        delay(1000)
                    } catch(e: CancellationException) {
                        wasCanceled = true
                    }
                }
            }
        }

        fun testGetState(): TestState {
            return state
        }

        fun testGetLifecycle(): LifecycleState {
            return lifecycleState
        }
    }

    val mockLogger by memoized(CachingMode.EACH_GROUP) { mockk<CoreactorLogger<TestState>>(relaxUnitFun = true) }
    val view : TestView by memoized(CachingMode.EACH_GROUP) { TestView() }
    val coreactor: TestCoreactor by memoized(CachingMode.EACH_GROUP) { TestCoreactor(mockLogger) }
    val coroutineRule = CoroutineRule()
    val lifecycleRule = LifecycleRule()

    lateinit var coreactorHelper: CoreactorTestHelper<TestState>
    beforeEachGroup {
        coroutineRule.setUp()
        lifecycleRule.setUp()
    }

    beforeEachTest {
        coreactorHelper = CoreactorTestHelper(coreactor, view, lifecycleRule.lifecycle)
    }

    afterEachGroup {
        lifecycleRule.tearDown()
        coroutineRule.tearDown()
    }

    Feature("Coreactor state before attach view") {
        Scenario("accessing state") {
            lateinit var errorResult: Throwable
            When("state is accessed") {
                errorResult = catch { coreactor.testGetState() }
            }
            Then("should throw coreactor exception") {
                assertTrue { errorResult is CoreactorException }
            }
        }

        Scenario("accessing lifecycle state") {
            lateinit var result: LifecycleState
            When("lifecycle state is accessed") {
                result = coreactor.testGetLifecycle()
            }
            Then("should be equals to INITIAL state") {
                assertEquals(LifecycleState.INITIAL, result)
            }
        }
    }

    Feature("Coreactor state after attach view") {
        beforeEachTest {
            coreactorHelper.attach()
        }

        Scenario("accessing state") {
            lateinit var state: TestState
            When("state is accessed") {
                state = coreactor.testGetState()
            }
            Then("should be equals to initial state") {
                assertEquals(TestState(initialCounterValue), state)
            }
        }
        Scenario("accessing lifecycle state") {
            lateinit var result: LifecycleState
            When("lifecycle state is accessed") {
                result = coreactor.testGetLifecycle()
            }
            Then("should be equals to ATTACHED state") {
                assertEquals(LifecycleState.ON_ATTACH, result)
            }
        }
    }

    Feature("Coreactor after onCreate") {
        Scenario("accessing lifecycle") {
            lateinit var result: LifecycleState
            When("lifecycle is accessed") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnCreate()
                result = coreactor.testGetLifecycle()
            }
            Then("should be equals to ON_CREATE state") {
                assertEquals(LifecycleState.ON_CREATE, result)
            }
        }
    }

    Feature("Coreactor after onStart") {
        Scenario("accessing lifecycle") {
            lateinit var result: LifecycleState
            When("lifecycle is accessed") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnStart()
                result = coreactor.testGetLifecycle()
            }
            Then("should be equals to ON_START state") {
                assertEquals(LifecycleState.ON_START, result)
            }
        }
    }

    Feature("Coreactor after onResume") {
        Scenario("accessing lifecycle") {
            lateinit var result: LifecycleState
            When("lifecycle is accessed") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                result = coreactor.testGetLifecycle()
            }
            Then("should be equals to ON_RESUME state") {
                assertEquals(LifecycleState.ON_RESUME, result)
            }
        }
    }

    Feature("Coreactor after onPause") {
        Scenario("accessing lifecycle") {
            lateinit var result: LifecycleState
            When("lifecycle is accessed") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnPause()
                result = coreactor.testGetLifecycle()
            }
            Then("should be equals to ON_PAUSE state") {
                assertEquals(LifecycleState.ON_PAUSE, result)
            }
        }
    }

    Feature("Coreactor after onStop") {
        Scenario("accessing lifecycle") {
            lateinit var result: LifecycleState
            When("lifecycle is accessed") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnStop()
                result = coreactor.testGetLifecycle()
            }
            Then("should be equals to ON_STOP state") {
                assertEquals(LifecycleState.ON_STOP, result)
            }
        }
    }

    Feature("Coreactor after onDestroy") {
        Scenario("accessing lifecycle") {
            lateinit var result: LifecycleState
            When("lifecycle is accessed") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnDestroy()
                result = coreactor.testGetLifecycle()
            }
            Then("should be equals to ON_DESTROY state") {
                assertEquals(LifecycleState.ON_DESTROY, result)
            }
        }
    }

    Feature("Coreactor after detach without finishing") {
        Scenario("accessing lifecycle") {
            lateinit var result: LifecycleState
            When("lifecycle is accessed") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnDestroy()
                coreactorHelper.detachWithoutFinishing()
                result = coreactor.testGetLifecycle()
            }
            Then("should be equals to ON_DESTROY state") {
                assertEquals(LifecycleState.ON_DESTROY, result)
            }
        }
    }

    Feature("Coreactor after detach with finishing") {
        Scenario("accessing lifecycle") {
            lateinit var result: LifecycleState
            When("lifecycle is accessed") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnDestroy()
                coreactorHelper.detachWithFinishing()
                result = coreactor.testGetLifecycle()
            }
            Then("should be equals to ON_DETACH state") {
                assertEquals(LifecycleState.ON_DETACH, result)
            }
        }
    }

    Feature("Coreactor after orientation change") {
        Scenario("creating initial state") {
            lateinit var result: LifecycleState
            When("coreactor is created, destroyed and then created again") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnDestroy()
                coreactorHelper.detachWithoutFinishing()
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                result = coreactor.testGetLifecycle()
            }
            Then("create initial state is called only once") {
                assertEquals(1, coreactor.createInitialStateCalledCount)
            }
            Then("lifecycle should be equals to ON_RESUME state") {
                assertEquals(LifecycleState.ON_RESUME, result)
            }
        }
    }

    Feature("Coreactor sendAction") {
        Scenario("action send before attachView") {
            lateinit var result: Throwable
            When("sending action before attachView") {
                result = catch { coreactor.sendAction(IncrementAction()) }
            }
            Then("exception should be thrown") {
                assertTrue { result is CoreactorException }
            }
        }

        Scenario("action send after attachView") {
            val action = IncrementAction()
            When("sending action after attachView") {
                coreactorHelper.attach()
                coreactor.sendAction(action)
            }
            Then("action should be dispatched") {
                assertEquals(1, coreactor.actionList.size)
            }
            Then("action should be unchanged") {
                assertEquals(action, coreactor.actionList[0])
            }
        }

        Scenario("action send after onCreate") {
            val action = IncrementAction()
            When("sending action after onCreate") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnCreate()
                coreactor.sendAction(action)
            }
            Then("action should be dispatched") {
                assertEquals(1, coreactor.actionList.size)
            }
            Then("action should be unchanged") {
                assertEquals(action, coreactor.actionList[0])
            }
        }

        Scenario("action send after onStart") {
            val action = IncrementAction()
            When("sending action after onStart") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnStart()
                coreactor.sendAction(action)
            }
            Then("action should be dispatched") {
                assertEquals(1, coreactor.actionList.size)
            }
            Then("action should be unchanged") {
                assertEquals(action, coreactor.actionList[0])
            }
        }

        Scenario("action send after onResume") {
            val action = IncrementAction()
            When("sending action after onResume") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(action)
            }
            Then("action should be dispatched") {
                assertEquals(1, coreactor.actionList.size)
            }
            Then("action should be unchanged") {
                assertEquals(action, coreactor.actionList[0])
            }
        }

        Scenario("action send after onPause") {
            val action = IncrementAction()
            When("sending action after onPause") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnPause()
                coreactor.sendAction(action)
            }
            Then("action should be dispatched") {
                assertEquals(1, coreactor.actionList.size)
            }
            Then("action should be unchanged") {
                assertEquals(action, coreactor.actionList[0])
            }
        }

        Scenario("action send after onStop") {
            val action = IncrementAction()
            When("sending action after onStop") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnStop()
                coreactor.sendAction(action)
            }
            Then("action should be dispatched") {
                assertEquals(1, coreactor.actionList.size)
            }
            Then("action should be unchanged") {
                assertEquals(action, coreactor.actionList[0])
            }
        }

        Scenario("action send after onDestroy") {
            val action = IncrementAction()
            When("sending action after onDestroy") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnDestroy()
                coreactor.sendAction(action)
            }
            Then("action should be dispatched") {
                assertEquals(1, coreactor.actionList.size)
            }
            Then("action should be unchanged") {
                assertEquals(action, coreactor.actionList[0])
            }
        }

        Scenario("action send after onDetach without finishing") {
            val action = IncrementAction()
            When("sending action after onDetach") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnDestroy()
                coreactorHelper.detachWithoutFinishing()
                coreactor.sendAction(action)
            }
            Then("action should be dispatched") {
                assertEquals(1, coreactor.actionList.size)
            }
            Then("action should be unchanged") {
                assertEquals(action, coreactor.actionList[0])
            }
        }

        Scenario("action send after onDetach with finishing") {
            lateinit var result: Throwable
            When("sending action after onDetach") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnDestroy()
                coreactorHelper.detachWithFinishing()
                result = catch { coreactor.sendAction(IncrementAction()) }
            }
            Then("exception should be thrown") {
                assertTrue { result is CoreactorException }
            }
        }

        Scenario("action send after orientation change") {
            val action = IncrementAction()
            When("sending action after orientation change") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnDestroy()
                coreactorHelper.detachWithoutFinishing()
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(action)
            }
            Then("action should be dispatched") {
                assertEquals(1, coreactor.actionList.size)
            }
            Then("action should be unchanged") {
                assertEquals(action, coreactor.actionList[0])
            }
        }

        Scenario("multiple actions send") {
            val action1 = IncrementAction()
            val action2 = IncrementAction()
            val action3 = IncrementAction()
            When("sending multiple actions") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnDestroy()
                coreactor.sendAction(action1)
                coreactor.sendAction(action2)
                coreactor.sendAction(action3)
            }
            Then("all actions should be dispatched") {
                assertEquals(3, coreactor.actionList.size)
            }
            Then("all actions should be unchanged") {
                assertEquals(action1, coreactor.actionList[0])
                assertEquals(action2, coreactor.actionList[1])
                assertEquals(action3, coreactor.actionList[2])
            }
        }
    }

    Feature("CoreactorView onState") {
        Scenario("view does not receive any state before onStart") {
            When("coreactor is created and no action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnCreate()
            }
            Then("no state is send to view") {
                assertEquals(0, view.stateList.size)
            }
        }

        Scenario("view receives initial state after onStart") {
            When("coreactor is started and no action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnStart()
            }
            Then("only initial state is send to view") {
                assertEquals(1, view.stateList.size)
                assertEquals(TestState(initialCounterValue), view.stateList[0])
            }
        }

        Scenario("reducer changes the state and propagate it to view") {
            When("coreactor is resumed and action which emits reducer is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(IncrementAction())
            }
            Then("new state is propagated to view") {
                assertEquals(2, view.stateList.size)
                assertEquals(TestState(initialCounterValue + 1), view.stateList[1])
            }
        }

        Scenario("reducer changes the state before onStart") {
            When("coreactor is created and action which emits reducer is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnCreate()
                coreactor.sendAction(IncrementAction())
            }
            Then("no state is propagated to view") {
                assertEquals(0, view.stateList.size)
            }
            Then("state is changed") {
                assertEquals(TestState(initialCounterValue + 1), coreactor.testGetState())
            }
        }

        Scenario("reducer changes the state before onStart and then coreactor is started") {
            When("coreactor is created and action which emits reducer is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnCreate()
                coreactor.sendAction(IncrementAction())
                coreactorHelper.fromOnCreateToOnStart()
            }
            Then("changed state is propagated to view") {
                assertEquals(1, view.stateList.size)
                assertEquals(TestState(initialCounterValue + 1), view.stateList[0])
            }
            Then("coreactor state is the same as the last view state") {
                assertEquals(view.stateList[0], coreactor.testGetState())
            }
        }

        Scenario("reducer changes the state after onStart and then after onStop") {
            When("coreactor is resumed and action which emits reducer is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnStart()
                coreactor.sendAction(IncrementAction())
                coreactorHelper.fromOnStartToOnStop()
                coreactor.sendAction(IncrementAction())
            }
            Then("only initial and the first state are send to view") {
                assertEquals(2, view.stateList.size)
                assertEquals(TestState(initialCounterValue + 1), view.stateList[1])
            }
            Then("coreactor state is changed by the action send after onStop") {
                assertEquals(TestState(initialCounterValue + 2), coreactor.testGetState())
            }
        }

        Scenario("reducer changes the state during orientation change") {
            When("coreactor is resumed and during orientation change reducer is triggered") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnDestroy()
                coreactorHelper.detachWithoutFinishing()
                coreactor.sendAction(IncrementAction())
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnStart()
            }
            Then("initial and changed state are propagated to the view") {
                assertEquals(2, view.stateList.size)
                assertEquals(TestState(initialCounterValue + 1), view.stateList[1])
            }
            Then("coreactor state is the same as the view state") {
                assertEquals(view.stateList[1], coreactor.testGetState())
            }
        }

        Scenario("action triggers delayed reducer during resumed state") {
            When("action is triggered during resumed state and time is advanced") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(DelayedIncrementAction())
                coroutineRule.advanceTimeBy(defaultDelay + 1)
            }
            Then("initial and changed state are propagated to the view") {
                assertEquals(2, view.stateList.size)
                assertEquals(TestState(initialCounterValue + 1), view.stateList[1])
            }
            Then("coreactor state is the same as view state") {
                assertEquals(view.stateList[1], coreactor.testGetState())
            }
        }

        Scenario("action triggers delayed reducer after stopped state") {
            When("action is triggered during resumed state and time is advanced after stopped state") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(DelayedIncrementAction())
                coreactorHelper.fromOnResumeToOnStop()
                coroutineRule.advanceTimeBy(defaultDelay + 1)
            }
            Then("only initial state is propagated to the view") {
                assertEquals(1, view.stateList.size)
                assertEquals(TestState(initialCounterValue), view.stateList[0])
            }
            Then("coreactor receives the new changed state") {
                assertEquals(TestState(initialCounterValue + 1), coreactor.testGetState())
            }
        }

        Scenario("multiple reducers change the state and propagate it to view") {
            When("coreactor is resumed and multiple actions which emits reducers are send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(IncrementAction())
                coreactor.sendAction(IncrementAction())
                coreactor.sendAction(IncrementAction())
            }
            Then("new states are propagated to the view") {
                assertEquals(4, view.stateList.size)
                assertEquals(TestState(initialCounterValue + 3), view.stateList[3])
            }
        }

        Scenario("multiple delayed reducers change the state and propagate it to view") {
            When("coreactor is resumed and multiple actions which emits reducers are send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(DelayedIncrementAction())
                coroutineRule.advanceTimeBy(100)
                coreactor.sendAction(DelayedIncrementAction())
                coroutineRule.advanceTimeBy(100)
                coreactor.sendAction(DelayedIncrementAction())
                coroutineRule.advanceTimeBy(defaultDelay)
            }
            Then("new states are propagated to the view") {
                assertEquals(4, view.stateList.size)
                assertEquals(TestState(initialCounterValue + 3), view.stateList[3])
            }
        }
    }

    Feature("CoreactorView onEvent with DISPATCH_EVERY_TIME behavior") {
        val behavior = EventBehaviour.DISPATCH_EVERY_TIME
        Scenario("view does not receive event before onCreate") {
            When("coreactor is created and event is send") {
                coreactorHelper.attach()
                coreactor.sendAction(TriggerEventAction(message, behavior))
            }
            Then("no event is send to view") {
                assertEquals(0, view.eventList.size)
            }
        }

        Scenario("view receives event after onCreate") {
            When("coreactor is in created state and then event is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnCreate()
                coreactor.sendAction(TriggerEventAction(message, behavior))
            }
            Then("event is send to view") {
                assertEquals(1, view.eventList.size)
                assertEquals(MessageEvent(message, behavior), view.eventList[0])
            }
        }

        Scenario("view receives event before onCreate") {
            When("event is send and then coreactor is moved to create state") {
                coreactorHelper.attach()
                coreactor.sendAction(TriggerEventAction(message, behavior))
                coreactorHelper.fromOnAttachToOnCreate()
            }
            Then("event is send to view") {
                assertEquals(1, view.eventList.size)
                assertEquals(MessageEvent(message, behavior), view.eventList[0])
            }
        }

        Scenario("view receives event during orientation change") {
            When("event is send when coreactor is destroyed") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnDestroy()
                coreactorHelper.detachWithoutFinishing()
                coreactor.sendAction(TriggerEventAction(message, behavior))
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnCreate()
            }
            Then("event is send to view") {
                assertEquals(1, view.eventList.size)
                assertEquals(MessageEvent(message, behavior), view.eventList[0])
            }
        }
    }

    Feature("CoreactorView onEvent with DISPATCH_TO_STARTED_OR_WAIT behavior") {
        val behavior = EventBehaviour.DISPATCH_TO_STARTED_OR_WAIT

        Scenario("view does not receive event before onStart") {
            When("coreactor is created and event is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnCreate()
                coreactor.sendAction(TriggerEventAction(message, behavior))
            }
            Then("no event is send to view") {
                assertEquals(0, view.eventList.size)
            }
        }

        Scenario("view receives event after onStart") {
            When("coreactor is in started state and then event is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnStart()
                coreactor.sendAction(TriggerEventAction(message, behavior))
            }
            Then("event is send to view") {
                assertEquals(1, view.eventList.size)
                assertEquals(MessageEvent(message, behavior), view.eventList[0])
            }
        }

        Scenario("view receives event before onStart") {
            When("event is send and then coreactor is moved to started state") {
                coreactorHelper.attach()
                coreactor.sendAction(TriggerEventAction(message, behavior))
                coreactorHelper.fromOnAttachToOnStart()
            }
            Then("event is send to view") {
                assertEquals(1, view.eventList.size)
                assertEquals(MessageEvent(message, behavior), view.eventList[0])
            }
        }

        Scenario("view receives event during orientation change") {
            When("event is send when coreactor is destroyed") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnDestroy()
                coreactorHelper.detachWithoutFinishing()
                coreactor.sendAction(TriggerEventAction(message, behavior))
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnStart()
            }
            Then("event is send to view") {
                assertEquals(1, view.eventList.size)
                assertEquals(MessageEvent(message, behavior), view.eventList[0])
            }
        }
    }

    Feature("CoreactorView onEvent with DISPATCH_TO_STARTED_OR_THROW_AWAY behavior") {
        val behavior = EventBehaviour.DISPATCH_TO_STARTED_OR_THROW_AWAY

        Scenario("view does not receive event before onStart") {
            When("coreactor is created and event is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnCreate()
                coreactor.sendAction(TriggerEventAction(message, behavior))
            }
            Then("no event is send to view") {
                assertEquals(0, view.eventList.size)
            }
        }

        Scenario("view receives event after onStart") {
            When("coreactor is in started state and then event is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnStart()
                coreactor.sendAction(TriggerEventAction(message, behavior))
            }
            Then("event is send to view") {
                assertEquals(1, view.eventList.size)
            }
            Then("event is unchanged") {
                assertEquals(MessageEvent(message, behavior), view.eventList[0])
            }
        }

        Scenario("view receives event before onStart") {
            When("event is send and then coreactor is moved to resumed state") {
                coreactorHelper.attach()
                coreactor.sendAction(TriggerEventAction(message, behavior))
                coreactorHelper.fromOnAttachToOnResume()
            }
            Then("no event is send to view") {
                assertEquals(0, view.eventList.size)
            }
        }

        Scenario("view receives event during orientation change") {
            When("event is send when coreactor is destroyed") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnDestroy()
                coreactorHelper.detachWithoutFinishing()
                coreactor.sendAction(TriggerEventAction(message, behavior))
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
            }
            Then("no event is send to view") {
                assertEquals(0, view.eventList.size)
            }
        }
    }

    Feature("exception thrown during onAction") {
        Scenario("exception in onAction is throw") {
            lateinit var result: Throwable
            When("action causing exception is send") {
                coreactorHelper.attach()
                result = catch { coreactor.sendAction(ThrowExceptionAction()) }
            }
            Then("exception is thrown") {
                assertTrue { result is TestException }
            }
        }
    }

    Feature("exception thrown during onAction in coroutine") {
        Scenario("exception in onAction is throw") {
            lateinit var result: Throwable
            When("action causing exception is send") {
                coreactorHelper.attach()
                result = catch { coreactor.sendAction(ThrowExceptionInCoroutineAction()) }
            }
            Then("exception is handled") {
                assertTrue { result is TestException }
            }
        }
    }

    Feature("coroutines are canceled after detaching") {
        Scenario("delayed action is send and coreactor is detached before finishing") {
            When("delayed action is send and then coreactor is detached") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(CancelableAction())
                coreactorHelper.fromOnResumeToOnDestroy()
                coreactorHelper.detachWithFinishing()
            }
            Then("running flow should be canceled") {
                assertTrue { coreactor.wasCanceled }
            }
        }
    }

    Feature("Coreactor logger") {
        Scenario("onNewStateReceived is called for initial state") {
            When("attaching view") {
                coreactorHelper.attach()
            }
            Then("state is logged") {
                verify { mockLogger.onNewStateReceived(TestState(initialCounterValue)) }
            }
        }

        Scenario("onNewStateReceived is called for new state which is not send to view") {
            When("attaching view and sending action") {
                coreactorHelper.attach()
                coreactor.sendAction(IncrementAction())
            }
            Then("initial and next state are logged") {
                verifyOrder {
                    mockLogger.onNewStateReceived(TestState(initialCounterValue))
                    mockLogger.onNewStateReceived(TestState(initialCounterValue + 1))
                }
            }
        }

        Scenario("onNewStateReceived is called for new state which is send to view") {
            When("attaching view, moving to resumed state and sending action") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(IncrementAction())
            }
            Then("initial and next state are logged") {
                verifyOrder {
                    mockLogger.onNewStateReceived(TestState(initialCounterValue))
                    mockLogger.onNewStateReceived(TestState(initialCounterValue + 1))
                }
            }
        }

        Scenario("onStateDispatchedToView is not called for initial state") {
            When("attaching view") {
                coreactorHelper.attach()
            }
            Then("state is not logged") {
                verify(exactly = 0) { mockLogger.onStateDispatchedToView(TestState(initialCounterValue)) }
            }
        }

        Scenario("onStateDispatchedToView is called after onStart") {
            When("attaching view and moving to started state") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnStart()
            }
            Then("state is logged") {
                verify { mockLogger.onStateDispatchedToView(TestState(initialCounterValue)) }
            }
        }

        Scenario("onStateDispatchedToView is called after onStart with change state") {
            When("attaching view, sending action and moving to started state") {
                coreactorHelper.attach()
                coreactor.sendAction(IncrementAction())
                coreactorHelper.fromOnAttachToOnStart()
            }
            Then("state is logged") {
                verify { mockLogger.onStateDispatchedToView(TestState(initialCounterValue + 1)) }
            }
        }

        Scenario("onAction is called") {
            val action1 = IncrementAction()
            val action2 = DelayedIncrementAction()
            When("sending actions before attaching and after starting") {
                coreactorHelper.attach()
                coreactor.sendAction(action1)
                coreactorHelper.fromOnAttachToOnStart()
                coreactor.sendAction(action2)
            }
            Then("all actions are logged") {
                verifyOrder {
                    mockLogger.onAction(action1)
                    mockLogger.onAction(action2)
                }
            }
        }

        Scenario("onLifecycle is called during orientation change") {
            When("orientation is changing") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnDestroy()
                coreactorHelper.detachWithoutFinishing()
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
            }
            Then("all lifecycle events are logged") {
                verifyOrder {
                    mockLogger.onLifecycle(LifecycleState.ON_ATTACH)
                    mockLogger.onLifecycle(LifecycleState.ON_CREATE)
                    mockLogger.onLifecycle(LifecycleState.ON_START)
                    mockLogger.onLifecycle(LifecycleState.ON_RESUME)
                    mockLogger.onLifecycle(LifecycleState.ON_PAUSE)
                    mockLogger.onLifecycle(LifecycleState.ON_STOP)
                    mockLogger.onLifecycle(LifecycleState.ON_DESTROY)
                    mockLogger.onLifecycle(LifecycleState.ON_CREATE)
                    mockLogger.onLifecycle(LifecycleState.ON_START)
                    mockLogger.onLifecycle(LifecycleState.ON_RESUME)
                }
            }
        }

        Scenario("onLifecycle is called during detaching") {
            When("coreactor is resumed and then detached") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnDestroy()
                coreactorHelper.detachWithFinishing()
            }
            Then("all lifecycle events are logged") {
                verifyOrder {
                    mockLogger.onLifecycle(LifecycleState.ON_ATTACH)
                    mockLogger.onLifecycle(LifecycleState.ON_CREATE)
                    mockLogger.onLifecycle(LifecycleState.ON_START)
                    mockLogger.onLifecycle(LifecycleState.ON_RESUME)
                    mockLogger.onLifecycle(LifecycleState.ON_PAUSE)
                    mockLogger.onLifecycle(LifecycleState.ON_STOP)
                    mockLogger.onLifecycle(LifecycleState.ON_DESTROY)
                    mockLogger.onLifecycle(LifecycleState.ON_DETACH)
                }
            }
        }

        Scenario("onReducer is called") {
            When("attaching view and sending action") {
                coreactorHelper.attach()
                coreactor.sendAction(IncrementAction())
            }
            Then("reducer is logged") {
                val initialState = TestState(initialCounterValue)
                val newState = TestState(initialCounterValue + 1)
                verify { mockLogger.onReducer(initialState, any<IncrementReducer>(), newState) }
            }
        }

        Scenario("onEventEmitted is called") {
            When("attaching view and sending action") {
                coreactorHelper.attach()
                coreactor.sendAction(TriggerEventAction(message, EventBehaviour.DISPATCH_EVERY_TIME))
            }
            Then("onEventEmitted is logged") {
                verify { mockLogger.onEventEmitted(MessageEvent(message, EventBehaviour.DISPATCH_EVERY_TIME)) }
            }
        }

        Scenario("onEventDispatchedToView is called when event is dispatched") {
            When("attaching view and sending action") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnCreate()
                coreactor.sendAction(TriggerEventAction(message, EventBehaviour.DISPATCH_EVERY_TIME))
            }
            Then("onEventDispatchedToView is logged") {
                verify { mockLogger.onEventDispatchedToView(MessageEvent(message, EventBehaviour.DISPATCH_EVERY_TIME)) }
            }
        }

        Scenario("onEventDispatchedToView is not called when event is not dispatched") {
            When("attaching view and sending action") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnCreate()
                coreactor.sendAction(TriggerEventAction(message, EventBehaviour.DISPATCH_TO_STARTED_OR_THROW_AWAY))
            }
            Then("onEventDispatchedToView is logged") {
                verify(exactly = 0) { mockLogger.onEventDispatchedToView(MessageEvent(message, EventBehaviour.DISPATCH_TO_STARTED_OR_THROW_AWAY)) }
            }
        }

        Scenario("onEventThrownAway is called when event is triggered and view is not started") {
            When("attaching view, moving to created state and sending action") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnCreate()
                coreactor.sendAction(TriggerEventAction(message, EventBehaviour.DISPATCH_TO_STARTED_OR_THROW_AWAY))
            }
            Then("onEventThrownAway is logged") {
                verify { mockLogger.onEventThrownAway(MessageEvent(message, EventBehaviour.DISPATCH_TO_STARTED_OR_THROW_AWAY)) }
                verify(exactly = 0) { mockLogger.onEventDispatchedToView(MessageEvent(message, EventBehaviour.DISPATCH_TO_STARTED_OR_THROW_AWAY)) }
            }
        }

        Scenario("onEventWaitingForStartedView is called when event is triggered and view is not started") {
            When("attaching view, sending action and resuming coreactor") {
                coreactorHelper.attach()
                coreactor.sendAction(TriggerEventAction(message, EventBehaviour.DISPATCH_TO_STARTED_OR_WAIT))
                coreactorHelper.fromOnAttachToOnResume()

            }
            Then("onEventWaitingForCreatedView is logged") {
                verifyOrder {
                    mockLogger.onEventWaitingForStartedView(MessageEvent(message, EventBehaviour.DISPATCH_TO_STARTED_OR_WAIT))
                    mockLogger.onEventDispatchedToView(MessageEvent(message, EventBehaviour.DISPATCH_TO_STARTED_OR_WAIT))
                }
            }
        }

        Scenario("onEventWaitingForCreatedView is called when event is triggered and view is not created") {
            When("attaching view, sending action and resuming coreactor") {
                coreactorHelper.attach()
                coreactor.sendAction(TriggerEventAction(message, EventBehaviour.DISPATCH_EVERY_TIME))
                coreactorHelper.fromOnAttachToOnResume()

            }
            Then("onEventWaitingForCreatedView is logged") {
                verifyOrder {
                    mockLogger.onEventWaitingForCreatedView(MessageEvent(message, EventBehaviour.DISPATCH_EVERY_TIME))
                    mockLogger.onEventDispatchedToView(MessageEvent(message, EventBehaviour.DISPATCH_EVERY_TIME))
                }
            }
        }
    }

    Feature("launchWhenResumed tests") {
        Scenario("launchWhenResumed shouldn't be started before coreactor is in resumed state") {
            When("action is send before resumed state") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnStart()
                coreactor.sendAction(LaunchWhenResumedAction())
            }
            Then("launch block shouldn't be started") {
                assertEquals(0, view.eventList.size)
            }
        }
        Scenario("launchWhenResumed should wait and be started when coreactor is in resumed state") {
            When("action is send before resumed state") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnStart()
                coreactor.sendAction(LaunchWhenResumedAction())
                coreactorHelper.fromOnStartToOnResume()
            }
            Then("launch block should be started") {
                assertEquals(1, view.eventList.size)
            }
        }
        Scenario("launchWhenResumed should be started immediately if coreactor is already in resumed state") {
            When("action is send in started state") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(LaunchWhenResumedAction())
            }
            Then("launch block should be started") {
                assertEquals(1, view.eventList.size)
            }
        }
        Scenario("launchWhenResumed should be canceled when coreactor will be paused") {
            When("action is send in resumed state and then state is moved to pause state") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(LaunchWhenResumedAction())
                coreactorHelper.fromOnResumeToOnPause()
            }
            Then("launch block should be started") {
                assertEquals(1, view.eventList.size)
            }
            Then("launch block should be canceled") {
                assertTrue { coreactor.wasCanceled }
            }
        }
    }

    Feature("launchWhenStarted tests") {
        Scenario("launchWhenStarted shouldn't be started before coreactor is in started state") {
            When("action is send before started state") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnCreate()
                coreactor.sendAction(LaunchWhenStartedAction())
            }
            Then("launch block shouldn't be started") {
                assertEquals(0, view.eventList.size)
            }
        }
        Scenario("launchWhenStarted should wait and be started when coreactor is in started state") {
            When("action is send before started state") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnCreate()
                coreactor.sendAction(LaunchWhenStartedAction())
                coreactorHelper.fromOnCreateToOnStart()
            }
            Then("launch block should be started") {
                assertEquals(1, view.eventList.size)
            }
        }
        Scenario("launchWhenStarted should be started immediately if coreactor is already in started state") {
            When("action is send in started state") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnStart()
                coreactor.sendAction(LaunchWhenStartedAction())
            }
            Then("launch block should be started") {
                assertEquals(1, view.eventList.size)
            }
        }
        Scenario("launchWhenStarted should be canceled when coreactor will be stopped") {
            When("action is send in resumed state and then state is moved to stopped state") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(LaunchWhenStartedAction())
                coreactorHelper.fromOnResumeToOnStop()
            }
            Then("launch block should be started") {
                assertEquals(1, view.eventList.size)
            }
            Then("launch block should be canceled") {
                assertTrue { coreactor.wasCanceled }
            }
        }
    }

    Feature("launchWhenCreated tests") {
        Scenario("launchWhenCreated shouldn't be started before coreactor is in created state") {
            When("action is send before created state") {
                coreactorHelper.attach()
                coreactor.sendAction(LaunchWhenCreatedAction())
            }
            Then("launch block shouldn't be started") {
                assertEquals(0, view.eventList.size)
            }
        }
        Scenario("launchWhenCreated should wait and be started when coreactor is in created state") {
            When("action is send before created state") {
                coreactorHelper.attach()
                coreactor.sendAction(LaunchWhenCreatedAction())
                coreactorHelper.fromOnAttachToOnCreate()
            }
            Then("launch block should be started") {
                assertEquals(1, view.eventList.size)
            }
        }
        Scenario("launchWhenCreated should be started immediately if coreactor is already in created state") {
            When("action is send in created state") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnCreate()
                coreactor.sendAction(LaunchWhenCreatedAction())
            }
            Then("launch block should be started") {
                assertEquals(1, view.eventList.size)
            }
        }
        Scenario("launchWhenCreated should be canceled when coreactor will be destroyed") {
            When("action is send in created state and then state is moved to destroyed state") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(LaunchWhenCreatedAction())
                coreactorHelper.fromOnResumeToOnDestroy()
            }
            Then("launch block should be started") {
                assertEquals(1, view.eventList.size)
            }
            Then("launch block should be canceled") {
                assertTrue { coreactor.wasCanceled }
            }
        }
    }
})
