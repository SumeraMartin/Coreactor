package com.sumera.coreactor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.reflect.KClass

abstract class CoreactorFactory<COREACTOR : ViewModel> : ViewModelProvider.Factory {

    abstract val coreactor: COREACTOR

    abstract val coreactorClass: KClass<COREACTOR>

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return coreactor as T
    }
}
