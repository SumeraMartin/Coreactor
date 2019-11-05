package com.sumera.coreactor

import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.event.Event
import com.sumera.coreactor.contract.reducer.Reducer
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

class CoreactorWaitUntilStateTest : Spek({

    class TriggerWaitUntilStateCounterIsMoreThanTwoAction : Action<TestState>

    class IncrementAction : Action<TestState>

    class TestEvent : Event<TestState>()

    class IncrementReducer : Reducer<TestState>() {
        override fun reduce(oldState: TestState): TestState {
            return oldState.copy(counter = oldState.counter + 1)
        }
    }

    class TestCoreactor : TestableCoreactor<TestState>() {

        var successWaitingCounter = 0

        override fun createInitialState(): TestState {
            return TestState(0)
        }

        override fun onAction(action: Action<TestState>) {
            if (action is TriggerWaitUntilStateCounterIsMoreThanTwoAction) {
                launch {
                    waitUntilState { it.counter >= 2 }
                    emit(TestEvent())
                }
            }

            if (action is IncrementAction) {
                launch {
                    emit(IncrementReducer())
                }
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

    Feature("waitUntilState") {
        Scenario("waitUntilState should wait until expected state is received") {
            When("the trigger action is send and then state is changed but doesn't fulfill condition") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(TriggerWaitUntilStateCounterIsMoreThanTwoAction())
                coreactor.sendAction(IncrementAction())
            }
            Then("expected event is not emitted") {
                assertEquals(0, view.eventList.size)
            }
        }

        Scenario("waitUntilState should wait until expected state is received") {
            When("the trigger action is send and then the state is changed and fulfills waitUntil condition") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(TriggerWaitUntilStateCounterIsMoreThanTwoAction())
                coreactor.sendAction(IncrementAction())
                coreactor.sendAction(IncrementAction())
            }
            Then("expected event is emitted") {
                assertEquals(1, view.eventList.size)
            }
        }

        Scenario("waitUntilState should wait until expected state is received") {
            When("the trigger action is send two times and then the state is changed and fulfills waitUntil condition") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(TriggerWaitUntilStateCounterIsMoreThanTwoAction())
                coreactor.sendAction(TriggerWaitUntilStateCounterIsMoreThanTwoAction())
                coreactor.sendAction(IncrementAction())
                coreactor.sendAction(IncrementAction())
            }
            Then("expected events are emitted") {
                assertEquals(2, view.eventList.size)
            }
        }

        Scenario("waitUntilState should not wait if the current state fulfills condition") {
            When("state is changed to fulfill waitUntil condition and then the trigger action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(IncrementAction())
                coreactor.sendAction(IncrementAction())
                coreactor.sendAction(TriggerWaitUntilStateCounterIsMoreThanTwoAction())
            }
            Then("expected event is emitted") {
                assertEquals(1, view.eventList.size)
            }
        }
    }
})
