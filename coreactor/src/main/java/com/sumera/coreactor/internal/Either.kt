package com.sumera.coreactor.internal

sealed class Either<out LEFT, out RIGHT> {

    data class Left<out LEFT>(val value: LEFT) : Either<LEFT, Nothing>()

    data class Right<out RIGHT>(val value: RIGHT) : Either<Nothing, RIGHT>()
}