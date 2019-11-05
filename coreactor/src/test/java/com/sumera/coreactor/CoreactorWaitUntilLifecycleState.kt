package com.sumera.coreactor

import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.event.Event
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

class CoreactorWaitUntilLifecycleStateTest : Spek({

    class TriggerWaitUntilLifecycleIsResumedAction : Action<TestState>

    class TestEvent : Event<TestState>()

    class TestCoreactor : TestableCoreactor<TestState>() {

        var successWaitingCounter = 0

        override fun createInitialState(): TestState {
            return TestState(0)
        }

        override fun onAction(action: Action<TestState>) {
            if (action is TriggerWaitUntilLifecycleIsResumedAction) {
                launch {
                    waitUntilLifecycle(LifecycleState.ON_RESUME)
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

    Feature("waitUntilLifecycle") {
        Scenario("waitUntilLifecycle should wait until expected lifecycle state is received") {
            When("the trigger action is send and the current lifecycle state is not changed") {
                coreactorHelper.attach()
                coreactor.sendAction(TriggerWaitUntilLifecycleIsResumedAction())
            }
            Then("expected event is not emitted") {
                assertEquals(0, view.eventList.size)
            }
        }

        Scenario("waitUntilLifecycle should wait until expected lifecycle state is received") {
            When("the trigger action is send and then the lifecycle state is changed") {
                coreactorHelper.attach()
                coreactor.sendAction(TriggerWaitUntilLifecycleIsResumedAction())
                coreactorHelper.fromOnAttachToOnResume()
            }
            Then("expected event is emitted") {
                assertEquals(1, view.eventList.size)
            }
        }

        Scenario("waitUntilLifecycle should wait until expected lifecycle state is received") {
            When("the trigger action is send two times and then the lifecycle is changed") {
                coreactorHelper.attach()
                coreactor.sendAction(TriggerWaitUntilLifecycleIsResumedAction())
                coreactor.sendAction(TriggerWaitUntilLifecycleIsResumedAction())
                coreactorHelper.fromOnAttachToOnResume()
            }
            Then("expected events are emitted") {
                assertEquals(2, view.eventList.size)
            }
        }

        Scenario("waitUntilLifecycle should not wait if expected lifecycle state is already set") {
            When("lifecycle state is set to the expected state and then the trigger action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(TriggerWaitUntilLifecycleIsResumedAction())
            }
            Then("expected event is emitted") {
                assertEquals(1, view.eventList.size)
            }
        }
    }
})
