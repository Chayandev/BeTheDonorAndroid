package com.example.bethedonor.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bethedonor.data.api.RetrofitClient
import com.example.bethedonor.data.dataModels.AccountResponse
import com.example.bethedonor.data.dataModels.ProfileResponse
import com.example.bethedonor.data.preferences.PreferencesManager
import com.example.bethedonor.data.repository.UserRepositoryImp
import com.example.bethedonor.data.dataModels.UserUpdate
import com.example.bethedonor.domain.usecase.CloseAccountUseCase
import com.example.bethedonor.domain.usecase.GetUserProfileUseCase
import com.example.bethedonor.domain.usecase.UpdateProfileUseCase
import com.example.bethedonor.ui.utils.uievent.RegistrationUIEvent
import com.example.bethedonor.ui.utils.uistate.RegistrationUiState
import com.example.bethedonor.utils.NetworkConnectivityMonitor
import com.example.bethedonor.utils.Validator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application, ) : AndroidViewModel(application) {


    // ***** access the datastore ***** //
    private val preferencesManager = PreferencesManager(getApplication())
    private fun getAuthToken():String?{
        return preferencesManager.jwtToken
    }
    //*************************

    private val hasFetchedProfile = mutableStateOf(false)
    fun setFetchedProfile(value: Boolean) {
        hasFetchedProfile.value = value
    }
    fun getFetchedProfile(): Boolean {
        return hasFetchedProfile.value
    }
    var updateProfileUiState = mutableStateOf(RegistrationUiState())

    //***update-profile-bottom-sheet ***//
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

            is RegistrationUIEvent.PasswordValueChangeEvent -> {}
            is RegistrationUIEvent.ConfirmPasswordValueChangeEvent -> {}
            is RegistrationUIEvent.DateValueChangeEvent -> {}
            is RegistrationUIEvent.BloodGroupValueChangeEvent -> {}
            is RegistrationUIEvent.NameValueChangeEvent -> {}
            is RegistrationUIEvent.EmailValueChangeEvent -> {}
            RegistrationUIEvent.RegistrationButtonClick -> {
                //  printState()
            }

            is RegistrationUIEvent.BloodUnitValueChangeEvent -> {}
            is RegistrationUIEvent.DonationCenterValueChangeEvent -> {}
        }
    }

    fun validateWithRulesForUpdate(): Boolean {
        //  printState()
        Log.d("updateProfileUiState", updateProfileUiState.value.genderErrorState.toString())
        Log.d("updateProfileUiState", updateProfileUiState.value.stateErrorState.toString())
        Log.d("updateProfileUiState", updateProfileUiState.value.districtErrorState.toString())
        Log.d("updateProfileUiState", updateProfileUiState.value.cityErrorState.toString())
        Log.d("updateProfileUiState", updateProfileUiState.value.pinCodeErrorState.toString())
        //  if (availableToDonate.value)
        return updateProfileUiState.value.genderErrorState.status
                && updateProfileUiState.value.stateErrorState.status
                && updateProfileUiState.value.districtErrorState.status
                && updateProfileUiState.value.cityErrorState.status
                && updateProfileUiState.value.pinCodeErrorState.status
    }

    private val _selectedState = MutableStateFlow("")
    val selectedState: StateFlow<String> = _selectedState

    private val _selectedDistrict = MutableStateFlow("")
    val selectedDistrict: StateFlow<String>  = _selectedDistrict

    private val _selectedCity = MutableStateFlow("")
    val selectedCity: StateFlow<String> = _selectedCity

    private val _selectedPinCode = MutableStateFlow("")
    val selectedPinCode: StateFlow<String> = _selectedPinCode

    private val _availableToDonate = MutableStateFlow(false)
    val availableToDonate: StateFlow<Boolean> = _availableToDonate

    fun setAllProfileDetails(state:String,district:String,city:String,pin:String,available:Boolean){
        _selectedState.value=state
        _selectedDistrict.value=district
        _selectedCity.value=city
        _selectedPinCode.value=pin
        _availableToDonate.value=available
    }

    fun selectState(state: String) {
        _selectedState.value = state
        clearSelectionsFor("state")
    }

    fun selectDistrict(district: String) {
        _selectedDistrict.value = district
       clearSelectionsFor("district")
    }

    fun selectPin(pinCode: String) {
        _selectedPinCode.value = pinCode
        clearSelectionsFor("pin")
    }

    fun selectCity(city: String) {
        _selectedCity.value = city
    }

    fun setAvailableToDonate(value: Boolean) {
        _availableToDonate.value = value
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
    // Edit Email address here and OTP validation **********

    //****************************************************


    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing.asStateFlow()

    fun setRefresherStatusTrue() {
        _isRefreshing.value = true
    }


    //*** api-responses ***//
    private val _profileResponse = MutableStateFlow<Result<ProfileResponse>?>(null)
    val profileResponse: StateFlow<Result<ProfileResponse>?> = _profileResponse

    private val _retryFlag = MutableStateFlow(false)
    val retryFlag: StateFlow<Boolean> get() = _retryFlag
    fun setRetryFlag(value: Boolean) {
        _retryFlag.value = value
    }


    private val _deleteAccountResponse = MutableStateFlow<Result<AccountResponse>?>(null)
    val deleteAccountResponse: StateFlow<Result<AccountResponse>?> = _deleteAccountResponse

    //*** api_service  use case ***
    private val apiService = RetrofitClient.instance
    private val userRepository = UserRepositoryImp(apiService)
    private val getUserProfileUserUseCase = GetUserProfileUseCase(userRepository)
    private val closeAccountUseCase = CloseAccountUseCase(userRepository)
    private val updateProfileUseCase = UpdateProfileUseCase(userRepository)
    var requestInProgress = mutableStateOf(false)
    var deletingAccountProgress= mutableStateOf(false)
    var updatingProfileInProgress = mutableStateOf(false)

    fun getProfile(onProfileFetched: () -> Unit) {
        requestInProgress.value = true
        viewModelScope.launch {
            try {
                Log.d("token", getAuthToken().toString())
                val response = getUserProfileUserUseCase.execute(getAuthToken().toString())
                val result = Result.success(response)
                _profileResponse.value = result
                Log.d("Response", response.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                val result = Result.failure<ProfileResponse>(e)
                _profileResponse.value = result
            } finally {
                requestInProgress.value = false
                _isRefreshing.value = false
                onProfileFetched()
            }
        }
    }

    fun updateProfile(onUpdate: (Pair<String, String>) -> Unit) {
        val updates = UserUpdate(
            // phoneNumber = updateProfileUiState.value.phoneNo,
            gender = updateProfileUiState.value.gender,
            state = updateProfileUiState.value.state,
            city = updateProfileUiState.value.city,
            district = updateProfileUiState.value.district,
            pin = updateProfileUiState.value.pinCode,
            available = updateProfileUiState.value.checkedAvailabilityStatus
        )
       updatingProfileInProgress.value=true
        viewModelScope.launch {
            try {
                val response = updateProfileUseCase.execute("0", getAuthToken().toString(), updates)
                val result = Result.success(response)
                //  _profileResponse.value = result
                Log.d("Response", response.toString())
                onUpdate(Pair("success", result.getOrNull()?.message.toString()))
            } catch (e: Exception) {
                val result = Result.failure<String>(e)
                Log.d("Error", e.message.toString())
                onUpdate(Pair("failure", result.exceptionOrNull()?.message.toString()))
            } finally {
                updatingProfileInProgress.value=false
            }
        }
    }


    fun deleteAccount(onDeletePerformed: (Result<AccountResponse>) -> Unit) {
        deletingAccountProgress.value=true
        viewModelScope.launch {
            try {
                val response = closeAccountUseCase.execute(getAuthToken().toString())
                _deleteAccountResponse.value = Result.success(response)
                Log.d("Response", response.toString())
                onDeletePerformed(Result.success(response))
            } catch (e: Exception) {
                _deleteAccountResponse.value = Result.failure(e)
                Log.d("Error", e.message.toString())
                onDeletePerformed(Result.failure(e))
            } finally {
                deletingAccountProgress.value=false
            }
        }
    }


    suspend fun logoutUser(onLogout: () -> Unit) {
        preferencesManager.clearUserData()
        // Confirm the data has been cleared
        if (preferencesManager.jwtToken.isNullOrEmpty()) {
            onLogout()
        } else {
            Log.e("Logout Error", "Failed to clear user data")
        }
    }

}
