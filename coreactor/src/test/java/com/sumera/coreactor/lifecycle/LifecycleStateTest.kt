package com.sumera.coreactor.lifecycle

import androidx.lifecycle.Lifecycle
import com.sumera.coreactor.error.CoreactorException
import com.sumera.coreactor.testutils.catch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LifecycleStateTest : Spek({

    Feature("is state methods") {
        Scenario("isInitialState") {
            Then("should be true for INITIAL state") {
                assertTrue { LifecycleState.INITIAL.isInitialState }
            }
        }
        Scenario("isAttachState") {
            Then("should be true for ON_ATTACH state") {
                assertTrue { LifecycleState.ON_ATTACH.isAttachState }
            }
        }
        Scenario("isCreateState") {
            Then("should be true for ON_CREATE state") {
                assertTrue { LifecycleState.ON_CREATE.isCreateState }
            }
        }
        Scenario("isStartState") {
            Then("should be true for ON_START state") {
                assertTrue { LifecycleState.ON_START.isStartState }
            }
        }
        Scenario("isResumeState") {
            Then("should be true for ON_RESUME state") {
                assertTrue { LifecycleState.ON_RESUME.isResumeState }
            }
        }
        Scenario("isPauseState") {
            Then("should be true for ON_PAUSE state") {
                assertTrue { LifecycleState.ON_PAUSE.isPauseState }
            }
        }
        Scenario("isStopState") {
            Then("should be true for ON_STOP state") {
                assertTrue { LifecycleState.ON_STOP.isStopState }
            }
        }
        Scenario("isDestroyState") {
            Then("should be true for ON_DESTROY state") {
                assertTrue { LifecycleState.ON_DESTROY.isDestroyState }
            }
        }
        Scenario("isDetachState") {
            Then("should be true for ON_DETACH state") {
                assertTrue { LifecycleState.ON_DETACH.isDetachState }
            }
        }
    }

    Feature("is in state methods") {
        Scenario("isInCreatedState") {
            Then("should be false for INITIAL state") {
                assertFalse { LifecycleState.INITIAL.isInCreatedState }
            }
            Then("should be false for ON_ATTACH state") {
                assertFalse { LifecycleState.ON_ATTACH.isInCreatedState }
            }
            Then("should be true for ON_CREATE state") {
                assertTrue { LifecycleState.ON_CREATE.isInCreatedState }
            }
            Then("should be true for ON_START state") {
                assertTrue { LifecycleState.ON_START.isInCreatedState }
            }
            Then("should be true for ON_RESUME state") {
                assertTrue { LifecycleState.ON_RESUME.isInCreatedState }
            }
            Then("should be true for ON_PAUSE state") {
                assertTrue { LifecycleState.ON_PAUSE.isInCreatedState }
            }
            Then("should be true for ON_STOP state") {
                assertTrue { LifecycleState.ON_STOP.isInCreatedState }
            }
            Then("should be false for ON_DESTROY state") {
                assertFalse { LifecycleState.ON_DESTROY.isInCreatedState }
            }
            Then("should be false for ON_DETACH state") {
                assertFalse { LifecycleState.ON_DETACH.isInCreatedState }
            }
        }
        Scenario("isInStartedState") {
            Then("should be false for INITIAL state") {
                assertFalse { LifecycleState.INITIAL.isInStartedState }
            }
            Then("should be false for ON_ATTACH state") {
                assertFalse { LifecycleState.ON_ATTACH.isInStartedState }
            }
            Then("should be false for ON_CREATE state") {
                assertFalse { LifecycleState.ON_CREATE.isInStartedState }
            }
            Then("should be true for ON_START state") {
                assertTrue { LifecycleState.ON_START.isInStartedState }
            }
            Then("should be true for ON_RESUME state") {
                assertTrue { LifecycleState.ON_RESUME.isInStartedState }
            }
            Then("should be true for ON_PAUSE state") {
                assertTrue { LifecycleState.ON_PAUSE.isInStartedState }
            }
            Then("should be false for ON_STOP state") {
                assertFalse { LifecycleState.ON_STOP.isInStartedState }
            }
            Then("should be false for ON_DESTROY state") {
                assertFalse { LifecycleState.ON_DESTROY.isInStartedState }
            }
            Then("should be false for ON_DETACH state") {
                assertFalse { LifecycleState.ON_DETACH.isInStartedState }
            }
        }
        Scenario("isInResumedState") {
            Then("should be false for INITIAL state") {
                assertFalse { LifecycleState.INITIAL.isInResumedState }
            }
            Then("should be false for ON_ATTACH state") {
                assertFalse { LifecycleState.ON_ATTACH.isInResumedState }
            }
            Then("should be false for ON_CREATE state") {
                assertFalse { LifecycleState.ON_CREATE.isInResumedState }
            }
            Then("should be false for ON_START state") {
                assertFalse { LifecycleState.ON_START.isInResumedState }
            }
            Then("should be true for ON_RESUME state") {
                assertTrue { LifecycleState.ON_RESUME.isInResumedState }
            }
            Then("should be false for ON_PAUSE state") {
                assertFalse { LifecycleState.ON_PAUSE.isInResumedState }
            }
            Then("should be false for ON_STOP state") {
                assertFalse { LifecycleState.ON_STOP.isInResumedState }
            }
            Then("should be false for ON_DESTROY state") {
                assertFalse { LifecycleState.ON_DESTROY.isInResumedState }
            }
            Then("should be false for ON_DETACH state") {
                assertFalse { LifecycleState.ON_DETACH.isInResumedState }
            }
        }
    }

    Feature("fromLifecycle") {
        Scenario("transforms ON_CREATE to correct state") {
            lateinit var result: LifecycleState
            When("ON_CREATE passed as argument") {
                result = LifecycleState.fromLifecycle(Lifecycle.Event.ON_CREATE)
            }
            Then("should be mapped to ON_CREATE") {
                assertEquals(LifecycleState.ON_CREATE, result)
            }
        }
        Scenario("transforms ON_START to correct state") {
            lateinit var result: LifecycleState
            When("ON_START passed as argument") {
                result = LifecycleState.fromLifecycle(Lifecycle.Event.ON_START)
            }
            Then("should be mapped to ON_START") {
                assertEquals(LifecycleState.ON_START, result)
            }
        }
        Scenario("transforms ON_RESUME to correct state") {
            lateinit var result: LifecycleState
            When("ON_RESUME passed as argument") {
                result = LifecycleState.fromLifecycle(Lifecycle.Event.ON_RESUME)
            }
            Then("should be mapped to ON_RESUME") {
                assertEquals(LifecycleState.ON_RESUME, result)
            }
        }
        Scenario("transforms ON_PAUSE to correct state") {
            lateinit var result: LifecycleState
            When("ON_PAUSE passed as argument") {
                result = LifecycleState.fromLifecycle(Lifecycle.Event.ON_PAUSE)
            }
            Then("should be mapped to ON_PAUSE") {
                assertEquals(LifecycleState.ON_PAUSE, result)
            }
        }
        Scenario("transforms ON_STOP to correct state") {
            lateinit var result: LifecycleState
            When("ON_STOP passed as argument") {
                result = LifecycleState.fromLifecycle(Lifecycle.Event.ON_STOP)
            }
            Then("should be mapped to ON_STOP") {
                assertEquals(LifecycleState.ON_STOP, result)
            }
        }
        Scenario("transforms ON_DESTROY to correct state") {
            lateinit var result: LifecycleState
            When("ON_DESTROY passed as argument") {
                result = LifecycleState.fromLifecycle(Lifecycle.Event.ON_DESTROY)
            }
            Then("should be mapped to ON_DESTROY") {
                assertEquals(LifecycleState.ON_DESTROY, result)
            }
        }
        Scenario("throws error for ON_ANY") {
            lateinit var result: Throwable
            When("ON_ANY passed as argument") {
                result = catch { LifecycleState.fromLifecycle(Lifecycle.Event.ON_ANY) }
            }
            Then("should be mapped to ON_ANY") {
                assertTrue { result is CoreactorException }
            }
        }
    }
})