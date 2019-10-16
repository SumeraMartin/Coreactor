package com.sumera.coreactor

import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.event.Event
import com.sumera.coreactor.contract.reducer.Reducer
import com.sumera.coreactor.interceptor.CoreactorInterceptor
import com.sumera.coreactor.testutils.CoreactorTestHelper
import com.sumera.coreactor.testutils.CoroutineRule
import com.sumera.coreactor.testutils.LifecycleRule
import com.sumera.coreactor.testutils.TestState
import com.sumera.coreactor.testutils.TestView
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoreactorInterceptorTest : Spek({

    val initialCounterValue = 100

    val interceptedCounterValue = 200

    class TestAction : Action<TestState>

    class InterceptedTestAction : Action<TestState>

    class TestEvent : Event<TestState>()

    class InterceptedTestEvent : Event<TestState>()

    class TestIncrementReducer : Reducer<TestState>() {
        override fun reduce(oldState: TestState): TestState {
            return oldState.copy(counter = oldState.counter + 1)
        }
    }

    class InterceptedTestDecrementReducer : Reducer<TestState>() {
        override fun reduce(oldState: TestState): TestState {
            return oldState.copy(counter = oldState.counter - 1)
        }
    }

    class TestCoreactor(override val interceptor: CoreactorInterceptor<TestState>) : Coreactor<TestState>() {

        val actionList = mutableListOf<Action<TestState>>()

        override fun createInitialState(): TestState {
            return TestState(initialCounterValue)
        }

        override fun onAction(action: Action<TestState>) = coreactorFlow {
            actionList.add(action)

            if (action is InterceptedTestAction) {
                emit(TestEvent())
                emit(TestIncrementReducer())
            }
        }
    }

    val mockInterceptor by memoized(CachingMode.EACH_GROUP) { mockk<CoreactorInterceptor<TestState>>(relaxUnitFun = true) }
    val view : TestView by memoized(CachingMode.EACH_GROUP) { TestView() }
    val coreactor: TestCoreactor by memoized(CachingMode.EACH_GROUP) { TestCoreactor(mockInterceptor) }
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

    Feature("interceptor with real values") {
        Scenario("intercepting state") {
            beforeEachTest {
                every { mockInterceptor.onInterceptState(any()) } returns TestState(interceptedCounterValue)
            }

            When("view is started") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnStart()
            }
            Then("initial state should be intercepted and replaced with the new one") {
                assertEquals(TestState(interceptedCounterValue), view.stateList[0])
            }
        }

        Scenario("intercepting action") {
            beforeEachTest {
                every { mockInterceptor.onInterceptState(any()) } returns TestState(interceptedCounterValue)
                every { mockInterceptor.onInterceptEvent(any()) } returns InterceptedTestEvent()
                every { mockInterceptor.onInterceptAction(any()) } returns InterceptedTestAction()
                every { mockInterceptor.onInterceptReducer(any()) } returns InterceptedTestDecrementReducer()
            }

            When("view is created and action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnCreate()
                coreactor.sendAction(TestAction())
            }
            Then("action should be intercepted and replaced with the new one") {
                assertTrue { coreactor.actionList[0] is InterceptedTestAction }
            }
        }

        Scenario("intercepting event") {
            beforeEachTest {
                every { mockInterceptor.onInterceptState(any()) } returns TestState(interceptedCounterValue)
                every { mockInterceptor.onInterceptEvent(any()) } returns InterceptedTestEvent()
                every { mockInterceptor.onInterceptAction(any()) } returns InterceptedTestAction()
                every { mockInterceptor.onInterceptReducer(any()) } returns InterceptedTestDecrementReducer()
            }

            When("view is resumed and action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(TestAction())
            }
            Then("event should be intercepted and replaced with the new one") {
                assertTrue { view.eventList[0] is InterceptedTestEvent }
            }
        }

        Scenario("intercepting reducer") {
            val stateSlot = slot<TestState>()
            beforeEachTest {
                every { mockInterceptor.onInterceptState(capture(stateSlot)) } answers { stateSlot.captured }
                every { mockInterceptor.onInterceptEvent(any()) } returns InterceptedTestEvent()
                every { mockInterceptor.onInterceptAction(any()) } returns InterceptedTestAction()
                every { mockInterceptor.onInterceptReducer(any()) } returns InterceptedTestDecrementReducer()
            }

            When("view is resumed and action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(TestAction())
            }
            Then("reducer should be intercepted and replaced with the new one which decrement the initial state") {
                assertEquals(2, view.stateList.size)
                assertEquals(TestState(initialCounterValue - 1), view.stateList[1])
            }
        }
    }

    Feature("interceptor with null values") {
        Scenario("intercepting state with null value") {
            beforeEachTest {
                every { mockInterceptor.onInterceptState(any()) } returns null
            }

            When("view is started") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnStart()
            }
            Then("state should be intercepted and ignored") {
                assertEquals(0, view.stateList.size)
            }
        }

        Scenario("intercepting action with null value") {
            beforeEachTest {
                every { mockInterceptor.onInterceptState(any()) } returns null
                every { mockInterceptor.onInterceptAction(any()) } returns null
            }

            When("view is created and action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnCreate()
                coreactor.sendAction(TestAction())
            }
            Then("action should be intercepted and ignored") {
                assertEquals(0, coreactor.actionList.size)
            }
        }

        Scenario("intercepting event with null value") {
            beforeEachTest {
                every { mockInterceptor.onInterceptState(any()) } returns TestState(interceptedCounterValue)
                every { mockInterceptor.onInterceptEvent(any()) } returns null
                every { mockInterceptor.onInterceptAction(any()) } returns InterceptedTestAction()
                every { mockInterceptor.onInterceptReducer(any()) } returns null
            }

            When("view is resumed and action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(TestAction())
            }
            Then("event should be intercepted and ignored") {
                assertEquals(0, view.eventList.size)
            }
        }

        Scenario("intercepting reducer with null value") {
            beforeEachTest {
                every { mockInterceptor.onInterceptState(any()) } returns TestState(interceptedCounterValue)
                every { mockInterceptor.onInterceptEvent(any()) } returns null
                every { mockInterceptor.onInterceptAction(any()) } returns InterceptedTestAction()
                every { mockInterceptor.onInterceptReducer(any()) } returns null
            }

            When("view is resumed and action is send") {
                coreactorHelper.attach()
                coreactorHelper.fromOnAttachToOnResume()
                coreactor.sendAction(TestAction())
            }
            Then("reducer should be intercepted and ignored") {
                assertEquals(1, view.stateList.size)
                assertEquals(TestState(interceptedCounterValue), view.stateList[0])
            }
        }
    }
})
