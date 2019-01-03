package org.kzilla.srmkzilla.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import java.util.*


@Dao
interface RegisteredEventDao {

    @Insert(onConflict = REPLACE)
    fun save(vararg event:RegisteredEvent)

    @Delete
    fun delete(event:RegisteredEvent)


    @Query("SELECT * FROM registered_events")
    fun getAll(): List<RegisteredEvent>



    @Query("SELECT * FROM registered_events")
    fun getAllLive(): LiveData<List<RegisteredEvent>>


    @Query("SELECT * FROM registered_events where registration_id = :registrationId")
    fun getById(registrationId:String): RegisteredEvent

    @Query("SELECT * FROM registered_events where registration_id = :registrationId")
    fun getByIdLive(registrationId:String): LiveData<RegisteredEvent>

    @Query("SELECT * FROM registered_events WHERE registration_id = :registrationId AND last_refresh > :lastRefreshMax LIMIT 1")
    fun hasEvent(registrationId: String, lastRefreshMax: Date): RegisteredEvent?

    @Query("SELECT min(last_refresh) FROM registered_events")
    fun lastRefreshed():Date?

}