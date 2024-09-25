package com.example.bethedonor.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bethedonor.data.api.RetrofitClient
import com.example.bethedonor.data.dataModels.BackendResponse
import com.example.bethedonor.data.dataModels.BloodRequest
import com.example.bethedonor.data.dataModels.DonorListResponse
import com.example.bethedonor.data.preferences.PreferencesManager
import com.example.bethedonor.data.repository.UserRepositoryImp
import com.example.bethedonor.domain.usecase.DeleteRequestUseCase
import com.example.bethedonor.domain.usecase.GetDonorListUseCase
import com.example.bethedonor.domain.usecase.GetRequestHistoryUseCase
import com.example.bethedonor.domain.usecase.ToggleRequestStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Data class to hold the UI state for HistoryViewModel.
 * Includes various state flags and data related to requests and donors.
 */
data class UiState(
    val isRequestFetching: Boolean = false,
    val isDonorListFetching: Boolean = false,
    val isDeletingRequest: Boolean = false,
    val isToggleStatusRequestFetching: Map<String, Boolean> = emptyMap(),
    val requestHistory: Result<List<RequestHistory>>? = null,
    val donorListResponse: Result<DonorListResponse>? = null,
    val deleteRequestResponse: Result<String>? = null,
    val recomposeTime: Long = -1L
)

data class RequestHistory(
    val bloodRequest: BloodRequest
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    // ***** Access the datastore ***** //
    private val preferencesManager = PreferencesManager(getApplication())

    /**
     * Retrieves the JWT token from the PreferencesManager.
     * @return the JWT token as a String.
     */
    private fun getAuthToken(): String? = preferencesManager.jwtToken

    // ***** UseCase and Repository Setup ***** //
    private val apiService = RetrofitClient.instance
    private val userRepository = UserRepositoryImp(apiService)
    private val getRequestHistoryUseCase = GetRequestHistoryUseCase(userRepository)
    private val getDonorListUseCase = GetDonorListUseCase(userRepository)
    private val deleteRequestUseCase = DeleteRequestUseCase(userRepository)
    private val toggleRequestStatusUseCase = ToggleRequestStatusUseCase(userRepository)

    // ***** UI State ***** //
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    /**
     * Updates the recompose time and triggers UI recomposition if necessary.
     */
    fun updateRecomposeTime() {
        _uiState.value = _uiState.value.copy(recomposeTime = _uiState.value.recomposeTime + 1)
    }

    /**
     * Checks whether the request history should be fetched based on the recompose time.
     * Returns true if fetching is needed, otherwise false.
     */
    fun shouldFetch(): Boolean {
        return (_uiState.value.recomposeTime % 3).toInt() == 0
    }

    /**
     * Fetches the request history from the backend using GetRequestHistoryUseCase.
     * Updates the state with the fetched request history or an error message.
     */
    fun fetchRequestHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRequestFetching = true)
            try {
                val response = getRequestHistoryUseCase.execute(getAuthToken().toString())
                if (response.bloodRequests != null) {
                    val requestHistoryList = response.bloodRequests.map { RequestHistory(it) }
                    _uiState.value =
                        _uiState.value.copy(requestHistory = Result.success(requestHistoryList))
                } else {
                    _uiState.value =
                        _uiState.value.copy(requestHistory = Result.failure(Exception(response.message)))
                }
                Log.d("HistoryViewModel", response.toString())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(requestHistory = Result.failure(e))
            } finally {
                _uiState.value = _uiState.value.copy(isRequestFetching = false)
            }
        }
    }

    /**
     * Fetches the donor list for a specific request.
     * Updates the state with the donor list or an error message.
     */
    fun fetchDonorList(requestId: String) {
        _uiState.value = _uiState.value.copy(donorListResponse = null)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDonorListFetching = true)
            try {
                val response = getDonorListUseCase.execute(getAuthToken().toString(), requestId)
                if (response.donors != null) {
                    _uiState.value =
                        _uiState.value.copy(donorListResponse = Result.success(response))
                } else {
                    _uiState.value =
                        _uiState.value.copy(donorListResponse = Result.failure(Exception(response.message)))
                }
                Log.d("HistoryViewModel", "Donor List Response: $response")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(donorListResponse = Result.failure(e))
                Log.e("HistoryViewModel", "Error fetching donor list: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isDonorListFetching = false)
            }
        }
    }

    /**
     * Deletes a blood request and updates the state with the result.
     * Calls the provided onResponse callback with the result of the deletion.
     */
    fun deleteRequest(requestId: String, onResponse: (Result<String>) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeletingRequest = true)
            try {
                val response = deleteRequestUseCase.execute(getAuthToken().toString(), requestId)
                Log.d("HistoryViewModel", "Delete Request Response: $response")
                if (response.statusCode == "200") {
                    _uiState.value = _uiState.value.copy(
                        deleteRequestResponse = Result.success(
                            response.message ?: "Request deleted successfully"
                        )
                    )
                } else {
                    _uiState.value =
                        _uiState.value.copy(deleteRequestResponse = Result.failure(Exception("Error: ${response.message}")))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(deleteRequestResponse = Result.failure(e))
                Log.e("HistoryViewModel", "Error deleting request: ${e.message}")
            } finally {
                _uiState.value.deleteRequestResponse?.let { onResponse(it) }
                _uiState.value = _uiState.value.copy(isDeletingRequest = false)
            }
        }
    }

    /**
     * Updates the request history after a successful deletion by removing the deleted request.
     */
    fun updateAfterDeletion(requestId: String?) {
        val updatedList =
            _uiState.value.requestHistory?.getOrNull()?.filter { it.bloodRequest.id != requestId }
        _uiState.value =
            _uiState.value.copy(requestHistory = Result.success(updatedList ?: listOf()))
    }

    /**
     * Toggles the status of a blood request (open/closed).
     * Updates the state and UI based on the toggle response.
     */
    fun toggleRequestStatus(requestId: String, onToggleStatus: (BackendResponse) -> Unit) {
        _uiState.value =
            _uiState.value.copy(isToggleStatusRequestFetching = mapOf(requestId to true))
        viewModelScope.launch {
            try {
                val response =
                    toggleRequestStatusUseCase.execute(getAuthToken().toString(), requestId)
                Log.d("response_toggle", response.toString())
                onToggleStatus(
                    BackendResponse(
                        message = response.message,
                        statusCode = response.statusCode.toString()
                    )
                )

                _uiState.value.requestHistory?.getOrNull()?.let { requestHistoryList ->
                    val updatedRequestHistoryList = requestHistoryList.map { requestHistory ->
                        if (requestHistory.bloodRequest.id == requestId) {
                            requestHistory.copy(
                                bloodRequest = requestHistory.bloodRequest.copy(
                                    isClosed = !requestHistory.bloodRequest.isClosed
                                )
                            )
                        } else {
                            requestHistory
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        requestHistory = Result.success(updatedRequestHistoryList)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onToggleStatus(BackendResponse(message = e.message, statusCode = "500"))
            } finally {
                _uiState.value =
                    _uiState.value.copy(isToggleStatusRequestFetching = mapOf(requestId to false))
            }
        }
    }

    /**
     * Resets all mutable states to their initial values.
     */
    fun resetUiStates() {
        _uiState.value = UiState()
    }
}
