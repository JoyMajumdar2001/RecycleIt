package com.qdot.recycleit

import android.app.Application
import com.google.android.material.color.DynamicColors

class RecycleApp:Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}