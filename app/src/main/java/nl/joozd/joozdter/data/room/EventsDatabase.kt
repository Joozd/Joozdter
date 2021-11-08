package nl.joozd.joozdter.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RoomEvent::class], version = 1)
abstract class EventsDatabase: RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        private var INSTANCE: EventsDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): EventsDatabase = INSTANCE
        ?: Room.databaseBuilder(context.applicationContext,
            EventsDatabase::class.java,
            "events"
        ).build().also{
            INSTANCE = it
        }
    }
}