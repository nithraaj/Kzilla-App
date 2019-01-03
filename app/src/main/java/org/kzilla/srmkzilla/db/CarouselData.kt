package org.kzilla.srmkzilla.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "carousel")
data class CarouselData (
    @PrimaryKey
    @ColumnInfo(name = "carousel_id")
    var carouselId: String,
    @ColumnInfo(name="url_land") var urlLand: String?,
    @ColumnInfo(name="url_port") var urlPort: String?,
    @ColumnInfo(name="last_refresh") var lastRefreshed: Date?
)