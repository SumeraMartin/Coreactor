package com.sumera.coreactor

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.sumera.coreactor.contract.state.State

abstract class CoreactorFragment<STATE : State> : Fragment(), CoreactorOwner<STATE> {

    override val coreactorDelegate = CoreactorDelegate<STATE>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        coreactorDelegate.initCoreactor(coreactorOwner = this, lifecycle = lifecycle, fragment = this)
    }

    override fun onDestroy() {
        coreactorDelegate.destroyCoreactor(fragment = this)

        super.onDestroy()
    }
}
