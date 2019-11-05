package com.sumera.coreactor

import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.event.Event
import com.sumera.coreactor.contract.reducer.Reducer
import com.sumera.coreactor.lifecycle.LifecycleState
import com.sumera.coreactor.testutils.CoreactorTestHelper
import com.sumera.coreactor.testutils.CoroutineRule
import com.sumera.coreactor.testutils.LifecycleRule
import com.sumera.coreactor.testutils.MainThreadCheckRule
import com.sumera.coreactor.testutils.TestState
import com.sumera.coreactor.testutils.TestView
import com.sumera.coreactor.testutils.TestableCoreactor
import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoreactorChannelsTest : Spek({

    val initialCounterValue = 100

    class TestAction : Action<TestState>

    class TestEvent : Event<TestState>()

    class TestIncrementReducer : Reducer<TestState>() {
        override fun reduce(oldState: TestState): TestState {
            return oldState.copy(counter = oldState.counter + 1)
        }
    }

    class TestCoreactor : TestableCoreactor<TestState>() {

        val actionList = mutableListOf<Action<TestState>>()

        val reducerList = mutableListOf<Reducer<TestState>>()

        val eventList = mutableListOf<Event<TestState>>()

        val stateList = mutableListOf<TestState>()

        val lifecycleList = mutableListOf<LifecycleState>()

        override fun createInitialState(): TestState {
            return TestState(initialCounterValue)
        }

        override fun onLifecycleState(state: LifecycleState) {
            when (state) {
                LifecycleState.ON_RESUME -> {
                    launch {
                        val subscription = openActionSubscription()
                        while (true) {
                            val action = subscription.receive()
                            actionList.add(action)
                        }
                    }
                    launch {
                        val subscription = openReducerSubscription()
                        while (true) {
                            val reducer = subscription.receive()
                            reducerList.add(reducer)
                        }
                    }
                    launch {
                        val subscription = openEventSubscription()
                        while (true) {
                            val event = subscription.receive()
                            eventList.add(event)
                        }
                    }
                    launch {
                        val subscription = openStateSubscription()
                        while (true) {
                            val state = subscription.receive()
                            stateList.add(state)
                        }
                    }
                    launch {
                        val subscription = openLifecycleSubscription()
                        while (true) {
                            val lifecycleState = subscription.receive()
                            lifecycleList.add(lifecycleState)
                        }
                    }
                }
            }
        }

        override fun onAction(action: Action<TestState>) {
            if (action is TestAction) {
                emit(TestEvent())
                emit(TestIncrementReducer())
            }
        }
    }

    val view : TestView by memoized(CachingMode.EACH_GROUP) { TestView() }
    val coreactor: TestCoreactor by memoized(CachingMode.EACH_GROUP) { TestCoreactor() }
    val coroutineRule = CoroutineRule()
    val lifecycleRule = LifecycleRule()
    val mainThreadCheckRule = MainThreadCheckRule()

    lateinit var coreactorHelper: CoreactorTestHelper<TestState>
    beforeEachGroup {
        coroutineRule.setUp()
        lifecycleRule.setUp()
        mainThreadCheckRule.setUp()
    }

    beforeEachTest {
        coreactorHelper = CoreactorTestHelper(coreactor, view, lifecycleRule.lifecycle)
    }

    afterEachGroup {
        mainThreadCheckRule.tearDown()
        lifecycleRule.tearDown()
        coroutineRule.tearDown()
    }

    Feature("openStateSubscription") {
        Scenario("contains the state right after consuming") {
            When("coreactor is attached") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
            }
            Then("initial state is send to channel") {
                assertEquals(1, coreactor.stateList.size)
                assertEquals(TestState(initialCounterValue), coreactor.stateList[0])
            }
        }

        Scenario("contains the same states as view") {
            When("coreactor is resumed and action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(TestAction())
            }
            Then("two states are send to channel") {
                assertEquals(2, coreactor.stateList.size)
                assertEquals(2, view.stateList.size)
                assertEquals(view.stateList[0], coreactor.stateList[0])
                assertEquals(view.stateList[1], coreactor.stateList[1])
                assertEquals(TestState(initialCounterValue), coreactor.stateList[0])
                assertEquals(TestState(initialCounterValue + 1), coreactor.stateList[1])
            }
        }
    }

    Feature("openActionSubscription") {
        Scenario("does not contain action right after consuming") {
            When("action is send and then coreactor is attached and resumed") {
                coreactorHelper.attach()
                coreactor.sendAction(TestAction())
                coreactorHelper.fromOnAttachToOnResume()
            }
            Then("no action is send to channel") {
                assertEquals(0, coreactor.actionList.size)
            }
        }

        Scenario("contains all send actions") {
            val action1 = TestAction()
            val action2 = TestAction()
            val action3 = TestAction()
            When("coreactor is resumed and actions are send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(action1)
                coreactor.sendAction(action2)
                coreactor.sendAction(action3)
            }
            Then("all actions are send to channel") {
                assertEquals(3, coreactor.actionList.size)
                assertTrue { action1 === coreactor.actionList[0] }
                assertTrue { action2 === coreactor.actionList[1] }
                assertTrue { action3 === coreactor.actionList[2] }
            }
        }
    }

    Feature("openEventSubscription") {
        Scenario("does not contain event right after consuming") {
            When("action is send and then coreactor is resumed") {
                coreactorHelper.attach()
                coreactor.sendAction(TestAction())
                coreactorHelper.fromOnAttachToOnResume()
            }
            Then("no event is send to channel") {
                assertEquals(0, coreactor.eventList.size)
            }
        }

        Scenario("contains all send events") {
            When("coreactor is resumed and actions are send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(TestAction())
                coreactor.sendAction(TestAction())
            }
            Then("all events are send to channel") {
                assertEquals(2, coreactor.eventList.size)
                assertTrue { coreactor.eventList[0] is TestEvent }
                assertTrue { coreactor.eventList[1] is TestEvent }
            }
        }
    }

    Feature("`openReducerSubscription`") {
        Scenario("does not contain reducer right after consuming") {
            When("action is send and then coreactor is resumed") {
                coreactorHelper.attach()
                coreactor.sendAction(TestAction())
                coreactorHelper.fromOnAttachToOnResume()
            }
            Then("no reducer is send to channel") {
                assertEquals(0, coreactor.reducerList.size)
            }
        }

        Scenario("contains all send reducers") {
            When("coreactor is resumed and actions are send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(TestAction())
                coreactor.sendAction(TestAction())
            }
            Then("all reducers are send to channel") {
                assertEquals(2, coreactor.reducerList.size)
                assertTrue { coreactor.reducerList[0] is TestIncrementReducer }
                assertTrue { coreactor.reducerList[1] is TestIncrementReducer }
            }
        }
    }

    Feature("openLifecycleSubscription") {
        Scenario("contains the lifecycle state right after consuming") {
            When("coreactor is attached") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
            }
            Then("initial lifecycle state is send to channel") {
                assertEquals(1, coreactor.lifecycleList.size)
                assertEquals(LifecycleState.ON_RESUME, coreactor.lifecycleList[0])
            }
        }

        Scenario("contains all lifecycle states") {
            When("coreactor is resumed and then destroyed") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactorHelper.fromOnResumeToOnDestroy()
            }
            Then("all lifecycle states are send to channel") {
                assertEquals(4, coreactor.lifecycleList.size)
                assertEquals(LifecycleState.ON_RESUME, coreactor.lifecycleList[0])
                assertEquals(LifecycleState.ON_PAUSE, coreactor.lifecycleList[1])
                assertEquals(LifecycleState.ON_STOP, coreactor.lifecycleList[2])
                assertEquals(LifecycleState.ON_DESTROY, coreactor.lifecycleList[3])
            }
        }
    }
})
