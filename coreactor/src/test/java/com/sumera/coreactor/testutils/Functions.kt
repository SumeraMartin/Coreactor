package com.sumera.coreactor.testutils

fun catch(throwBlock: () -> Unit): Throwable {
    try {
        throwBlock()
    } catch (e: Throwable) {
        return e
    }
    throw AssertionError("Exception was not thrown")
}
