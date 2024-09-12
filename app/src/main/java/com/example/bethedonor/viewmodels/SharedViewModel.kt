package com.example.bethedonor.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedViewModel : ViewModel() {
    private val _changeInServer = MutableStateFlow(false)

    fun setChangeInServer(value: Boolean) {
        _changeInServer.value = value
    }

    fun checkChangeInServer(): Boolean {
        return _changeInServer.value
    }
}