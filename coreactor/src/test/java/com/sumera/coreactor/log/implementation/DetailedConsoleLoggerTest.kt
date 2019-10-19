package com.sumera.coreactor.log.implementation

import com.sumera.coreactor.contract.action.Action
import com.sumera.coreactor.contract.event.Event
import com.sumera.coreactor.contract.reducer.Reducer
import com.sumera.coreactor.internal.log.ConsoleLogger
import com.sumera.coreactor.internal.log.Logger
import com.sumera.coreactor.lifecycle.LifecycleState
import com.sumera.coreactor.testutils.TestState
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals

class DetailedConsoleLoggerTest : Spek({

    val tag = "TAG"

    class TestEvent : Event<TestState>() {
        override fun toString(): String {
            return "TestEvent()"
        }
    }

    class TestAction : Action<TestState> {
        override fun toString(): String {
            return "TestAction()"
        }
    }

    class TestReducer : Reducer<TestState>() {
        override fun reduce(oldState: TestState): TestState {
            return oldState
        }
        override fun toString(): String {
            return "TestReducer()"
        }
    }

    val testWriter by memoized(CachingMode.EACH_GROUP) {
        object : Logger.Writer {
            val messages = mutableListOf<Pair<String, String>>()
            override fun logMessage(tag: String, message: String) {
                messages.add(tag to message)
            }
        }
    }

    val testLogger by memoized(CachingMode.EACH_GROUP) {
        DetailedConsoleLogger<TestState>(tag)
    }

    beforeEachGroup {
        Logger.writer = testWriter
    }

    afterEachGroup {
        Logger.writer = ConsoleLogger()
    }

    Feature("methods are called with correct data") {
        Scenario("onNewStateReceived") {
            When("called with state") {
                testLogger.onNewStateReceived(TestState(10))
            }
            Then("should log expected message") {
                assertEquals(tag, testWriter.messages[0].first)
                assertEquals("State received: TestState(counter=10)", testWriter.messages[0].second)
            }
        }
        Scenario("onStateDispatchedToView") {
            When("called with state") {
                testLogger.onStateDispatchedToView(TestState(10))
            }
            Then("should log expected message") {
                assertEquals(tag, testWriter.messages[0].first)
                assertEquals("State dispatched: TestState(counter=10)", testWriter.messages[0].second)
            }
        }
        Scenario("onAction") {
            When("called with action") {
                testLogger.onAction(TestAction())
            }
            Then("should log expected message") {
                assertEquals(tag, testWriter.messages[0].first)
                assertEquals("Action: TestAction()", testWriter.messages[0].second)
            }
        }
        Scenario("onReducer") {
            When("called with reducer") {
                testLogger.onReducer(TestState(0), TestReducer(), TestState(1))
            }
            Then("should log expected message") {
                assertEquals(tag, testWriter.messages[0].first)
                assertEquals(tag, testWriter.messages[1].first)
                assertEquals(tag, testWriter.messages[2].first)
                assertEquals(tag, testWriter.messages[3].first)
                assertEquals(tag, testWriter.messages[4].first)
                assertEquals("Reducer <<<<<<", testWriter.messages[0].second)
                assertEquals("Reducer old state: TestState(counter=0)", testWriter.messages[1].second)
                assertEquals("Reducer: TestReducer()", testWriter.messages[2].second)
                assertEquals("Reducer new state: TestState(counter=1)", testWriter.messages[3].second)
                assertEquals("Reducer >>>>>>", testWriter.messages[4].second)
            }
        }
        Scenario("onLifecycle") {
            When("called with lifecycle") {
                testLogger.onLifecycle(LifecycleState.ON_CREATE)
            }
            Then("should log expected message") {
                assertEquals(tag, testWriter.messages[0].first)
                assertEquals("Lifecycle: ON_CREATE", testWriter.messages[0].second)
            }
        }
        Scenario("onEventEmitted") {
            When("called with event") {
                testLogger.onEventEmitted(TestEvent())
            }
            Then("should log expected message") {
                assertEquals(tag, testWriter.messages[0].first)
                assertEquals("Event emitted: TestEvent()", testWriter.messages[0].second)
            }
        }
        Scenario("onEventDispatchedToView") {
            When("called with event") {
                testLogger.onEventDispatchedToView(TestEvent())
            }
            Then("should log expected message") {
                assertEquals(tag, testWriter.messages[0].first)
                assertEquals("Event dispatched: TestEvent()", testWriter.messages[0].second)
            }
        }
        Scenario("onEventThrownAway") {
            When("called with event") {
                testLogger.onEventThrownAway(TestEvent())
            }
            Then("should log expected message") {
                assertEquals(tag, testWriter.messages[0].first)
                assertEquals("Event thrown away: TestEvent()", testWriter.messages[0].second)
            }
        }
        Scenario("onEventWaitingForStartedView") {
            When("called with event") {
                testLogger.onEventWaitingForStartedView(TestEvent())
            }
            Then("should log expected message") {
                assertEquals(tag, testWriter.messages[0].first)
                assertEquals("Event waiting for started view: TestEvent()", testWriter.messages[0].second)
            }
        }
        Scenario("onEventWaitingForCreatedView") {
            When("called with event") {
                testLogger.onEventWaitingForCreatedView(TestEvent())
            }
            Then("should log expected message") {
                assertEquals(tag, testWriter.messages[0].first)
                assertEquals("Event waiting for created view: TestEvent()", testWriter.messages[0].second)
            }
        }
    }
})