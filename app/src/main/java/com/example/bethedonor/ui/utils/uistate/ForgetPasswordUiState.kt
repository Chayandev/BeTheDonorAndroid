package com.example.bethedonor.ui.utils.uistate
import com.example.bethedonor.utils.ValidationResult
data class ForgetPasswordUiState(
    val emailId: String = "",
    val emailIdErrorState: ValidationResult = ValidationResult()
)