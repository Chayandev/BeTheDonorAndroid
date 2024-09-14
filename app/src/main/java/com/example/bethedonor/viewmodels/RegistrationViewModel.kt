package com.example.bethedonor.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bethedonor.data.api.RetrofitClient
import com.example.bethedonor.data.dataModels.BackendResponse
import com.example.bethedonor.data.repository.UserRepositoryImp
import com.example.bethedonor.data.dataModels.User
import com.example.bethedonor.data.dataModels.UserBase
import com.example.bethedonor.domain.usecase.RegistrationUserUseCase
import com.example.bethedonor.ui.utils.uievent.RegistrationUIEvent
import com.example.bethedonor.ui.utils.uistate.RegistrationUiState
import com.example.bethedonor.utils.Validator
import com.example.bethedonor.utils.toDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class RegistrationViewModel() : ViewModel() {
    private val TAG = RegistrationViewModel::class.simpleName
    var registrationUIState = mutableStateOf(RegistrationUiState())

    //*** api_service and registration use case ***
    private val _registrationResponse = MutableLiveData<Result<BackendResponse>>()
    val registrationResponse: LiveData<Result<BackendResponse>> = _registrationResponse
    private val apiService = RetrofitClient.instance
    private val userRepository = UserRepositoryImp(apiService)
    private val registrationUserUseCase = RegistrationUserUseCase(userRepository)

    fun registerUser(onRegister: () -> Unit?) {
        _requestInProgress.value = true
        val dob = (registrationUIState.value.date.toDate())!!
        val userBase = UserBase(
            registrationUIState.value.name,
            registrationUIState.value.phoneNo,
            dob,
            registrationUIState.value.gender,
            registrationUIState.value.bloodGroup,
            registrationUIState.value.state,
            registrationUIState.value.city,
            registrationUIState.value.district,
            registrationUIState.value.pinCode,
            registrationUIState.value.password,
            registrationUIState.value.checkedAvailabilityStatus
        )
        val user = User(
            registrationUIState.value.emailId,
            userBase
        )
        viewModelScope.launch {
            try {
                //val response = userRepository.registerUser(user)

                val response = registrationUserUseCase.execute(user)
                _registrationResponse.value = Result.success(response)
                Log.d("Response", response.toString())
            } catch (e: Exception) {
                e.stackTrace
                _registrationResponse.value = Result.failure(e)
            } finally {
                _requestInProgress.value = false
                onRegister()
            }
        }
    }

    // Initialize selectedState
    // MutableStateFlow for UI state
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

    private val _requestInProgress = MutableStateFlow(false)
    val requestInProgress: StateFlow<Boolean> = _requestInProgress

    fun selectState(state: String) {
        _selectedState.value = state
        _selectedDistrict.value = ""
        _selectedCity.value = ""
        _selectedPinCode.value = ""
    }

    fun selectDistrict(district: String) {
        _selectedDistrict.value = district
        _selectedCity.value = ""
        _selectedPinCode.value = ""
    }

    fun selectPin(pinCode: String) {
        _selectedPinCode.value = pinCode
        _selectedCity.value=""
    }
    fun selectCity(city: String) {
        _selectedCity.value = city
    }

    fun setAvailableToDonate(value: Boolean) {
        _availableToDonate.value = value
    }

    fun onEvent(event: RegistrationUIEvent) {
        when (event) {
            is RegistrationUIEvent.NameValueChangeEvent -> {
                registrationUIState.value = registrationUIState.value.copy(
                    name = event.name,
                    nameErrorState = Validator.validateFirstName(event.name)
                )
            }

            is RegistrationUIEvent.EmailValueChangeEvent -> {
                registrationUIState.value = registrationUIState.value.copy(
                    emailId = event.emailId,
                    emailIdErrorState = Validator.validateEmailId(event.emailId)
                )
            }

            is RegistrationUIEvent.PhoneNoChangeEvent -> {
                registrationUIState.value = registrationUIState.value.copy(
                    phoneNo = event.phoneNo,
                    phoneNoErrorState = Validator.validatePhoneNo(event.phoneNo)
                )
            }

            is RegistrationUIEvent.PasswordValueChangeEvent -> {
                registrationUIState.value = registrationUIState.value.copy(
                    password = event.password,
                    passwordErrorState = Validator.validatePassword(event.password)
                )
            }

            is RegistrationUIEvent.ConfirmPasswordValueChangeEvent -> {
                registrationUIState.value = registrationUIState.value.copy(
                    confirmPassword = event.confirmPassword,
                    confirmPasswordState = Validator.validateConfirmPassword(
                        registrationUIState.value.password,
                        event.confirmPassword
                    )
                )
            }

            is RegistrationUIEvent.DateValueChangeEvent -> {
                registrationUIState.value = registrationUIState.value.copy(
                    date = event.date,
                    ageErrorState = Validator.validateAge(event.date)
                )
            }

            is RegistrationUIEvent.BloodGroupValueChangeEvent -> {
                registrationUIState.value = registrationUIState.value.copy(
                    bloodGroup = event.bloodGroup,
                    bloodGroupErrorState = Validator.validateString(event.bloodGroup)
                )
            }

            is RegistrationUIEvent.DistrictValueChangeEvent -> {
                registrationUIState.value = registrationUIState.value.copy(
                    district = event.district,
                    districtErrorState = Validator.validateString(event.district)
                )
            }

            is RegistrationUIEvent.CityValueChangeEvent -> {
                registrationUIState.value = registrationUIState.value.copy(
                    city = event.city,
                    cityErrorState = Validator.validateString(event.city)
                )
            }

            is RegistrationUIEvent.GenderValueChangeEvent -> {
                registrationUIState.value = registrationUIState.value.copy(
                    gender = event.gender,
                    genderErrorState = Validator.validateString(event.gender)
                )
            }

            is RegistrationUIEvent.PinCodeValueChangeEvent -> {
                registrationUIState.value = registrationUIState.value.copy(
                    pinCode = event.pinCode,
                    pinCodeErrorState = Validator.validatePinCode(event.pinCode)
                )
            }

            is RegistrationUIEvent.StateValueChangeEvent -> {
                registrationUIState.value = registrationUIState.value.copy(
                    state = event.state,
                    stateErrorState = Validator.validateString(event.state)
                )
            }

            is RegistrationUIEvent.AvailabilityCheckerValueChangeEvent -> {
                registrationUIState.value = registrationUIState.value.copy(
                    checkedAvailabilityStatus = event.status
                )
            }

            RegistrationUIEvent.RegistrationButtonClick -> {
                printState()
            }

            is RegistrationUIEvent.BloodUnitValueChangeEvent -> {}
            is RegistrationUIEvent.DonationCenterValueChangeEvent -> {}
        }
    }


    fun validateWithRulesForRegister(): Boolean {
        //  if (availableToDonate.value)
        return registrationUIState.value.nameErrorState.status
                && registrationUIState.value.emailIdErrorState.status
                && registrationUIState.value.phoneNoErrorState.status
                && registrationUIState.value.ageErrorState.status
                && registrationUIState.value.genderErrorState.status
                && registrationUIState.value.bloodGroupErrorState.status
                && registrationUIState.value.stateErrorState.status
                && registrationUIState.value.cityErrorState.status
                && registrationUIState.value.pinCodeErrorState.status
                && registrationUIState.value.passwordErrorState.status
                && registrationUIState.value.confirmPasswordState.status
    }


    fun printState() {
        Log.d(TAG, "Inside_printState")
        Log.d(TAG, registrationUIState.value.toString())
    }


    fun resetUiState() {
        registrationUIState.value = RegistrationUiState()
    }
}
