package com.sumera.sample.interactors

import com.sumera.sample.data.Item
import com.sumera.sample.data.LoadingError
import com.sumera.sample.tools.Try
import com.sumera.sample.tools.randomBoolean
import com.sumera.sample.tools.randomDelay

class SetFavoriteItemStateInteractor {

    suspend fun execute(item: Item, isFavorite: Boolean): Try<Item> {
        randomDelay()
        return if(randomBoolean()) {
            Try(item.copy(isFavorite = isFavorite))
        } else {
            Try(LoadingError())
        }
    }
}
