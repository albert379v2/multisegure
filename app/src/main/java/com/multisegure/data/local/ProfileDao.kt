package com.multisegure.data.local

import androidx.room.*
import com.multisegure.data.model.BrowserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY createdAt DESC")
    fun getAllProfiles(): Flow<List<BrowserProfile>>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileById(id: Int): BrowserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: BrowserProfile): Long

    @Update
    suspend fun updateProfile(profile: BrowserProfile)

    @Delete
    suspend fun deleteProfile(profile: BrowserProfile)
}
