package com.sumera.coreactor.error

class CoreactorException(msg: String = "Oops! Looks like an internal error.") : Throwable("Coreactor error: $msg")
