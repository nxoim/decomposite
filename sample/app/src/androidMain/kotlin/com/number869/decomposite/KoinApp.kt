package com.number869.decomposite

import android.app.Application
import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext.startKoin

class KoinApp : Application() {
    companion object {
        lateinit var koinInstance: KoinApplication
    }

    override fun onCreate() {
        super.onCreate()

        koinInstance = startKoin { }
    }
}