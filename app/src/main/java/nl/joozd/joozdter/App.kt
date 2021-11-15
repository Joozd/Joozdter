package nl.joozd.joozdter

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class App : Application() {
    val ctx: Context by lazy { applicationContext }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }

    fun checkCalendarWritePermission(): Boolean {
        val result = ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_CALENDAR
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private var INSTANCE: App? = null
        val instance: App
            get() = INSTANCE ?: error ("NOT INITIALIZED AUB GRGR")
    }
}