package ru.custle.mobile

import android.app.Application
import ru.custle.mobile.core.data.AppContainer

class CustleApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
