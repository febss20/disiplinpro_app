package com.dsp.disiplinpro.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsp.disiplinpro.data.model.User
import com.dsp.disiplinpro.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = FirestoreRepository()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    init {
        fetchUser()
    }

    private fun fetchUser() {
        viewModelScope.launch {
            _user.value = repository.getUser()
        }
    }
}