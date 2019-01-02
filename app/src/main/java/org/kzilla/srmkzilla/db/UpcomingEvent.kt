package org.kzilla.srmkzilla.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "upcoming_events")
data class UpcomingEvent (
    @PrimaryKey
    @ColumnInfo(name = "event_id")
    var eventId: String,
    @ColumnInfo(name="event_info") var eventContent: String?,
    @ColumnInfo(name="event_name") var eventName: String?,
    @ColumnInfo(name="event_start") var eventStart: Date?,
    @ColumnInfo(name="event_icon") var eventIcon: String?,
    @ColumnInfo(name="event_banner_land") var eventBannerLand: String?,
    @ColumnInfo(name="event_banner_port") var eventBannerPort: String?,
    @ColumnInfo(name="event_end") var eventEnd: Date?,
    @ColumnInfo(name="event_reg_open") var eventRegOpen: Date?,
    @ColumnInfo(name="event_reg_close") var eventRegClose: Date?,
    @ColumnInfo(name="event_venue") var eventVenue: String?,
    @ColumnInfo(name="event_allowed_depts") var eventAllowedDepts: ArrayList<String>?,
    @ColumnInfo(name="event_allowed_years") var eventAllowedYears: ArrayList<Int>?,
    @ColumnInfo(name="event_status") var eventStatus: String?,
    @ColumnInfo(name="last_refresh") var lastRefreshed: Date?
)