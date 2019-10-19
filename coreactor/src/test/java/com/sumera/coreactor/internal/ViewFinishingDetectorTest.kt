package com.sumera.coreactor.internal

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.sumera.coreactor.error.CoreactorException
import com.sumera.coreactor.testutils.catch
import io.mockk.every
import io.mockk.mockk
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertTrue

class ViewFinishingDetectorTest : Spek({

    Feature("Detect if activity if finishing") {
        val mockActivity by memoized(CachingMode.EACH_GROUP) { mockk<Activity>() }

        Scenario("activity with changing configurations") {
            Given("isFinishing set to true") {
                every { mockActivity.isChangingConfigurations } returns true
            }
            var result: Boolean? = null
            When("isFinishing is called") {
                result = ViewFinishingDetector.isFinishing(mockActivity)
            }
            Then("should return false") {
                assertTrue { result == false }
            }
        }

        Scenario("finishing activity without changing configurations") {
            Given("isFinishing set to true") {
                every { mockActivity.isChangingConfigurations } returns false
                every { mockActivity.isFinishing } returns true
            }
            var result: Boolean? = null
            When("isFinishing is called") {
                result = ViewFinishingDetector.isFinishing(mockActivity)
            }
            Then("should return true") {
                assertTrue { result == true }
            }
        }

        Scenario("not finishing activity without changing configurations") {
            Given("isFinishing set to false") {
                every { mockActivity.isChangingConfigurations } returns false
                every { mockActivity.isFinishing } returns false
            }
            var result: Boolean? = null
            When("isFinishing is called") {
                result = ViewFinishingDetector.isFinishing(mockActivity)
            }
            Then("should return false") {
                assertTrue { result == false }
            }
        }
    }

    Feature("Detect if fragment if finishing") {
        val mockFragment by memoized(CachingMode.EACH_GROUP) { mockk<Fragment>() }
        val mockActivity by memoized(CachingMode.EACH_GROUP) { mockk<FragmentActivity>() }

        Scenario("fragment without attached activity") {
            Given("attached activity is null") {
                every { mockFragment.activity } returns null
            }
            lateinit var result: Throwable
            When("isFinishing is called") {
                result = catch { ViewFinishingDetector.isFinishing(mockFragment) }
            }
            Then("should throw exception") {
                assertTrue { result is CoreactorException }
            }
        }

        Scenario("fragment with changing configurations") {
            Given("isChangingConfigurations set to true") {
                every { mockFragment.activity } returns mockActivity
                every { mockActivity.isChangingConfigurations } returns true
            }
            var result: Boolean? = null
            When("isFinishing is called") {
                result = ViewFinishingDetector.isFinishing(mockFragment)
            }
            Then("should return false") {
                assertTrue { result == false }
            }
        }

        Scenario("fragment with finishing activity and without changing configurations") {
            Given("isFinishing set to true") {
                every { mockFragment.activity } returns mockActivity
                every { mockActivity.isChangingConfigurations } returns false
                every { mockActivity.isFinishing } returns true
            }
            var result: Boolean? = null
            When("isFinishing is called") {
                result = ViewFinishingDetector.isFinishing(mockFragment)
            }
            Then("should return false") {
                assertTrue { result == false }
            }
        }

        Scenario("not removing fragment without finishing activity and without changing configurations") {
            Given("isChangingConfigurations set to true") {
                every { mockFragment.activity } returns mockActivity
                every { mockActivity.isChangingConfigurations } returns false
                every { mockActivity.isFinishing } returns false
                every { mockFragment.isRemoving } returns false
            }
            var result: Boolean? = null
            When("isFinishing is called") {
                result = ViewFinishingDetector.isFinishing(mockFragment)
            }
            Then("should return false") {
                assertTrue { result == false }
            }
        }

        Scenario("removing fragment without finishing activity and without changing configurations") {
            Given("isChangingConfigurations set to true") {
                every { mockFragment.activity } returns mockActivity
                every { mockActivity.isChangingConfigurations } returns false
                every { mockActivity.isFinishing } returns false
                every { mockFragment.isRemoving } returns true
            }
            var result: Boolean? = null
            When("isFinishing is called") {
                result = ViewFinishingDetector.isFinishing(mockFragment)
            }
            Then("should return true") {
                assertTrue { result == true }
            }
        }
    }
})