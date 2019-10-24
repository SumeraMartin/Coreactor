package com.sumera.coreactor

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.sumera.coreactor.contract.state.State

abstract class CoreactorActivity<STATE : State> : AppCompatActivity(), CoreactorOwner<STATE> {

    @LayoutRes abstract fun layoutRes(): Int

    override val coreactorDelegate = CoreactorDelegate<STATE>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layoutRes())

        coreactorDelegate.initCoreactor(coreactorOwner = this, lifecycle = lifecycle, activity = this)
    }
}
