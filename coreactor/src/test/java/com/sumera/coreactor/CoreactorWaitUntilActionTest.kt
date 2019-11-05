package com.sumera.coreactor

import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.event.Event
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

class CoreactorWaitUntilActionTest : Spek({

    class WaitUntilActionFirstAction : Action<TestState>

    class WaitUntilActionSecondAction : Action<TestState>

    class WaitUntilActionTypeFirstAction : Action<TestState>

    class WaitUntilActionTypeSecondAction : Action<TestState>

    class WaitUntilActionMultipleFirstAction : Action<TestState>

    class WaitUntilActionMultipleSecondAction : Action<TestState>

    class WaitUntilSeveralSameConsequentActionsAction : Action<TestState>

    class TestEvent : Event<TestState>()

    class TestCoreactor : TestableCoreactor<TestState>() {

        override fun createInitialState(): TestState {
            return TestState(0)
        }

        override fun onAction(action: Action<TestState>) {
            if (action is WaitUntilActionFirstAction) {
                launch {
                    waitUntilAction { it is WaitUntilActionSecondAction }
                    emit(TestEvent())
                }
            }
            if (action is WaitUntilActionTypeFirstAction) {
                launch {
                    waitUntilActionType<WaitUntilActionTypeSecondAction>()
                    emit(TestEvent())
                }
            }
            if (action is WaitUntilActionMultipleFirstAction) {
                launch {
                    waitUntilActionType<WaitUntilActionMultipleSecondAction>()
                    emit(TestEvent())
                }
                launch {
                    waitUntilActionType<WaitUntilActionMultipleSecondAction>()
                    emit(TestEvent())
                }
            }
            if (action is WaitUntilSeveralSameConsequentActionsAction) {
                launch {
                    repeat(100) {
                        waitUntilAction { it is WaitUntilActionSecondAction }
                    }
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

    Feature("waitUntilAction") {
        Scenario("waitUntilAction should wait until expected action is received") {
            When("the initial action is send and then the expected action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(WaitUntilActionFirstAction())
                coreactor.sendAction(WaitUntilActionSecondAction())
            }
            Then("expected event is emitted") {
                assertEquals(1, view.eventList.size)
            }
        }

        Scenario("waitUntilAction should not react to previously emitted action") {
            When("the expected action is send then the initial action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(WaitUntilActionSecondAction())
                coreactor.sendAction(WaitUntilActionFirstAction())
            }
            Then("expected event is not emitted") {
                assertEquals(0, view.eventList.size)
            }
        }

        Scenario("waitUntilAction launched multiple times should wait until expected action is received and react multiple times") {
            When("the initial action is send two times and then the expected action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(WaitUntilActionFirstAction())
                coreactor.sendAction(WaitUntilActionFirstAction())
                coreactor.sendAction(WaitUntilActionSecondAction())
            }
            Then("expected events are emitted") {
                assertEquals(2, view.eventList.size)
            }
        }

        Scenario("waitUntilActionType should wait until expected action is received") {
            When("the initial action is send and then the expected action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(WaitUntilActionTypeFirstAction())
                coreactor.sendAction(WaitUntilActionTypeSecondAction())
            }
            Then("expected event is emitted") {
                assertEquals(1, view.eventList.size)
            }
        }

        Scenario("waitUntilAction launched multiple times should wait until expected action is received and react multiple times") {
            When("the initial action is send two times and then the expected action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(WaitUntilActionTypeFirstAction())
                coreactor.sendAction(WaitUntilActionTypeFirstAction())
                coreactor.sendAction(WaitUntilActionTypeSecondAction())
            }
            Then("expected events are emitted") {
                assertEquals(2, view.eventList.size)
            }
        }

        Scenario("action that launches multiple waitUntilAction methods should wait until expected action is received and react multiple times") {
            When("the initial action is send and then the expected action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(WaitUntilActionMultipleFirstAction())
                coreactor.sendAction(WaitUntilActionMultipleSecondAction())
            }
            Then("expected events are emitted") {
                assertEquals(2, view.eventList.size)
            }
        }

        Scenario("waitUntilActionType used multiple times consequently with the same action should wait for multiple actions") {
            When("the initial action is send and then the expected action is send 99 times") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(WaitUntilSeveralSameConsequentActionsAction())
                repeat(99) {
                    coreactor.sendAction(WaitUntilActionSecondAction())
                }
            }
            Then("expected event is not emitted") {
                assertEquals(0, view.eventList.size)
            }
        }

        Scenario("waitUntilActionType used multiple times consequently with the same action should wait for multiple actions") {
            When("the initial action is send and then the expected action is send 100 times") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(WaitUntilSeveralSameConsequentActionsAction())
                repeat(100) {
                    coreactor.sendAction(WaitUntilActionSecondAction())
                }
            }
            Then("expected event is emitted") {
                assertEquals(1, view.eventList.size)
            }
        }
    }
})
