package com.sumera.coreactor.internal.log

import android.util.Log

class ConsoleLogger : Logger.Writer {
    override fun logMessage(tag: String, message: String) {
        Log.d(tag, message)
    }
}
