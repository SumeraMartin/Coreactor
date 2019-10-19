package com.sumera.coreactor.internal.log

object Logger {

    interface Writer {
        fun logMessage(tag: String, message: String)
    }

    var writer: Writer = ConsoleLogger()

    fun log(tag: String, message: String) {
        writer.logMessage(tag, message)
    }
}
