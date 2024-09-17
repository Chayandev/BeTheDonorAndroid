package com.example.bethedonor.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bethedonor.data.api.RetrofitClient
import com.example.bethedonor.data.dataModels.BackendResponse
import com.example.bethedonor.data.dataModels.NewBloodRequest
import com.example.bethedonor.data.preferences.PreferencesManager
import com.example.bethedonor.data.repository.UserRepositoryImp
import com.example.bethedonor.domain.usecase.CreateRequestUseCase
import com.example.bethedonor.ui.utils.uievent.RegistrationUIEvent
import com.example.bethedonor.ui.utils.uistate.RegistrationUiState
import com.example.bethedonor.utils.NetworkConnectivityMonitor
import com.example.bethedonor.utils.Validator
import com.example.bethedonor.utils.toDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateRequestViewModel(application: Application) : AndroidViewModel(application) {

    //***** Initialize Preferences Manager for accessing DataStore *****//
    private val preferencesManager = PreferencesManager(getApplication())

    // Helper function to get the auth token from DataStore
    private fun getAuthToken(): String? = preferencesManager.jwtToken

    // UI State to hold the data of the new request
    var newRequestUiState = mutableStateOf(RegistrationUiState())

    // UI state for selected fields in the bottom sheet
    // MutableStateFlow for UI state
    private val _selectedState = MutableStateFlow("")
    val selectedState: StateFlow<String> = _selectedState

    private val _selectedDistrict = MutableStateFlow("")
    val selectedDistrict: StateFlow<String>  = _selectedDistrict

    private val _selectedCity = MutableStateFlow("")
    val selectedCity: StateFlow<String> = _selectedCity

    private val _selectedPinCode = MutableStateFlow("")
    val selectedPinCode: StateFlow<String> = _selectedPinCode

    private val _requestInProgress = MutableStateFlow(false)
    val requestInProgress: StateFlow<Boolean>get() = _requestInProgress

    //***** Event Handler for UI Interaction *****//
    fun onEvent(event: RegistrationUIEvent) {
        newRequestUiState.value = when (event) {
            is RegistrationUIEvent.StateValueChangeEvent -> newRequestUiState.value.copy(
                state = event.state,
                stateErrorState = Validator.validateString(event.state)
            )
            is RegistrationUIEvent.DistrictValueChangeEvent -> newRequestUiState.value.copy(
                district = event.district,
                districtErrorState = Validator.validateString(event.district)
            )
            is RegistrationUIEvent.CityValueChangeEvent -> newRequestUiState.value.copy(
                city = event.city,
                cityErrorState = Validator.validateString(event.city)
            )
            is RegistrationUIEvent.PinCodeValueChangeEvent -> newRequestUiState.value.copy(
                pinCode = event.pinCode,
                pinCodeErrorState = Validator.validatePinCode(event.pinCode)
            )
            is RegistrationUIEvent.BloodGroupValueChangeEvent -> newRequestUiState.value.copy(
                bloodGroup = event.bloodGroup,
                bloodGroupErrorState = Validator.validateString(event.bloodGroup)
            )
            is RegistrationUIEvent.BloodUnitValueChangeEvent -> newRequestUiState.value.copy(
                bloodUnit = event.unit,
                bloodUnitErrorState = Validator.validateBloodUnit(event.unit)
            )
            is RegistrationUIEvent.DonationCenterValueChangeEvent -> newRequestUiState.value.copy(
                donationCenter = event.center,
                donationCenterErrorState = Validator.validateString(event.center)
            )
            is RegistrationUIEvent.DateValueChangeEvent -> newRequestUiState.value.copy(
                date = event.date,
                deadLineErrorState = Validator.validateString(event.date)
            )
            else -> newRequestUiState.value // Handle other events if needed
        }
    }

    //***** Validation Logic for New Blood Request *****//
    fun validateWithRulesForNewRequest(): Boolean {
        return listOf(
            newRequestUiState.value.donationCenterErrorState.status,
            newRequestUiState.value.stateErrorState.status,
            newRequestUiState.value.districtErrorState.status,
            newRequestUiState.value.cityErrorState.status,
            newRequestUiState.value.pinCodeErrorState.status,
            newRequestUiState.value.bloodGroupErrorState.status,
            newRequestUiState.value.bloodUnitErrorState.status,
            newRequestUiState.value.deadLineErrorState.status
        ).all { it } // All validation statuses must be true
    }

    //***** Network Call to Create a New Blood Request *****//
    private val apiService = RetrofitClient.instance
    private val userRepository = UserRepositoryImp(apiService)
    private val createNewBloodRequestUseCase = CreateRequestUseCase(userRepository)

    fun createNewBloodRequest(onCreated: (BackendResponse) -> Unit) {
        if (validateWithRulesForNewRequest()) {
            _requestInProgress.value = true
            val bloodRequest = buildNewBloodRequest() // Build request from UI state

            viewModelScope.launch {
                try {
                    val response = createNewBloodRequestUseCase.execute(getAuthToken().toString(), bloodRequest)
                    Log.d("Response", response.toString())
                    onCreated(response)
                } catch (e: Exception) {
                    Log.e("Error", e.message ?: "Unknown error")
                    onCreated(BackendResponse(message = "Exception: ${e.message}"))
                } finally {
                    clearUiState() // Clear the form after submission
                    _requestInProgress.value = false
                }
            }
        } else {
            Log.e("Validation", "Request validation failed")
        }
    }

    // Helper to build NewBloodRequest from UI state
    private fun buildNewBloodRequest(): NewBloodRequest {
        return NewBloodRequest(
            donationCenter = newRequestUiState.value.donationCenter,
            state = newRequestUiState.value.state,
            district = newRequestUiState.value.district,
            city = newRequestUiState.value.city,
            pin = newRequestUiState.value.pinCode,
            bloodGroup = newRequestUiState.value.bloodGroup,
            bloodUnit = newRequestUiState.value.bloodUnit,
            deadline = newRequestUiState.value.date.toDate()!!
        )
    }

    //***** Clear UI State After Request Submission *****//
    private fun clearUiState() {
        newRequestUiState.value = RegistrationUiState()
        _selectedState.value = ""
        _selectedDistrict.value=""
        _selectedCity.value = ""
        _selectedPinCode.value = ""
    }

    //***** Helper Methods for Selection Logic *****//
    fun selectState(state: String) {
        _selectedState.value = state
        Log.d("selection",state)
        clearSelectionsFor("state") // Clear dependent selections
    }

    fun selectDistrict(district: String) {
        _selectedDistrict.value = district
        Log.d("selection",district)
        clearSelectionsFor("district")
    }
    fun selectPin(pinCode: String) {
        _selectedPinCode.value = pinCode
        Log.d("selection",pinCode)
        clearSelectionsFor("pin")
    }
    fun selectCity(city: String) {
        _selectedCity.value = city
    }


    // Clear dependent selections based on the selected field
    private fun clearSelectionsFor(level: String) {
        Log.d("clearSelection",level)
        when (level) {
            "state" -> {
                Log.d("clearSelection",level)
                _selectedDistrict.value=""
                _selectedCity.value = ""
                _selectedPinCode.value = ""
            }
            "district" -> {
                Log.d("clearSelection",level)
                _selectedCity.value = ""
                _selectedPinCode.value = ""
            }
            "pin"->{
                Log.d("clearSelection",level)
                _selectedCity.value = ""
            }
        }
    }
}
