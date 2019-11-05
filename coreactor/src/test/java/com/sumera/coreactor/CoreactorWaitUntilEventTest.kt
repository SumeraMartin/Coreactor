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

class CoreactorWaitUntilEventTest : Spek({

    class TriggerExpectedEventAction : Action<TestState>

    class WaitForExpectedEventAction : Action<TestState>

    class TestEvent : Event<TestState>()

    class ExpectedEvent : Event<TestState>()

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
            if (action is TriggerExpectedEventAction) {
                launch {
                    emit(ExpectedEvent())
                }
            }

            if (action is WaitForExpectedEventAction) {
                launch {
                    waitUntilEventType<ExpectedEvent>()
                    successWaitingCounter += 1
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

    Feature("waitUntilEvent") {
        Scenario("waitUntilEvent should wait until expected event is emitted") {
            When("the initial action is send and the awaiting event is not send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(WaitForExpectedEventAction())
            }
            Then("coroutine is still suspended and waiting") {
                assertEquals(0, coreactor.successWaitingCounter)
            }
        }

        Scenario("waitUntilEvent should wait until expected event is emitted") {
            When("the initial action is send and then the awaiting event is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(WaitForExpectedEventAction())
                coreactor.sendAction(TriggerExpectedEventAction())
            }
            Then("coroutine is not waiting") {
                assertEquals(1, coreactor.successWaitingCounter)
            }
        }

        Scenario("waitUntilEvent should wait until expected event is emitted multiple times") {
            When("the initial action is send three times and then the awaiting event is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(WaitForExpectedEventAction())
                coreactor.sendAction(WaitForExpectedEventAction())
                coreactor.sendAction(WaitForExpectedEventAction())
                coreactor.sendAction(TriggerExpectedEventAction())
            }
            Then("coroutine is not waiting") {
                assertEquals(3, coreactor.successWaitingCounter)
            }
        }


        Scenario("waitUntilEvent should not react to previously emitted event") {
            When("the awaiting event is send and then the initial action is action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(TriggerExpectedEventAction())
                coreactor.sendAction(WaitForExpectedEventAction())
            }
            Then("coroutine is suspended and waiting") {
                assertEquals(0, coreactor.successWaitingCounter)
            }
        }
    }
})
