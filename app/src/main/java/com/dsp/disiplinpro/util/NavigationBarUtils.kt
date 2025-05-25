package com.dsp.disiplinpro.util

import android.content.Context

object NavigationBarUtils {

    const val NAVIGATION_MODE_THREE_BUTTON = 0
    const val NAVIGATION_MODE_TWO_BUTTON = 1
    const val NAVIGATION_MODE_GESTURE = 2

    fun getNavigationBarMode(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
        return if (resourceId > 0) {
            resources.getInteger(resourceId)
        } else {
            NAVIGATION_MODE_THREE_BUTTON
        }
    }

    fun isGestureNavigation(context: Context): Boolean {
        return getNavigationBarMode(context) == NAVIGATION_MODE_GESTURE
    }
}