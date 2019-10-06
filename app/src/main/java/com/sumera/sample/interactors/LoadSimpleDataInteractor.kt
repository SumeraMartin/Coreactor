package com.sumera.sample.interactors

import com.sumera.sample.data.LoadingError
import com.sumera.sample.tools.Try
import com.sumera.sample.tools.randomBoolean
import com.sumera.sample.tools.randomDelay

class LoadSimpleDataInteractor {

    suspend fun execute(): Try<String> {
        randomDelay()

        return if (randomBoolean()) {
            Try("Result :-)")
        } else {
            Try(LoadingError())
        }
    }
}
