package com.sumera.coreactor.internal

import android.app.Activity
import androidx.fragment.app.Fragment
import com.sumera.coreactor.error.CoreactorException

object ViewFinishingDetector {

    fun isFinishing(activity: Activity): Boolean {
        return !activity.isChangingConfigurations && activity.isFinishing
    }

    fun isFinishing(fragment: Fragment): Boolean {
        val activity = fragment.activity ?: throw CoreactorException("Fragment is not attached to activity $fragment")
        if (activity.isChangingConfigurations || activity.isFinishing) {
            return false
        }
        return fragment.isRemoving
    }
}
