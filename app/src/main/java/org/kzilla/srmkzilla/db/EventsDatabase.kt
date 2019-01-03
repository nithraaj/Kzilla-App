package org.kzilla.srmkzilla.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [UpcomingEvent::class, RegisteredEvent::class, CarouselData::class], version = 1)
@TypeConverters(Converters::class)
abstract class EventsDatabase : RoomDatabase() {
    abstract fun upcomingEventsDao(): UpcomingEventDao
    abstract fun registeredEventsDao(): RegisteredEventDao
    abstract fun carouselDao(): CarouselDao
}