package com.sumera.coreactor.contract.reducer

import com.sumera.coreactor.contract.state.State
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals

object ActionReducerSpec : Spek({

    data class TestState(val counter: Int = 0) : State

    Feature("Action reducer") {
        Scenario("Action reducer class") {
            lateinit var result: TestState
            When("reducer is invoked") {
                val actionReducer = ActionReducer<TestState> { state -> state.copy(counter = state.counter + 1) }
                result = actionReducer.reduce(TestState(0))
            }
            Then("should change state") {
                assertEquals(1, result.counter)
            }
        }

        Scenario("Action reducer function") {
            lateinit var result: TestState
            When("reducer is invoked") {
                val actionReducer = reducer<TestState> { state -> state.copy(counter = state.counter + 1) }
                result = actionReducer.reduce(TestState(0))
            }
            Then("should change state") {
                assertEquals(1, result.counter)
            }
        }
    }
})
