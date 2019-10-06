package com.sumera.sample.tools

import kotlinx.coroutines.delay

fun randomBoolean(): Boolean {
    return Math.random() < 0.5
}

suspend fun randomDelay() {
    delay((1000L..3000L).random())
}
