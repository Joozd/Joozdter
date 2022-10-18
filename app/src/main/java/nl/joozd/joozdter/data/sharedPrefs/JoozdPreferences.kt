package nl.joozd.joozdter.data.sharedPrefs

import android.content.Context
import nl.joozd.joozdter.App
import nl.joozd.joozdter.data.DataStoreProviderNoMigration

/**
 * define preferences as follows:
 * var somePreference: Boolean by JoozdLogSharedPreference(dataStore, true)
 * val somePreferenceFlow by PrefsFlow(somePreference)
 * @NOTE The Flow MUST have the same name as the property plus the word "Flow" or it won't work.
 */
abstract class JoozdPreferences {
    protected abstract val preferencesFileKey: String

    protected val context: Context get () = App.instance

    //Initialized lazy because initializing it immediately would use uninitialized FILE_KEY.
    val dataStore by lazy {
        DataStoreProviderNoMigration(context,preferencesFileKey).dataStore
    }
}