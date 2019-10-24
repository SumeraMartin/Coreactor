package com.sumera.coreactor

import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sumera.coreactor.contract.event.EventBehaviour
import com.sumera.coreactor.testutils.CoroutineRule
import com.sumera.coreactor.testutils.testfragment.DecrementAction
import com.sumera.coreactor.testutils.testfragment.DelayedDecrementAction
import com.sumera.coreactor.testutils.testfragment.IncrementAction
import com.sumera.coreactor.testutils.testfragment.MultipleDecrementAction
import com.sumera.coreactor.testutils.testfragment.SendEventAction
import com.sumera.coreactor.testutils.testfragment.TestCounterFragment
import com.sumera.coreactor.testutils.testfragment.TestCounterState
import com.sumera.coreactor.testutils.testfragment.TestEvent
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoreactorFragmentTest {

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
        val scenario = FragmentScenario.launch(TestCounterFragment::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // WHEN

        // THEN
        scenario.onFragment { fragment ->
            verifySequence {
                fragment.mockView.onState(TestCounterState(0))
            }
        }
    }

    @Test
    fun `coreactor should propagate changed state when action is send`() {
        // GIVEN
        val scenario = FragmentScenario.launch(TestCounterFragment::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // WHEN
        scenario.onFragment { fragment ->
            fragment.action(IncrementAction)
        }

        // THEN
        scenario.onFragment { fragment ->
            verifySequence {
                fragment.mockView.onState(TestCounterState(0))
                fragment.mockView.onState(TestCounterState(1))
            }
        }
    }

    @Test
    fun `coreactor should propagate changed states when multiple actions are send`() {
        // GIVEN
        val scenario = FragmentScenario.launch(TestCounterFragment::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // WHEN
        scenario.onFragment { fragment ->
            fragment.action(IncrementAction)
            fragment.action(IncrementAction)
            fragment.action(IncrementAction)
            fragment.action(DecrementAction)
        }

        // THEN
        scenario.onFragment { fragment ->
            verifySequence {
                fragment.mockView.onState(TestCounterState(0))
                fragment.mockView.onState(TestCounterState(1))
                fragment.mockView.onState(TestCounterState(2))
                fragment.mockView.onState(TestCounterState(3))
                fragment.mockView.onState(TestCounterState(2))
            }
        }
    }

    @Test
    fun `coreactor should propagate changed states when multiple reducers are send`() {
        // GIVEN
        val scenario = FragmentScenario.launch(TestCounterFragment::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // WHEN
        scenario.onFragment { fragment ->
            fragment.action(MultipleDecrementAction)
        }

        // THEN
        scenario.onFragment { fragment ->
            verifySequence {
                fragment.mockView.onState(TestCounterState(0))
                fragment.mockView.onState(TestCounterState(-1))
                fragment.mockView.onState(TestCounterState(-2))
                fragment.mockView.onState(TestCounterState(-3))
            }
        }
    }

    @Test
    fun `coreactor should propagate changed states when delayed action is send`() {
        // GIVEN
        val scenario = FragmentScenario.launch(TestCounterFragment::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // WHEN
        scenario.onFragment { fragment ->
            fragment.action(DelayedDecrementAction)
            fragment.action(IncrementAction)
            fragment.action(IncrementAction)
            coroutineRule.advanceTimeBy(1000)
        }

        // THEN
        scenario.onFragment { fragment ->
            verifySequence {
                fragment.mockView.onState(TestCounterState(0))
                fragment.mockView.onState(TestCounterState(1))
                fragment.mockView.onState(TestCounterState(2))
                fragment.mockView.onState(TestCounterState(1))
            }
        }
    }

    @Test
    fun `coreactor should send event when event action is send`() {
        // GIVEN
        val scenario = FragmentScenario.launch(TestCounterFragment::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // WHEN
        scenario.onFragment { fragment ->
            fragment.action(SendEventAction("message", EventBehaviour.DISPATCH_EVERY_TIME))
        }

        // THEN
        scenario.onFragment { fragment ->
            verify {
                fragment.mockView.onEvent(TestEvent("message", EventBehaviour.DISPATCH_EVERY_TIME))
            }
        }
    }

    @Test
    fun `coreactor should emit the previous state after recreate`() {
        // GIVEN
        val scenario = FragmentScenario.launch(TestCounterFragment::class.java)

        // WHEN
        scenario.onFragment { fragment ->
            fragment.action(IncrementAction)
        }
        scenario.recreate()

        // THEN
        scenario.onFragment { fragment ->
            verifySequence {
                fragment.mockView.onState(TestCounterState(1))
            }
        }
    }

    @Test
    fun `coreactor should emit the previous state after recreate with delayed action and receive state produced by delayed reducer`() {
        // GIVEN
        val scenario = FragmentScenario.launch(TestCounterFragment::class.java)

        // WHEN
        scenario.onFragment { fragment ->
            fragment.action(IncrementAction)
            fragment.action(DelayedDecrementAction)
        }
        scenario.recreate()
        coroutineRule.advanceTimeBy(1000)

        // THEN
        scenario.onFragment { fragment ->
            verifySequence {
                fragment.mockView.onState(TestCounterState(1))
                fragment.mockView.onState(TestCounterState(0))
            }
        }
    }
}
