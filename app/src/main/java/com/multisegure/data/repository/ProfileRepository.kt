package com.multisegure.data.repository

import com.multisegure.data.local.ProfileDao
import com.multisegure.data.model.BrowserProfile
import kotlinx.coroutines.flow.Flow

class ProfileRepository(private val profileDao: ProfileDao) {
    val allProfiles: Flow<List<BrowserProfile>> = profileDao.getAllProfiles()

    suspend fun getProfileById(id: Int): BrowserProfile? = profileDao.getProfileById(id)
    suspend fun insertProfile(profile: BrowserProfile): Long = profileDao.insertProfile(profile)
    suspend fun updateProfile(profile: BrowserProfile) = profileDao.updateProfile(profile)
    suspend fun deleteProfile(profile: BrowserProfile) = profileDao.deleteProfile(profile)
}
