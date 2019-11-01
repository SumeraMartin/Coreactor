package com.sumera.coreactor.internal.assert

import android.os.Looper
import com.sumera.coreactor.error.CoreactorException

internal object MainThreadChecker {

    var ignoreCheck = false

    fun requireMainThread(methodName: String) {
        if (ignoreCheck) {
            return
        }
        if (Looper.getMainLooper().thread != Thread.currentThread()) {
            throw CoreactorException("$methodName method is not called on the main thread")
        }
    }
}
