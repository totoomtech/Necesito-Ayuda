package com.totoom.necesitoayuda.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Contact::class, AppSettings::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
