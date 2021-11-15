package nl.joozd.joozdter.data.room

import androidx.room.*
import java.time.Instant

@Dao
interface EventDao {
    @Query("SELECT * FROM RoomEvent")
    suspend fun getAll(): List<RoomEvent>

    /**
     * Get events starting in a specific timeframe
     */
    @Query("SELECT * FROM RoomEvent WHERE startTime > :earliestStart AND startTime < :latestStart")
    suspend fun getEvents(earliestStart: Long = Instant.MIN.epochSecond, latestStart: Long = Instant.MAX.epochSecond): List<RoomEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(vararg events: RoomEvent)

    @Query("DELETE FROM RoomEvent WHERE id = :id")
    suspend fun deleteByID(vararg id: Long)

    @Query("DELETE FROM RoomEvent")
    suspend fun wipeDB()
}