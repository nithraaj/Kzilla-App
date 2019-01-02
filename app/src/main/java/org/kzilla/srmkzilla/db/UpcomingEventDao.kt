package org.kzilla.srmkzilla.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import java.util.*


@Dao
interface UpcomingEventDao {

    @Insert(onConflict = REPLACE)
    fun save(vararg event:UpcomingEvent)

    @Delete
    fun delete(event:UpcomingEvent)


    @Query("SELECT * FROM upcoming_events")
    fun getAll(): List<UpcomingEvent>



    @Query("SELECT * FROM upcoming_events")
    fun getAllLive(): LiveData<List<UpcomingEvent>>


    @Query("SELECT * FROM upcoming_events where event_id = :eventId")
    fun getById(eventId:String): UpcomingEvent

    @Query("SELECT * FROM upcoming_events where event_id = :eventId")
    fun getByIdLive(eventId:String): LiveData<UpcomingEvent>

    @Query("SELECT * FROM upcoming_events WHERE event_id = :eventId AND last_refresh > :lastRefreshMax LIMIT 1")
    fun hasEvent(eventId: String, lastRefreshMax: Date): UpcomingEvent?

    @Query("SELECT min(last_refresh) FROM upcoming_events")
    fun lastRefreshed():Date?

}