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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BloodRequestWithUser(
    val bloodRequest: BloodRequest,
    val user: UserProfile
)
data class AllRequestUiState(
    val hasFetchedResult: Boolean = false,
    val retryFlag: Boolean = false,
    val isRefreshing: Boolean = false,
    val currentUserDetails: Result<ProfileResponse>? = null,
    val allBloodRequestResponseList: Result<List<BloodRequestWithUser>>? = null,
    val allBloodRequestResponse: Result<List<BloodRequestWithUser>>? = null,
    val switchChecked: Boolean = false,
    val searchText: String = "",
    val filterState: String = "",
    val filterDistrict: String = "",
    val filterCity: String = "",
    val filterPin: String = "",
    val isNetworkConnected: Boolean = true,
    val isRequestFetching: Boolean = false,
    val requestingToAccept: Map<String, Boolean> = emptyMap(),
    val isFiltered:Boolean=false
)


class AllRequestViewModel(
    application: Application,
) : AndroidViewModel(application) {

    // Preferences manager for JWT token retrieval
    private val preferencesManager = PreferencesManager(getApplication())
    private fun getAuthToken(): String? = preferencesManager.jwtToken

    // MutableStateFlow to manage UI state with initial state as AllRequestUiState
    private val _uiState = MutableStateFlow(AllRequestUiState())
    val uiState: StateFlow<AllRequestUiState> get() = _uiState

    // API service and repositories for data fetching and processing
    private val apiService = RetrofitClient.instance
    private val userRepository = UserRepositoryImp(apiService)
    private val getAllBloodRequestsUseCase = GetAllBloodRequestsUseCase(userRepository)
    private val fetchUserDetailsUseCase = FetchUserDetailsUseCase(userRepository)
    private val getUserProfileUserUseCase = GetUserProfileUseCase(userRepository)
    private val acceptDonationUseCase = AcceptDonationUseCase(userRepository)

    /**
     * Updates the switchChecked state and filters blood requests based on the switch value.
     */
    fun setSwitchChecked(value: Boolean) {
        _uiState.update { it.copy(switchChecked = value) }
        filterByOpenCloseValue(value)
    }

    /**
     * Updates the refresh status state with value true
     */
    fun setRefresherStatusTrue(){
        _uiState.update { it.copy(isRefreshing = true) }
    }

    fun setIsFiltered(value:Boolean){
         _uiState.update { it.copy(isFiltered=value) }
    }
    /**
     * Filters blood requests based on the switch value (open/closed).
     */
    private fun filterByOpenCloseValue(value: Boolean) {
        _uiState.update { it.copy(isFiltered = true) }
        val result = uiState.value.allBloodRequestResponse
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
        _uiState.update { it.copy(allBloodRequestResponseList = filteredResult) }
    }

    /**
     * Resets filter UI state and clears search text.
     */
    private fun resetFilterUi() {
        clearFilters()
        _uiState.update { it.copy(searchText = "") }
    }


    fun parallelNetworkCall(){
        viewModelScope.launch {
            try {
                // Launch both network calls in parallel using async
                val getAllBloodRequestDeferred = async {
                   getAllBloodRequest()
                }
                val fetchCurrentUserDetailsDeferred = async {
                    fetchCurrentUserDetails()
                }
                // Await the results of both calls
                awaitAll(getAllBloodRequestDeferred, fetchCurrentUserDetailsDeferred)
                // Handle any additional logic if needed after both calls are complete
            } catch (e: Exception) {
                // Handle any exceptions that occur during the network calls
                e.printStackTrace()
            }
        }
    }


    /**
     * Fetches all blood requests and updates the UI state accordingly.
     */
    private fun getAllBloodRequest() {
        _uiState.update { it.copy(isRequestFetching = true) }
        resetFilterUi()
        setSwitchChecked(false)
        viewModelScope.launch {
            try {
                val response = getAllBloodRequestsUseCase.execute(getAuthToken().toString())
                if (response.bloodRequests != null) {
                    // Fetch user details for each blood request asynchronously
                    val bloodRequestWithUsersDeferred = response.bloodRequests.map { bloodRequest ->
                        async {
                            val userResponse = fetchUserDetailsUseCase.execute(getAuthToken().toString(), bloodRequest.userId)
                            if (userResponse.user == null) {
                                throw Exception("Failed to fetch user details for request: ${bloodRequest.userId}")
                            }
                            BloodRequestWithUser(bloodRequest, userResponse.user)
                        }
                    }
                    val bloodRequestWithUsers = bloodRequestWithUsersDeferred.awaitAll().reversed()
                    _uiState.update {
                        it.copy(
                            allBloodRequestResponse = Result.success(bloodRequestWithUsers),
                            allBloodRequestResponseList = Result.success(bloodRequestWithUsers),
                            retryFlag = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            retryFlag = true,
                            allBloodRequestResponse = Result.failure(Exception(response.message)),
                            allBloodRequestResponseList = Result.failure(Exception(response.message))
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        retryFlag = true,
                        allBloodRequestResponse = Result.failure(e),
                        allBloodRequestResponseList = Result.failure(e)
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(
                        isRequestFetching = false,
                        isRefreshing = false,
                        hasFetchedResult = true
                    )
                }
            }
        }
    }

    /**
     * Fetches current user details and updates the UI state.
     */
    private fun fetchCurrentUserDetails() {
        viewModelScope.launch {
            try {
                val response = getUserProfileUserUseCase.execute(getAuthToken().toString())
                _uiState.update { it.copy(currentUserDetails = Result.success(response)) }
                Log.d("currentUserDetails", response.toString())
            } catch (e: Exception) {
                _uiState.update { it.copy(currentUserDetails = Result.failure(e)) }
                e.printStackTrace()
            }
        }
    }

    /**
     * Accepts a blood donation request and updates the UI state and current user details.
     */
    fun acceptDonation(requestId: String, onResult: (Result<AcceptDonationResponse>) -> Unit) {
        _uiState.update { it.copy(requestingToAccept = mapOf(requestId to true)) }
        viewModelScope.launch {
            try {
                val response = acceptDonationUseCase.execute(getAuthToken().toString(), requestId)
                onResult(Result.success(response))
                updateCurrentUserDetails(requestId)
            } catch (e: Exception) {
                onResult(Result.failure(e))
            } finally {
                _uiState.update { it.copy(requestingToAccept = mapOf(requestId to false)) }
            }
        }
    }

    /**
     * Updates current user details with the accepted donation request.
     */
    private fun updateCurrentUserDetails(requestId: String) {
        _uiState.value.currentUserDetails?.getOrNull()?.let { profileResponse ->
            val updatedProfileResponse = profileResponse.myProfile?.copy(
                donates = (profileResponse.myProfile.donates ?: emptyList()) + requestId
            )
            _uiState.update { it.copy(currentUserDetails = Result.success(profileResponse.copy(myProfile = updatedProfileResponse))) }
        }
    }

    /**
     * Handles changes to the search text and filters blood requests based on the updated text.
     */
    fun onSearchTextChange(text: String) {
        _uiState.update { it.copy(searchText = text) }
        filterBloodRequests()
    }

    /**
     * Updates the filter state and clears related filters.
     */
    fun updateFilterState(state: String) {
        _uiState.update { it.copy(filterState = state) }
        clearDistrictFilter()
    }

    /**
     * Updates the filter district and clears related filters.
     */
    fun updateFilterDistrict(district: String) {
        _uiState.update { it.copy(filterDistrict = district) }
        clearPinFilter()
    }

    /**
     * Updates the filter city.
     */
    fun updateFilterCity(city: String) {
        _uiState.update { it.copy(filterCity = city) }
    }

    /**
     * Updates the filter pin and clears related filters.
     */
    fun updateFilterPin(pin: String) {
        _uiState.update { it.copy(filterPin = pin) }
        clearCityFilter()
        filterBloodRequests()
    }

    /**
     * Clears all filters and refreshes the blood requests list.
     */
    fun clearStateFilter() {
        clearFilters()
        filterBloodRequests()
    }

    /**
     * Clears district-related filters and refreshes the blood requests list.
     */
    fun clearDistrictFilter() {
        _uiState.update { it.copy(filterDistrict = "", filterCity = "", filterPin = "") }
        filterBloodRequests()
    }

    /**
     * Clears pin-related filters and refreshes the blood requests list.
     */
    fun clearPinFilter() {
        _uiState.update { it.copy(filterPin = "", filterCity = "") }
        filterBloodRequests()
    }

    /**
     * Clears city-related filters and refreshes the blood requests list.
     */
    fun clearCityFilter() {
        _uiState.update { it.copy(filterCity = "") }
        filterBloodRequests()
    }

    /**
     * Clears all filter criteria.
     */
    private fun clearFilters() {
        _uiState.update {
            it.copy(
                filterState = "",
                filterDistrict = "",
                filterCity = "",
                filterPin = ""
            )
        }
    }

    /**
     * Filters blood requests based on search text and filter criteria.
     */
    private fun filterBloodRequests() {
        try {
            _uiState.update { it.copy(isFiltered = true) }
            val result = if (_uiState.value.switchChecked) uiState.value.allBloodRequestResponseList else uiState.value.allBloodRequestResponse
            val query = _uiState.value.searchText.trim()

            val filteredResult = result?.let {
                it.fold(
                    onSuccess = { bloodRequestsResponse ->
                        val filteredList = bloodRequestsResponse.filter { bloodRequestWithUser ->
                            (query.isEmpty() || bloodRequestWithUser.bloodRequest.matchesQuery(query)) &&
                                    bloodRequestWithUser.bloodRequest.matchesFilter(
                                        _uiState.value.filterState,
                                        _uiState.value.filterDistrict,
                                        _uiState.value.filterCity,
                                        _uiState.value.filterPin
                                    )
                        }
                        Log.d("filteredList", filteredList.toString())
                        Result.success(filteredList)
                    },
                    onFailure = { error -> Result.failure(Exception(error.message)) }
                )
            }
            _uiState.update { it.copy(allBloodRequestResponseList = filteredResult) }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update { it.copy(allBloodRequestResponseList = Result.failure(e)) }
        }
    }

    /**
     * Checks if the blood request matches the search query in any of the relevant fields.
     *
     * @param query The search query to match against the blood request's attributes.
     * @return True if any of the blood request's attributes contain the query (case-insensitive), otherwise false.
     */
    private fun BloodRequest.matchesQuery(query: String): Boolean {
        return bloodGroup.contains(query, ignoreCase = true) ||
                state.contains(query, ignoreCase = true) ||
                district.contains(query, ignoreCase = true) ||
                city.contains(query, ignoreCase = true) ||
                pin.contains(query, ignoreCase = true)
    }

    /**
     * Checks if the blood request matches the filter criteria based on state, district, city, and pin.
     *
     * @param state The state filter criterion.
     * @param district The district filter criterion.
     * @param city The city filter criterion.
     * @param pin The pin filter criterion.
     * @return True if the blood request matches all non-empty filter criteria, otherwise false.
     */
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


    /**
     * Resets the UI state to its initial values.
     */
    fun resetUiState() {
        _uiState.value = AllRequestUiState()
    }


}
