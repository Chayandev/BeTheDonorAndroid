package com.example.bethedonor.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bethedonor.data.api.RetrofitClient
import com.example.bethedonor.data.dataModels.AcceptDonationResponse
import com.example.bethedonor.data.dataModels.BloodRequest
import com.example.bethedonor.data.dataModels.ProfileResponse
import com.example.bethedonor.data.dataModels.UserProfile
import com.example.bethedonor.data.preferences.PreferencesManager
import com.example.bethedonor.data.repository.UserRepositoryImp
import com.example.bethedonor.domain.usecase.*
import com.example.bethedonor.utils.NetworkConnectivityMonitor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BloodRequestWithUser(
    val bloodRequest: BloodRequest,
    val user: UserProfile
)

class AllRequestViewModel(
    application: Application,
) : AndroidViewModel(application) {
//    init {
//        observeNetworkChanges()
//    }

    // ***** Access the datastore ***** //
    private val preferencesManager = PreferencesManager(getApplication())
    private fun getAuthToken(): String? = preferencesManager.jwtToken

    // ***** Mutable States ***** //
    private val _hasFetchedResult = MutableStateFlow(false)
    val hasFetchedResult: StateFlow<Boolean> get() = _hasFetchedResult

    private val _retryFlag = MutableStateFlow(false)
    val retryFlag: StateFlow<Boolean> get() = _retryFlag

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

    private val _currentUserDetails = MutableStateFlow<Result<ProfileResponse>?>(null)
    val currentUserDetails: StateFlow<Result<ProfileResponse>?> = _currentUserDetails

    private val _allBloodRequestResponseList =
        MutableStateFlow<Result<List<BloodRequestWithUser>>?>(null)
    val allBloodRequestResponseList: StateFlow<Result<List<BloodRequestWithUser>>?> =
        _allBloodRequestResponseList

    private val _allBloodRequestResponse =
        MutableStateFlow<Result<List<BloodRequestWithUser>>?>(null)
    private val allBloodRequestResponse: StateFlow<Result<List<BloodRequestWithUser>>?> =
        _allBloodRequestResponse

    private val _switchChecked = MutableStateFlow(false)
    val switchChecked: StateFlow<Boolean> = _switchChecked

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _filterState = MutableStateFlow("")
    val filterState: StateFlow<String> = _filterState

    private val _filterDistrict = MutableStateFlow("")
    val filterDistrict: StateFlow<String> = _filterDistrict

    private val _filterCity = MutableStateFlow("")
    val filterCity: StateFlow<String> = _filterCity

    private val _filterPin = MutableStateFlow("")
    val filterPin: StateFlow<String> = _filterPin

    private val _isNetWorkConnected = MutableStateFlow(true)
    val isNetworkConnected: StateFlow<Boolean> = _isNetWorkConnected

    // ***** Repositories and UseCases ***** //
    private val apiService = RetrofitClient.instance
    private val userRepository = UserRepositoryImp(apiService)
    private val getAllBloodRequestsUseCase = GetAllBloodRequestsUseCase(userRepository)
    private val fetchUserDetailsUseCase = FetchUserDetailsUseCase(userRepository)
    private val getUserProfileUserUseCase = GetUserProfileUseCase(userRepository)
    private val acceptDonationUseCase = AcceptDonationUseCase(userRepository)

    val isRequestFetching = MutableStateFlow(false)
    val requestingToAccept = MutableStateFlow(mapOf<String, Boolean>())

    // ***** UI State Handlers ***** //
    fun setFetchedResult(value: Boolean) {
        _hasFetchedResult.value = value
    }

    fun setSwitchChecked(value: Boolean) {
        _switchChecked.value = value
        filterByOpenCloseValue(value)
    }

    fun setRefresherStatusTrue() {
        _isRefreshing.value = true
    }

    private fun filterByOpenCloseValue(value: Boolean) {
        try {
            val result = allBloodRequestResponse.value
            val filteredResult = result?.let {
                it.fold(
                    onSuccess = { bloodRequestsResponse ->
                        val filteredList = bloodRequestsResponse.filter { bloodRequestWithUser ->
                            value && !bloodRequestWithUser.bloodRequest.isClosed || !value
                        }
                        resetFilterUi()
                        Result.success(filteredList)
                    },
                    onFailure = { error -> Result.failure(Exception(error.message)) }
                )
            }
            _allBloodRequestResponseList.value = filteredResult
        } catch (e: Exception) {
            e.printStackTrace()
            _allBloodRequestResponseList.value = Result.failure(e)
        }
    }

    private fun resetFilterUi() {
        clearFilters()
        _searchText.value = ""
    }

//    private fun observeNetworkChanges() {
//        // Observe the network connectivity changes
//        viewModelScope.launch {
//            try {
//                networkMonitor.isNetworkAvailable.collect { isConnected ->
//                    _isNetWorkConnected.value = isConnected
//                    if (isConnected) {
//                        _isNetWorkConnected.value = true
//
//                        if(!hasFetchedResult.value){
//                            getAllBloodRequest()
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("Network Monitor", "Error observing network changes", e)
//            }
//        }
//    }

    fun getAllBloodRequest() {
        isRequestFetching.value = true
        resetFilterUi()
        setSwitchChecked(false)
        viewModelScope.launch {
            try {
                val response = getAllBloodRequestsUseCase.execute(getAuthToken().toString())
                if (response.bloodRequests != null) {
                    // Use async to fetch user details in parallel
                    val bloodRequestWithUsersDeferred = response.bloodRequests.map { bloodRequest ->
                        async {
                            val userResponse =
                                fetchUserDetailsUseCase.execute(
                                    getAuthToken().toString(),
                                    bloodRequest.userId
                                )
                            if (userResponse.user == null) {
                                throw Exception("Failed to fetch user details for request: ${bloodRequest.userId}")
                            }
                            BloodRequestWithUser(bloodRequest, userResponse.user)
                        }
                    }
                    // Await all async calls
                    val bloodRequestWithUsers = bloodRequestWithUsersDeferred.awaitAll().reversed()
                    _allBloodRequestResponse.value = Result.success(bloodRequestWithUsers)
                    _allBloodRequestResponseList.value = Result.success(bloodRequestWithUsers)
                    _retryFlag.value = false
                } else {
                    _retryFlag.value = true
                    _allBloodRequestResponse.value = Result.failure(Exception(response.message))
                    _allBloodRequestResponseList.value = Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                _retryFlag.value = true
                _allBloodRequestResponse.value = Result.failure(e)
                _allBloodRequestResponseList.value = Result.failure(e)
            } finally {
                isRequestFetching.value = false
                _isRefreshing.value = false
                setFetchedResult(true)
            }
        }
    }

    fun fetchCurrentUserDetails() {
        viewModelScope.launch {
            try {
                val response = getUserProfileUserUseCase.execute(getAuthToken().toString())
                _currentUserDetails.value = Result.success(response)
                Log.d("currentUserDetails", response.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                _currentUserDetails.value = Result.failure(e)
            }
        }
    }

    fun acceptDonation(requestId: String, onResult: (Result<AcceptDonationResponse>) -> Unit) {
        requestingToAccept.value = mapOf(requestId to true)
        viewModelScope.launch {
            try {
                val response = acceptDonationUseCase.execute(getAuthToken().toString(), requestId)
                onResult(Result.success(response))
                updateCurrentUserDetails(requestId)
            } catch (e: Exception) {
                onResult(Result.failure(e))
            } finally {
                requestingToAccept.value = mapOf(requestId to false)
            }
        }
    }

    private fun updateCurrentUserDetails(requestId: String) {
        _currentUserDetails.value?.getOrNull()?.let { profileResponse ->
            val updatedProfileResponse = profileResponse.myProfile?.copy(
                donates = (profileResponse.myProfile.donates ?: emptyList()) + requestId
            )
            _currentUserDetails.value =
                Result.success(profileResponse.copy(myProfile = updatedProfileResponse))
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
        filterBloodRequests()
    }

    fun updateFilterState(state: String) {
        _filterState.value = state
        clearDistrictFilter()
    }

    fun updateFilterDistrict(district: String) {
        _filterDistrict.value = district
        clearPinFilter()
    }

    fun updateFilterCity(city: String) {
        _filterCity.value = city
    }

    fun updateFilterPin(pin: String) {
        _filterPin.value = pin
        clearCityFilter()
        filterBloodRequests()
    }

    fun clearStateFilter() {
        clearFilters()
        filterBloodRequests()
    }

    fun clearDistrictFilter() {
        _filterDistrict.value = ""
        _filterCity.value = ""
        _filterPin.value = ""
        filterBloodRequests()
    }

    fun clearPinFilter() {
        _filterPin.value = ""
        _filterCity.value = ""
        filterBloodRequests()
    }

    fun clearCityFilter() {
        _filterCity.value = ""
        filterBloodRequests()
    }

    private fun clearFilters() {
        _filterState.value = ""
        _filterDistrict.value = ""
        _filterCity.value = ""
        _filterPin.value = ""
    }

    private fun filterBloodRequests() {
        try {
            val result =
                if (switchChecked.value) allBloodRequestResponseList.value else allBloodRequestResponse.value
            val query = _searchText.value.trim()

            val filteredResult = result?.let {
                it.fold(
                    onSuccess = { bloodRequestsResponse ->
                        val filteredList = bloodRequestsResponse.filter { bloodRequestWithUser ->
                            (query.isEmpty() || bloodRequestWithUser.bloodRequest.matchesQuery(query)) &&
                                    bloodRequestWithUser.bloodRequest.matchesFilter(
                                        filterState.value,
                                        filterDistrict.value,
                                        filterCity.value,
                                        filterPin.value
                                    )
                        }
                        Log.d("filteredList", filteredList.toString())
                        Result.success(filteredList)
                    },
                    onFailure = { error -> Result.failure(Exception(error.message)) }
                )
            }
            _allBloodRequestResponseList.value = filteredResult

        } catch (e: Exception) {
            e.printStackTrace()
            _allBloodRequestResponseList.value = Result.failure(e)
        }
    }
}

private fun BloodRequest.matchesQuery(query: String): Boolean {
    return bloodGroup.contains(query, ignoreCase = true) ||
            state.contains(query, ignoreCase = true) ||
            district.contains(query, ignoreCase = true) ||
            city.contains(query, ignoreCase = true) ||
            pin.contains(query, ignoreCase = true)
}

private fun BloodRequest.matchesFilter(
    state: String,
    district: String,
    city: String,
    pin: String
): Boolean {
    return (state.isEmpty() || this.state.contains(state.trim(), ignoreCase = true)) &&
            (district.isEmpty() || this.district.contains(district.trim(), ignoreCase = true)) &&
            (city.isEmpty() || this.city.contains(city.trim(), ignoreCase = true)) &&
            (pin.isEmpty() || this.pin.contains(pin.trim(), ignoreCase = true))
}
