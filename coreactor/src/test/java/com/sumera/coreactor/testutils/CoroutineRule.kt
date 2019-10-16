package com.sumera.coreactor.testutils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

class CoroutineRule {

    lateinit var testDispatcher: TestCoroutineDispatcher
    lateinit var testScope: TestCoroutineScope

    fun setUp() {
        testDispatcher = TestCoroutineDispatcher()
        testScope = TestCoroutineScope(testDispatcher)
        Dispatchers.setMain(testDispatcher)
    }

    fun advanceTimeBy(time: Long) {
        testDispatcher.advanceTimeBy(time)
    }

    fun tearDown() {
        Dispatchers.resetMain()
    }
}
