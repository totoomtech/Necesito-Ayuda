package com.totoom.necesitoayuda.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private var db: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        if (db == null) {
            db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "necesito_ayuda_db"
            )
            .fallbackToDestructiveMigration()
            .build()
        }
        return db!!
    }
}
