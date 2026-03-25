package com.mumu.app

import android.app.Application
import com.mumu.app.notification.NotificationHelper

class MuMuApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
