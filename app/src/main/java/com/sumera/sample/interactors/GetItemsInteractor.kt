package com.sumera.sample.interactors

import com.sumera.sample.data.Item
import com.sumera.sample.data.LoadingError
import com.sumera.sample.tools.Try
import com.sumera.sample.tools.randomBoolean
import com.sumera.sample.tools.randomDelay

class GetItemsInteractor {
    suspend fun execute(offset: Int): Try<List<Item>> {
        randomDelay()
        return if (randomBoolean()) {
            val items = List(10) { index ->
                val indexWithOffset = index + offset
                Item(indexWithOffset, "Item $indexWithOffset", false)
            }
            Try(items)
        } else {
            Try(LoadingError())
        }
    }
}
