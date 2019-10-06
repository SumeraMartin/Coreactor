package com.sumera.sample.tools.extensions

import android.view.View

var View.isVisible: Boolean
    set(value) {
        visibility = if (value) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
    get() {
        return visibility == View.VISIBLE
    }
