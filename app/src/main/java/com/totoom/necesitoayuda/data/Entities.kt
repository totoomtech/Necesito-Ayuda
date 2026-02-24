package com.totoom.necesitoayuda.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val photoPath: String? = null,
    val orderIndex: Int
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 0,
    val language: String = "es",
    val voiceAnnouncementsEnabled: Boolean = true,
    val doctorPhone: String = "",
    val hasCompletedPermissions: Boolean = false
)
