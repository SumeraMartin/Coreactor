package com.sumera.coreactor

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sumera.coreactor.contract.event.EventBehaviour
import com.sumera.coreactor.testutils.CoroutineRule
import com.sumera.coreactor.testutils.testactivity.TestCounterActivity
import com.sumera.coreactor.testutils.testcoreactor.DecrementAction
import com.sumera.coreactor.testutils.testcoreactor.DelayedDecrementAction
import com.sumera.coreactor.testutils.testcoreactor.IncrementAction
import com.sumera.coreactor.testutils.testcoreactor.MultipleDecrementAction
import com.sumera.coreactor.testutils.testcoreactor.SendEventAction
import com.sumera.coreactor.testutils.testcoreactor.TestCounterState
import com.sumera.coreactor.testutils.testcoreactor.TestEvent
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoreactorActivityTest {

    val coroutineRule = CoroutineRule()

    @Before
    fun setUp() {
        coroutineRule.setUp()
    }

    @After
    fun tearDown() {
        coroutineRule.tearDown()
    }

    @Test
    fun `coreactor should propagate the default state`() {
        // GIVEN
        val scenario = ActivityScenario.launch(TestCounterActivity::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // WHEN

        // THEN
        scenario.onActivity { activity ->
            verifySequence {
                activity.mockView.onState(TestCounterState(0))
            }
        }
    }

    @Test
    fun `coreactor should propagate changed state when action is send`() {
        // GIVEN
        val scenario = ActivityScenario.launch(TestCounterActivity::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // WHEN
        scenario.onActivity { activity ->
            activity.action(IncrementAction)
        }

        // THEN
        scenario.onActivity { activity ->
            verifySequence {
                activity.mockView.onState(TestCounterState(0))
                activity.mockView.onState(TestCounterState(1))
            }
        }
    }

    @Test
    fun `coreactor should propagate changed states when multiple actions are send`() {
        // GIVEN
        val scenario = ActivityScenario.launch(TestCounterActivity::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // WHEN
        scenario.onActivity { activity ->
            activity.action(IncrementAction)
            activity.action(IncrementAction)
            activity.action(IncrementAction)
            activity.action(DecrementAction)
        }

        // THEN
        scenario.onActivity { activity ->
            verifySequence {
                activity.mockView.onState(TestCounterState(0))
                activity.mockView.onState(TestCounterState(1))
                activity.mockView.onState(TestCounterState(2))
                activity.mockView.onState(TestCounterState(3))
                activity.mockView.onState(TestCounterState(2))
            }
        }
    }

    @Test
    fun `coreactor should propagate changed states when multiple reducers are send`() {
        // GIVEN
        val scenario = ActivityScenario.launch(TestCounterActivity::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // WHEN
        scenario.onActivity { activity ->
            activity.action(MultipleDecrementAction)
        }

        // THEN
        scenario.onActivity { activity ->
            verifySequence {
                activity.mockView.onState(TestCounterState(0))
                activity.mockView.onState(TestCounterState(-1))
                activity.mockView.onState(TestCounterState(-2))
                activity.mockView.onState(TestCounterState(-3))
            }
        }
    }

    @Test
    fun `coreactor should propagate changed states when delayed action is send`() {
        // GIVEN
        val scenario = ActivityScenario.launch(TestCounterActivity::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // WHEN
        scenario.onActivity { activity ->
            activity.action(DelayedDecrementAction)
            activity.action(IncrementAction)
            activity.action(IncrementAction)
            coroutineRule.advanceTimeBy(1000)
        }

        // THEN
        scenario.onActivity { activity ->
            verifySequence {
                activity.mockView.onState(TestCounterState(0))
                activity.mockView.onState(TestCounterState(1))
                activity.mockView.onState(TestCounterState(2))
                activity.mockView.onState(TestCounterState(1))
            }
        }
    }

    @Test
    fun `coreactor should send event when event action is send`() {
        // GIVEN
        val scenario = ActivityScenario.launch(TestCounterActivity::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // WHEN
        scenario.onActivity { activity ->
            activity.action(SendEventAction("message", EventBehaviour.DISPATCH_EVERY_TIME))
        }

        // THEN
        scenario.onActivity { activity ->
            verify {
                activity.mockView.onEvent(TestEvent("message", EventBehaviour.DISPATCH_EVERY_TIME))
            }
        }
    }

    @Test
    fun `coreactor should emit the previous state after recreate`() {
        // GIVEN
        val scenario = ActivityScenario.launch(TestCounterActivity::class.java)

        // WHEN
        scenario.onActivity { activity ->
            activity.action(IncrementAction)
        }
        scenario.recreate()

        // THEN
        scenario.onActivity { activity ->
            verifySequence {
                activity.mockView.onState(TestCounterState(1))
            }
        }
    }

    @Test
    fun `coreactor should emit the previous state after recreate with delayed action and receive state produced by delayed reducer`() {
        // GIVEN
        val scenario = ActivityScenario.launch(TestCounterActivity::class.java)

        // WHEN
        scenario.onActivity { activity ->
            activity.action(IncrementAction)
            activity.action(DelayedDecrementAction)
        }
        scenario.recreate()
        coroutineRule.advanceTimeBy(1000)

        // THEN
        scenario.onActivity { activity ->
            verifySequence {
                activity.mockView.onState(TestCounterState(1))
                activity.mockView.onState(TestCounterState(0))
            }
        }
    }
}
