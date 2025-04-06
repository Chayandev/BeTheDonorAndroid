package com.example.bethedonor.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bethedonor.data.api.RetrofitClient
import com.example.bethedonor.data.dataModels.AccountResponse
import com.example.bethedonor.data.dataModels.ProfileResponse
import com.example.bethedonor.data.dataModels.UserProfile
import com.example.bethedonor.data.preferences.PreferencesManager
import com.example.bethedonor.data.repository.UserRepositoryImp
import com.example.bethedonor.data.dataModels.UserUpdate
import com.example.bethedonor.domain.usecase.CloseAccountUseCase
import com.example.bethedonor.domain.usecase.GetUserProfileUseCase
import com.example.bethedonor.domain.usecase.UpdateProfileUseCase
import com.example.bethedonor.ui.utils.uievent.RegistrationUIEvent
import com.example.bethedonor.ui.utils.uistate.RegistrationUiState
import com.example.bethedonor.utils.Validator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds all profile-related states for better organization and management.
 */
data class ProfileUiState(
    val selectedState: String = "",
    val selectedDistrict: String = "",
    val selectedCity: String = "",
    val selectedPinCode: String = "",
    val availableToDonate: Boolean = false,
    val hasFetchedProfile: Boolean = false,
    val requestInProgress: Boolean = false,
    val isRefreshing: Boolean = false,
    val deletingAccountProgress: Boolean = false,
    val updatingProfileInProgress: Boolean = false,
    val profileResponse: Result<ProfileResponse>? = null,
    val deleteAccountResponse: Result<AccountResponse>? = null,
    val retryFlag: Boolean = false
)


class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // Preferences manager for JWT token retrieval
    private val preferencesManager = PreferencesManager(getApplication())


    // MutableStateFlow to manage UI state with initial state as AllRequestUiState
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> get() = _uiState
    val updateProfileUiState = mutableStateOf(RegistrationUiState())

    // API services
    private val apiService = RetrofitClient.instance
    private val userRepository = UserRepositoryImp(apiService)

    //use cases
    private val getUserProfileUserUseCase = GetUserProfileUseCase(userRepository)
    private val closeAccountUseCase = CloseAccountUseCase(userRepository)
    private val updateProfileUseCase = UpdateProfileUseCase(userRepository)


    /**
     * Retrieves the JWT token from the PreferencesManager.
     * @return the JWT token as a String.
     */
    private fun getAuthToken(): String? = preferencesManager.jwtToken

    /**
     * Updates the refresh status state with value true
     */
    fun setRefresherStatusTrue() {
        _uiState.update { it.copy(isRefreshing = true) }
    }

    /**
     * Handles UI events for profile management.
     * @param event the event triggered by the UI.
     */
    fun onEvent(event: RegistrationUIEvent) {
        when (event) {

            is RegistrationUIEvent.PhoneNoChangeEvent -> {
                updateProfileUiState.value = updateProfileUiState.value.copy(
                    phoneNo = event.phoneNo,
                    phoneNoErrorState = Validator.validatePhoneNo(event.phoneNo)
                )
            }

            is RegistrationUIEvent.DistrictValueChangeEvent -> {
                updateProfileUiState.value = updateProfileUiState.value.copy(
                    district = event.district,
                    districtErrorState = Validator.validateString(event.district)
                )
            }

            is RegistrationUIEvent.CityValueChangeEvent -> {
                updateProfileUiState.value = updateProfileUiState.value.copy(
                    city = event.city,
                    cityErrorState = Validator.validateString(event.city)
                )
            }

            is RegistrationUIEvent.GenderValueChangeEvent -> {
                updateProfileUiState.value = updateProfileUiState.value.copy(
                    gender = event.gender,
                    genderErrorState = Validator.validateString(event.gender)
                )
            }

            is RegistrationUIEvent.PinCodeValueChangeEvent -> {
                updateProfileUiState.value = updateProfileUiState.value.copy(
                    pinCode = event.pinCode,
                    pinCodeErrorState = Validator.validatePinCode(event.pinCode)
                )
            }

            is RegistrationUIEvent.StateValueChangeEvent -> {
                updateProfileUiState.value = updateProfileUiState.value.copy(
                    state = event.state,
                    stateErrorState = Validator.validateString(event.state)
                )
            }

            is RegistrationUIEvent.AvailabilityCheckerValueChangeEvent -> {
                updateProfileUiState.value = updateProfileUiState.value.copy(
                    checkedAvailabilityStatus = event.status
                )
            }

            else -> {
                updateProfileUiState.value
            }
            // Handle other events...
        }
    }


    /**
     * Validates the fields before updating the profile.
     * @return true if all fields are valid, false otherwise.
     */
    fun validateWithRulesForUpdate(): Boolean {
        //  printState()
        Log.d(
            "updateProfileUiState",
            updateProfileUiState.value.genderErrorState.toString()
        )
        Log.d(
            "updateProfileUiState",
            updateProfileUiState.value.stateErrorState.toString()
        )
        Log.d(
            "updateProfileUiState",
            updateProfileUiState.value.districtErrorState.toString()
        )
        Log.d("updateProfileUiState", updateProfileUiState.value.cityErrorState.toString())
        Log.d(
            "updateProfileUiState",
            updateProfileUiState.value.pinCodeErrorState.toString()
        )
        //  if (availableToDonate.value)
        return updateProfileUiState.value.run {
            genderErrorState.status && stateErrorState.status && districtErrorState.status &&
                    cityErrorState.status && pinCodeErrorState.status
        }
    }


    /**
     * Sets all selected profile details.
     * @param profileData .
     */
    private fun setAllProfileDetails(profileData: UserProfile?) {
        _uiState.value = _uiState.value.copy(
            selectedState = profileData?.state.toString(),
            selectedDistrict = profileData?.district.toString(),
            selectedCity = profileData?.city.toString(),
            selectedPinCode =profileData?.pin.toString(),
            availableToDonate = profileData?.available?:false
        )
        onEvent(
            RegistrationUIEvent.GenderValueChangeEvent(
               gender = profileData?.gender.toString()
            )
        )
        onEvent(
            RegistrationUIEvent.StateValueChangeEvent(
              state=profileData?.state.toString()
            )
        )
        onEvent(
            RegistrationUIEvent.DistrictValueChangeEvent(
                district=profileData?.district.toString()
            )
        )
        onEvent(
            RegistrationUIEvent.CityValueChangeEvent(
                city=profileData?.city.toString()
            )
        )
        onEvent(
            RegistrationUIEvent.PinCodeValueChangeEvent(
                profileData?.pin.toString()
            )
        )
        onEvent(
            RegistrationUIEvent.AvailabilityCheckerValueChangeEvent(
                status =profileData?.available?:false
            )
        )
    }

    //***** Helper Methods for Selection Logic *****//
    fun selectState(state: String) {
        _uiState.update { it.copy(selectedState = state) }
        clearDependentSelections("state")
    }

    fun selectDistrict(district: String) {
        _uiState.update { it.copy(selectedDistrict = district) }
        clearDependentSelections("district")
    }

    fun selectCity(city: String) {
        _uiState.update { it.copy(selectedCity = city) }
    }

    fun selectPin(pinCode: String) {
        _uiState.update { it.copy(selectedPinCode = pinCode) }
        clearDependentSelections("pin")
    }

    fun setAvailableToDonate(value: Boolean) {
        _uiState.update { it.copy(availableToDonate = value) }
    }

    //***********************************//


    //***** Clear Dependent Selections *****//
    private fun clearDependentSelections(level: String) {
        Log.d("level", level)
        when (level) {
            "state" -> {
                _uiState.update {
                    it.copy(
                        selectedDistrict = "",
                        selectedCity = "",
                        selectedPinCode = ""
                    )
                }
                onEvent(
                    RegistrationUIEvent.DistrictValueChangeEvent(
                        _uiState.value.selectedDistrict
                    )
                )
                onEvent(
                    RegistrationUIEvent.CityValueChangeEvent(
                        _uiState.value.selectedCity
                    )
                )
                onEvent(
                    RegistrationUIEvent.PinCodeValueChangeEvent(
                        _uiState.value.selectedPinCode
                    )
                )
            }

            "district" -> {
                _uiState.update {
                    it.copy(
                        selectedCity = "",
                        selectedPinCode = ""
                    )
                }
                onEvent(
                    RegistrationUIEvent.CityValueChangeEvent(
                        _uiState.value.selectedCity
                    )
                )
                onEvent(
                    RegistrationUIEvent.PinCodeValueChangeEvent(
                        _uiState.value.selectedPinCode
                    )
                )
            }

            "pin" -> {
                _uiState.update { it.copy(selectedCity = "") }
                onEvent(
                    RegistrationUIEvent.CityValueChangeEvent(
                        _uiState.value.selectedCity
                    )
                )
            }
        }
    }

    /**
     * Retrieves the user's profile information.
     * @param onProfileFetched callback to be executed after fetching profile.
     */
    fun getProfile(onProfileFetched: () -> Unit) {
        _uiState.value = _uiState.value.copy(requestInProgress = true)
        viewModelScope.launch {
            try {
                val response = getUserProfileUserUseCase.execute(getAuthToken().toString())
                if (response.myProfile != null) {
                    _uiState.value = _uiState.value.copy(
                        profileResponse = Result.success(response),
                        retryFlag = false
                    )
                    setAllProfileDetails(response.myProfile)
                } else {
                    _uiState.value = _uiState.value.copy(
                        profileResponse = Result.failure(Exception(response.message)),
                        retryFlag = true
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    profileResponse = Result.failure(e),
                    retryFlag = true
                )
            } finally {
                _uiState.value = _uiState.value.copy(
                    requestInProgress = false,
                    isRefreshing = false,
                    hasFetchedProfile = true
                )
                onProfileFetched()
            }
        }
    }

    /**
     * Updates the user's profile information.
     * @param onUpdate callback to be executed after updating profile.
     */
    fun updateProfile(onUpdate: (Pair<String, String>) -> Unit) {
        val updates = UserUpdate(
            gender = updateProfileUiState.value.gender,
            state = updateProfileUiState.value.state,
            city = updateProfileUiState.value.city,
            district = updateProfileUiState.value.district,
            pin = updateProfileUiState.value.pinCode,
            available =updateProfileUiState.value.checkedAvailabilityStatus
        )
        _uiState.value = _uiState.value.copy(updatingProfileInProgress = true)
        viewModelScope.launch {
            try {
                val response = updateProfileUseCase.execute("0", getAuthToken().toString(), updates)
                onUpdate(Pair("success", response.message.toString()))
            } catch (e: Exception) {
                onUpdate(Pair("failure", e.message.toString()))
            } finally {
                _uiState.value = _uiState.value.copy(updatingProfileInProgress = false)
            }
        }
    }

    /**
     * Deletes the user's account.
     * @param onDeletePerformed callback to handle the result of the delete operation.
     */
    fun deleteAccount(onDeletePerformed: (Result<AccountResponse>) -> Unit) {
        _uiState.value = _uiState.value.copy(deletingAccountProgress = true)
        viewModelScope.launch {
            try {
                val response = closeAccountUseCase.execute(getAuthToken().toString())
                onDeletePerformed(Result.success(response))
            } catch (e: Exception) {
                onDeletePerformed(Result.failure(e))
            } finally {
                _uiState.value = _uiState.value.copy(deletingAccountProgress = false)
            }
        }
    }


    /**
     * Logs out the user and clears the stored data.
     * @param onLogout callback to be executed after logout.
     */
    suspend fun logoutUser(onLogout: () -> Unit) {
        preferencesManager.clearUserData()
        if (preferencesManager.jwtToken.isNullOrEmpty()) {
            onLogout()
        } else {
            Log.e("Logout Error", "Failed to clear user data")
        }
    }


    /**
     * Resets the UI states to their default values.
     */
    fun resetUiStates() {
        _uiState.value = ProfileUiState() // Reset to default state
    }
}
