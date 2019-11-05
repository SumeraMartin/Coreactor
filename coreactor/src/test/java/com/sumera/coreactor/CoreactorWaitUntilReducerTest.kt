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

class CoreactorWaitUntilReducerTest : Spek({

    class TriggerExpectedReducerAction : Action<TestState>

    class WaitForReducerAction : Action<TestState>

    class TestEvent : Event<TestState>()

    class ExpectedReducer : Reducer<TestState>() {
        override fun reduce(oldState: TestState): TestState {
            return oldState
        }
    }

    class TestCoreactor : TestableCoreactor<TestState>() {

        override fun createInitialState(): TestState {
            return TestState(0)
        }

        override fun onAction(action: Action<TestState>) {
            if (action is TriggerExpectedReducerAction) {
                launch {
                    emit(ExpectedReducer())
                }
            }

            if (action is WaitForReducerAction) {
                launch {
                    waitUntilReducerType<ExpectedReducer>()
                    emit(TestEvent())
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

    Feature("waitUntilReducer") {
        Scenario("waitUntilReducer should wait until expected reducer is emitted") {
            When("the initial action is send and the awaiting reducer is not send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(WaitForReducerAction())
            }
            Then("expected event is not emitted") {
                assertEquals(0, view.eventList.size)
            }
        }

        Scenario("waitUntilReducer should wait until expected reducer is emitted") {
            When("the initial action is send and then the awaiting reducer is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(WaitForReducerAction())
                coreactor.sendAction(TriggerExpectedReducerAction())
            }
            Then("expected event is emitted") {
                assertEquals(1, view.eventList.size)
            }
        }

        Scenario("waitUntilReducer should wait until expected reduced is emitted multiple times") {
            When("the initial action is send three times and then the awaiting reducer is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(WaitForReducerAction())
                coreactor.sendAction(WaitForReducerAction())
                coreactor.sendAction(WaitForReducerAction())
                coreactor.sendAction(TriggerExpectedReducerAction())
            }
            Then("expected events are emitted") {
                assertEquals(3, view.eventList.size)
            }
        }


        Scenario("waitUntilReducer should not react to previously emitted reducer") {
            When("the awaiting reducer is send and then the initial action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(TriggerExpectedReducerAction())
                coreactor.sendAction(WaitForReducerAction())
            }
            Then("expected event is not emitted") {
                assertEquals(0, view.eventList.size)
            }
        }
    }
})
