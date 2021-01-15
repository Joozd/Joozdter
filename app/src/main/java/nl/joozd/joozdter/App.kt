package nl.joozd.joozdter

import android.app.Application
import android.content.Context

class App : Application() {
    companion object {
        private var INSTANCE: App? = null
        val instance: Context
            get() = INSTANCE ?: error ("NOT INITIALIZED AUB GRGR")
    }

    val ctx: Context by lazy { applicationContext }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }
}