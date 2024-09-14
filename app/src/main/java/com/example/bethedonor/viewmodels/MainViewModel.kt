package com.example.bethedonor.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bethedonor.utils.NetworkConnectivityMonitor

// MainViewModel that holds other ViewModels
class MainViewModel(application: Application,private val networkMonitor: NetworkConnectivityMonitor) : AndroidViewModel(application) {
    // Initialize ViewModels with Application context
    val sharedViewModel: SharedViewModel by lazy { SharedViewModel() }
    val homeViewModel: HomeViewModel by lazy { HomeViewModel(application,networkMonitor) }
    val profileViewModel: ProfileViewModel by lazy { ProfileViewModel(application,networkMonitor) }
    val allRequestViewModel: AllRequestViewModel by lazy { AllRequestViewModel(application,networkMonitor) }
    val createRequestViewModel: CreateRequestViewModel by lazy { CreateRequestViewModel(application,networkMonitor) }
    val historyViewModel: HistoryViewModel by lazy { HistoryViewModel(application,networkMonitor) }
    val editEmailViewModel: EditEmailViewModel by lazy { EditEmailViewModel(application,networkMonitor) }


    override fun onCleared() {
        super.onCleared()
        // Unregister network callback when ViewModel is cleared
        networkMonitor.unregisterCallback()
    }
}

// Factory for creating MainViewModel with Application context
class MainViewModelFactory(
    private val application: Application,
    private val networkMonitor: NetworkConnectivityMonitor
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(application, networkMonitor = networkMonitor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

