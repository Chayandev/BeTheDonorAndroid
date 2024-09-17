package com.example.bethedonor.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bethedonor.utils.NetworkConnectivityMonitor

// MainViewModel that holds other ViewModels
class MainViewModel(application: Application) : AndroidViewModel(application) {
    // Initialize ViewModels with Application context
    val sharedViewModel: SharedViewModel by lazy { SharedViewModel() }
    val homeViewModel: HomeViewModel by lazy { HomeViewModel(application) }
    val profileViewModel: ProfileViewModel by lazy { ProfileViewModel(application) }
    val allRequestViewModel: AllRequestViewModel by lazy { AllRequestViewModel(application) }
    val createRequestViewModel: CreateRequestViewModel by lazy { CreateRequestViewModel(application) }
    val historyViewModel: HistoryViewModel by lazy { HistoryViewModel(application) }
    val editEmailViewModel: EditEmailViewModel by lazy { EditEmailViewModel(application) }

    fun resettingViewModelState(){
        profileViewModel.resetUiStates()
        historyViewModel.resetUiStates()
        allRequestViewModel.resetUiState()
    }

}

// Factory for creating MainViewModel with Application context
class MainViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

