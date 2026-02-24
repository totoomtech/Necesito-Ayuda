package com.totoom.necesitoayuda.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Query("SELECT * FROM contacts ORDER BY orderIndex ASC LIMIT 3")
    fun getTopContacts(): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Query("SELECT * FROM app_settings WHERE id = 0")
    fun getSettingsFlow(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 0")
    suspend fun getSettings(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: AppSettings)

    @Query("UPDATE app_settings SET doctorPhone = :doctorPhone WHERE id = 0")
    suspend fun updateDoctorPhone(doctorPhone: String)

    @Query("UPDATE app_settings SET hasCompletedPermissions = :completed WHERE id = 0")
    suspend fun updatePermissionsCompleted(completed: Boolean)
}
