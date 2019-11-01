package com.sumera.coreactor.testutils

import com.sumera.coreactor.internal.assert.MainThreadChecker

class MainThreadCheckRule {

    fun setUp() {
        MainThreadChecker.ignoreCheck = true
    }

    fun tearDown() {
        MainThreadChecker.ignoreCheck = false
    }
}