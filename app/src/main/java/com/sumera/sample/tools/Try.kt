package com.sumera.sample.tools

data class Try<VALUE>(
    val value: VALUE?,
    val error: Throwable?
) {
    constructor(value: VALUE) : this(value, null)

    constructor(error: Throwable) : this(null, error)

    val isError = error != null

    val isValue = value != null

    val result = Pair(value, error)

    inline fun handleError(errorHandler: (Throwable) -> Unit) = apply {
        if (error != null) {
            errorHandler(error)
        }
    }

    inline fun handleValue(valueHandler: (VALUE) -> Unit) = apply {
        if (value != null) {
            valueHandler(value)
        }
    }

    inline fun unwrap(onValue: (VALUE) -> Unit, onError: (Throwable) -> Unit) {
        if (value != null) {
            onValue(value)
        }
        if (error != null) {
            onError(error)
        }
    }
}