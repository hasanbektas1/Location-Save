package com.hasanbektas.locationsave

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = arrayOf(Location::class),version = 1)
abstract class LocationDatabase: RoomDatabase() {
    abstract fun locationDao():LocationDao
}