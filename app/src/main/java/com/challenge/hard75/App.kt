package com.challenge.hard75

import android.app.Application
import com.challenge.hard75.worker.ResetWorker

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ResetWorker.schedule(this)
    }
}
