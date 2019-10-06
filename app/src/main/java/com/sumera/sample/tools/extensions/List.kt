package com.sumera.sample.tools.extensions

fun <E> List<E>.replace(new: E, predicate: (E) -> Boolean): List<E> {
    return map { current ->
        if (predicate(current)) {
            new
        } else {
            current
        }
    }
}