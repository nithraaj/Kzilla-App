package org.kzilla.srmkzilla.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "registered_events")
data class RegisteredEvent (
    @PrimaryKey
    @ColumnInfo(name = "registration_id")
    var registrationId: String,
    @ColumnInfo(name="event_id") var eventId: String?,
    @ColumnInfo(name="event_name") var eventName: String?,
    @ColumnInfo(name="event_start") var eventStart: Date?,
    @ColumnInfo(name="event_end") var eventEnd: Date?,
    @ColumnInfo(name="event_date") var eventDate: String?,
    @ColumnInfo(name="event_venue") var eventVenue: String?,
    @ColumnInfo(name="last_refresh") var lastRefreshed: Date?
)