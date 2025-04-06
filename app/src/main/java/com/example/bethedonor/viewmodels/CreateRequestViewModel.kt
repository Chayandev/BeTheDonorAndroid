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
import com.example.bethedonor.utils.Validator
import com.example.bethedonor.utils.toDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


//***** Combined Registration UI State *****//
data class NewRequestUiState(
    val state: String = "",
    val district: String = "",
    val city: String = "",
    val pinCode: String = "",
    val bloodGroup: String = "",
    val bloodUnit: String = "",
    val donationCenter: String = "",
    val date: String = "",
    val requestInProgress: Boolean = false
)

class CreateRequestViewModel(application: Application) : AndroidViewModel(application) {

    //***** Initialize Preferences Manager for accessing DataStore *****//
    private val preferencesManager = PreferencesManager(getApplication())

    /**
     * Retrieves the JWT token from the PreferencesManager.
     * @return the JWT token as a String.
     */
    private fun getAuthToken(): String? = preferencesManager.jwtToken

    // MutableStateFlow to manage UI state with initial state as AllRequestUiState
    private val _uiState = MutableStateFlow(NewRequestUiState())
    val uiState: StateFlow<NewRequestUiState> get() = _uiState

    // UI State to hold the data of the new request
    var newRequestUiState = mutableStateOf(RegistrationUiState())

    //***** API Service & Repository Initialization *****//
    private val apiService = RetrofitClient.instance
    private val userRepository = UserRepositoryImp(apiService)
    private val createNewBloodRequestUseCase = CreateRequestUseCase(userRepository)

    //***** Event Handler for UI Interaction on inputs *****//
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
    fun createNewBloodRequest(onCreated: (BackendResponse) -> Unit) {
        if (validateWithRulesForNewRequest()) {
            _uiState.update { it.copy(requestInProgress = true) }
            val bloodRequest = buildNewBloodRequest() // Build request from UI state
            viewModelScope.launch {
                try {
                    val response = createNewBloodRequestUseCase.execute(
                        getAuthToken().toString(),
                        bloodRequest
                    )
                    Log.d("Response", response.toString())
                    onCreated(response)
                } catch (e: Exception) {
                    Log.e("Error", e.message ?: "Unknown error")
                    onCreated(BackendResponse(message = "Exception: ${e.message}"))
                } finally {
                    _uiState.update { it.copy(requestInProgress = false) }
                }
            }
        } else {
            Log.e("Validation", "Request validation failed")
        }
    }

    //****** Helper to build NewBloodRequest from UI state ******
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
     fun resetUiState() {
        newRequestUiState.value = RegistrationUiState()
        _uiState.value = NewRequestUiState()
    }

    //***** Helper Methods for Selection Logic *****//
    fun selectState(state: String) {
        _uiState.update { it.copy(state = state) }
        clearDependentSelections("state")
    }

    fun selectDistrict(district: String) {
        _uiState.update { it.copy(district = district) }
        clearDependentSelections("district")
    }

    fun selectCity(city: String) {
        _uiState.update { it.copy(city = city) }
    }

    fun selectPin(pinCode: String) {
        _uiState.update { it.copy(pinCode = pinCode) }
        clearDependentSelections("pin")
    }

    //***** Clear Dependent Selections *****//
    private fun clearDependentSelections(level: String) {
        when (level) {
            "state" -> {
                _uiState.update {
                    it.copy(
                        district = "",
                        city = "",
                        pinCode = ""
                    )
                }

                onEvent(
                    RegistrationUIEvent.DistrictValueChangeEvent(
                        _uiState.value.district
                    )
                )
                onEvent(
                    RegistrationUIEvent.CityValueChangeEvent(
                        _uiState.value.city
                    )
                )
                onEvent(
                    RegistrationUIEvent.PinCodeValueChangeEvent(
                        _uiState.value.pinCode
                    )
                )
            }

            "district" -> {
                _uiState.update {
                    it.copy(
                        city = "",
                        pinCode = ""
                    )
                }
                onEvent(
                    RegistrationUIEvent.CityValueChangeEvent(
                        _uiState.value.city
                    )
                )
                onEvent(
                    RegistrationUIEvent.PinCodeValueChangeEvent(
                        _uiState.value.pinCode
                    )
                )
            }

            "pin" -> {
                _uiState.update { it.copy(city = "") }

                onEvent(
                    RegistrationUIEvent.CityValueChangeEvent(
                        _uiState.value.city
                    )
                )
            }
        }
    }
}
