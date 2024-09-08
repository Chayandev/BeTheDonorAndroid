package com.example.bethedonor.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bethedonor.data.api.RetrofitClient
import com.example.bethedonor.data.dataModels.AcceptDonationResponse
import com.example.bethedonor.data.dataModels.BloodRequest
import com.example.bethedonor.data.dataModels.ProfileResponse
import com.example.bethedonor.data.dataModels.UserProfile
import com.example.bethedonor.data.dataModels.UserResponse
import com.example.bethedonor.data.preferences.PreferencesManager
import com.example.bethedonor.data.repository.UserRepositoryImp
import com.example.bethedonor.domain.usecase.AcceptDonationUseCase
import com.example.bethedonor.domain.usecase.FetchUserDetailsUseCase
import com.example.bethedonor.domain.usecase.GetAllBloodRequestsUseCase
import com.example.bethedonor.domain.usecase.GetUserProfileUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BloodRequestWithUser(
    val bloodRequest: BloodRequest,
    val user: UserProfile
)

class AllRequestViewModel(application: Application) : AndroidViewModel(application) {

    // ***** access the datastore ***** //
    private val preferencesManager = PreferencesManager(getApplication())
    fun getAuthToken(): String? {
        return preferencesManager.jwtToken
    }

    // ********************* **************************** //
    private val hasFetchedRequests = mutableStateOf(false)
    fun setFetchedProfile(value: Boolean) {
        hasFetchedRequests.value = value
    }

    fun getFetchedProfile(): Boolean {
        return hasFetchedRequests.value
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing.asStateFlow()

    fun setRefresherStatusTrue() {
        _isRefreshing.value = true
    }

    private val _currentUserDetails = MutableStateFlow<Result<ProfileResponse>?>(null)
    val currentUserDetails: StateFlow<Result<ProfileResponse>?> = _currentUserDetails

    private val _allBloodRequestResponse = MutableLiveData<Result<List<BloodRequestWithUser>>>()
    private val allBloodRequestResponse: LiveData<Result<List<BloodRequestWithUser>>> =
        _allBloodRequestResponse

    private val _allBloodRequestResponseList =
        MutableStateFlow<Result<List<BloodRequestWithUser>>?>(null)
    val allBloodRequestResponseList: StateFlow<Result<List<BloodRequestWithUser>>?> =
        _allBloodRequestResponseList

    private val apiService = RetrofitClient.instance
    private val userRepository = UserRepositoryImp(apiService)
    private val getAllBloodRequestsUseCase = GetAllBloodRequestsUseCase(userRepository)
    private val fetchUserDetailsUseCase = FetchUserDetailsUseCase(userRepository)
    private val getUserProfileUserUseCase = GetUserProfileUseCase(userRepository)
    private val acceptDonationUseCase = AcceptDonationUseCase(userRepository)

    val isRequestFetching = MutableStateFlow(false)
    val requestingToAccept = MutableStateFlow(mapOf<String, Boolean>())
    private val _isSheetVisible = MutableStateFlow(false)
    val isSheetVisible: StateFlow<Boolean> = _isSheetVisible

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

    fun getAllBloodRequest() {
        isRequestFetching.value = true
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
                    val bloodRequestWithUsers = bloodRequestWithUsersDeferred.awaitAll()
                    _allBloodRequestResponse.value = Result.success(bloodRequestWithUsers)
                    _allBloodRequestResponseList.value = Result.success(bloodRequestWithUsers)
                } else {
                    _allBloodRequestResponse.value = Result.failure(Exception(response.message))
                    _allBloodRequestResponseList.value = Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                _allBloodRequestResponse.value = Result.failure(e)
                _allBloodRequestResponseList.value = Result.failure(e)
            } finally {
                isRequestFetching.value = false
                _isRefreshing.value = false
            }
        }
    }

    fun fetchCurrentUserDetails() {
        viewModelScope.launch {
            try {
                val response = getUserProfileUserUseCase.execute(getAuthToken().toString())
                _currentUserDetails.value = Result.success(response)
                Log.d("currentUserDetails", response.toString())
                //  Log.d("currentUserDetails", response.user.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                _currentUserDetails.value = Result.failure(e)
            }
        }
    }

    fun acceptDonation(
        requestId: String,
        onResult: (Result<AcceptDonationResponse>) -> Unit
    ) {
        requestingToAccept.value = mapOf(requestId to true)
        viewModelScope.launch {
            try {
                val response = acceptDonationUseCase.execute(getAuthToken().toString(), requestId)
                onResult(Result.success(response))

                // Retrieve the current user details from the state flow
                _currentUserDetails.value?.getOrNull()?.let { profileResponse ->
                    // Create a new ProfileResponse instance with an updated donates list
                    val updatedProfileResponse = profileResponse.myProfile?.copy(
                        donates = (profileResponse.myProfile.donates ?: emptyList()) + requestId
                    )

                    // Update _currentUserDetails with the modified ProfileResponse wrapped in Result.success
                    _currentUserDetails.value =
                        Result.success(profileResponse.copy(myProfile = updatedProfileResponse))
                }
            } catch (e: Exception) {
                onResult(Result.failure(e))
            } finally {
                requestingToAccept.value = mapOf(requestId to false)
            }
        }
    }

    fun onToggleSheetDialog() {
        _isSheetVisible.value = !_isSheetVisible.value
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
            val result = allBloodRequestResponse.value
            val query = _searchText.value.trim()

            val filteredResult = result?.let {
                it.fold(
                    onSuccess = { bloodRequestsResponse ->
                        val filteredList = bloodRequestsResponse
                            .asSequence()
                            .filter { bloodRequestWithUser ->
                                // Search query filter
                                query.isEmpty() || bloodRequestWithUser.bloodRequest.bloodGroup.contains(
                                    query,
                                    ignoreCase = true
                                ) ||
                                        bloodRequestWithUser.bloodRequest.state.contains(
                                            query,
                                            ignoreCase = true
                                        ) ||
                                        bloodRequestWithUser.bloodRequest.district.contains(
                                            query,
                                            ignoreCase = true
                                        ) ||
                                        bloodRequestWithUser.bloodRequest.city.contains(
                                            query,
                                            ignoreCase = true
                                        ) ||
                                        bloodRequestWithUser.bloodRequest.pin.contains(
                                            query,
                                            ignoreCase = true
                                        )
                            }
                            .filter { bloodRequestWithUser ->
                                // State filter
                                filterState.value.isEmpty() || bloodRequestWithUser.bloodRequest.state.contains(
                                    filterState.value.trim(),
                                    ignoreCase = true
                                )
                            }
                            .filter { bloodRequestWithUser ->
                                // District filter
                                filterDistrict.value.isEmpty() || bloodRequestWithUser.bloodRequest.district.contains(
                                    filterDistrict.value.trim(),
                                    ignoreCase = true
                                )
                            }
                            .filter { bloodRequestWithUser ->
                                // City filter
                                filterCity.value.isEmpty() || bloodRequestWithUser.bloodRequest.city.contains(
                                    filterCity.value.trim(),
                                    ignoreCase = true
                                )
                            }
                            .filter { bloodRequestWithUser ->
                                // Pin filter
                                filterPin.value.isEmpty() || bloodRequestWithUser.bloodRequest.pin.contains(
                                    filterPin.value.trim(),
                                    ignoreCase = true
                                )
                            }
                            .toList()
                        Log.d("filteredList", filteredList.toString())
                        Result.success(filteredList)
                    },
                    onFailure = { error ->
                        Result.failure(Exception(error.message))
                    }
                )
            }
            _allBloodRequestResponseList.value = filteredResult

        } catch (e: Exception) {
            e.printStackTrace()
            _allBloodRequestResponseList.value = Result.failure(e)
        }
    }
}
