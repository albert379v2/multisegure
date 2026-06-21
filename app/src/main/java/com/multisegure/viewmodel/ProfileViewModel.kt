package com.multisegure.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.multisegure.data.model.BrowserProfile
import com.multisegure.data.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    val allProfiles: Flow<List<BrowserProfile>> = repository.allProfiles

    private val _currentProfile = MutableStateFlow<BrowserProfile?>(null)
    val currentProfile: StateFlow<BrowserProfile?> = _currentProfile

    fun loadProfile(id: Int) {
        viewModelScope.launch {
            _currentProfile.value = repository.getProfileById(id)
        }
    }

    fun saveProfile(profile: BrowserProfile, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (profile.id == 0) {
                repository.insertProfile(profile)
            } else {
                repository.updateProfile(profile)
            }
            onComplete()
        }
    }

    fun deleteProfile(profile: BrowserProfile, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            onComplete()
        }
    }

    class Factory(private val repository: ProfileRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(repository) as T
        }
    }
}
