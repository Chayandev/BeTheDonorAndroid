package com.example.bethedonor.ui.main_screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.bethedonor.R
import com.example.bethedonor.ui.components.ButtonComponent
import com.example.bethedonor.ui.components.EditText
import com.example.bethedonor.ui.components.ProgressIndicatorComponent
import com.example.bethedonor.ui.components.SubGreetText
import com.example.bethedonor.ui.theme.Gray1
import com.example.bethedonor.ui.theme.Gray3
import com.example.bethedonor.ui.theme.bloodRed2
import com.example.bethedonor.ui.theme.fadeBlue11
import com.example.bethedonor.ui.theme.teal
import com.example.bethedonor.ui.utils.commons.showToast
import com.example.bethedonor.ui.utils.uievent.RegistrationUIEvent
import com.example.bethedonor.viewmodels.EditEmailViewModel
import com.example.bethedonor.viewmodels.ProfileViewModel
import com.example.bethedonor.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EditEmailScreen(
    editEmailViewModel: EditEmailViewModel,
    sharedViewModel: SharedViewModel,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    Scaffold(topBar = {
        ScreenTopBar(onNavigateBack = {
            onNavigateBack()
        })
    }, containerColor = fadeBlue11) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(fadeBlue11)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                SubGreetText(text = stringResource(R.string.change_email_address))
                Text(
                    text = stringResource(R.string.email_verification_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gray1
                )
                EditText(
                    label = stringResource(id = R.string.label_emailId),
                    labelIcon = Icons.Filled.Email,
                    onFiledValueChanged = {
                        editEmailViewModel.onEvent(
                            RegistrationUIEvent.EmailValueChangeEvent(it)
                        )
                        editEmailViewModel.editEmailUiState.value.emailIdErrorState
                    },
                )
                ButtonComponent(buttonText = stringResource(R.string.button_next), onButtonClick = {
                    editEmailViewModel.changeEmailId(onResponse = { response ->
                        response.let {
                            if (!it.otpId.isNullOrEmpty()) {
                                editEmailViewModel.setOTPDialog(true)
                            }
                            showToast(context = context, message = it.message.toString())
                        }
                    })
                }, isEnable = editEmailViewModel.editEmailUiState.value.emailIdErrorState.status)
                Text(
                    text = stringResource(R.string.agree_to_receive_code),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray
                )
            }
            if (editEmailViewModel.otpDialog.collectAsState().value) {
                OTPDialog(editEmailViewModel, onNavigateBack = {
                    onNavigateBack()
                })
            }

            if (editEmailViewModel.requestInProgress.value) {
                ProgressIndicatorComponent(label = stringResource(id = R.string.sending_indicator))
            }
            if (editEmailViewModel.verifyInProgress.value) {
                ProgressIndicatorComponent(label = stringResource(id = R.string.verifying_indicator))
            }
        }
    }

}

@Composable
fun ScreenTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(fadeBlue11)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back),
            modifier = Modifier
                .size(30.dp)
                .clickable {
                    onNavigateBack()
                }, tint = Color.White
        )
        Text(
            text = stringResource(R.string.edit_email_title),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OTPDialog(
    editEmailViewModel: EditEmailViewModel,
    onNavigateBack: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    // Launch effect to set the sheet to partially expanded initially
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            Log.d("bottomsheet_expand", "${sheetState.currentValue}")
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            coroutineScope.launch {
                sheetState.hide()
            }
            editEmailViewModel.setOTPDialog(false)
        },
        sheetState = sheetState,
        modifier = Modifier
            .fillMaxSize(),
        containerColor = fadeBlue11,
    ) {
        val context = LocalContext.current
        // State for OTP input
        val otpState = remember { mutableStateListOf("", "", "", "", "", "") }
        // State for countdown timer
        val timerState = remember { mutableIntStateOf(60) }
        // State for enabling/disabling the resend button
        val isResendEnabled = remember { mutableStateOf(false) }

        // Effect to start the countdown timer
        LaunchedEffect(key1 = timerState.intValue) {
            if (timerState.intValue > 0) {
                delay(1000L)
                timerState.intValue -= 1
            } else {
                isResendEnabled.value = true
            }
        }

        // Check if all OTP fields are filled
        val isOtpComplete = otpState.all { it.isNotEmpty() }

        Box(
            modifier = Modifier
                .background(fadeBlue11, shape = RoundedCornerShape(8.dp))
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp), // Added padding to avoid edge clipping
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize() // Wrap content size
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Check if not partially expanded
                    if (sheetState.currentValue != SheetValue.PartiallyExpanded) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_otp),
                            contentDescription = stringResource(R.string.otp_custom_image_desc),
                            modifier = Modifier
                                .size(80.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = stringResource(R.string._verification),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Enter the 6-digit code sent to your email-id ${editEmailViewModel.editEmailUiState.value.emailId}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray3,
                        textAlign = TextAlign.Center
                    )
                }
                // Display OTP input boxes
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.Center
                ) {
                    otpState.forEachIndexed { index, otpChar ->
                        OTPDigitField(
                            value = otpChar,
                            onValueChange = { newChar ->
                                if (newChar.length <= 1) {
                                    otpState[index] = newChar
                                }
                            },
                            isOtpComplete = isOtpComplete
                        )
                    }
                }

                // Display countdown timer and resend button
                Text(
                    text = if (timerState.intValue > 0) "Resend OTP in ${(timerState.intValue)} seconds else stringResource" else stringResource(
                        id = R.string.don_t_receive_otp
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (timerState.intValue > 0) teal else Color.White
                )
                Text(
                    text = stringResource(id = R.string.resend_otp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isResendEnabled.value) bloodRed2 else Color.Gray,
                    textDecoration = TextDecoration.Underline,
                    fontWeight = if (isResendEnabled.value) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clickable(enabled = isResendEnabled.value) {
                            timerState.intValue = 60
                            isResendEnabled.value = false
                            // Trigger resend OTP logic here
                        }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Verify OTP button
                ButtonComponent(
                    buttonText = context.getString(R.string.verify_and_proceed),
                    onButtonClick = {
                        editEmailViewModel.verifyOTP(
                            otpState.joinToString(""),
                            onResponse = { response ->
                                if (response.statusCode == context.getString(R.string.status_code_success)) {
                                    coroutineScope.launch {
                                        sheetState.hide()
                                    }
                                    editEmailViewModel.setOTPDialog(false)
                                    onNavigateBack()
                                }
                                showToast(context = context, response.message.toString())
                            })
                    },
                    isEnable = isOtpComplete
                )
            }

        }
    }
}

@Composable
fun OTPDigitField(
    value: String,
    onValueChange: (String) -> Unit,
    isOtpComplete: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .width(50.dp)
            .height(50.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            textAlign = TextAlign.Center,
            color = if (isOtpComplete) teal else Color.White,
            fontWeight = FontWeight.SemiBold
        ),
        singleLine = true,
        maxLines = 1,
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = teal,
            focusedBorderColor = if (isOtpComplete) teal else Color.White,
            unfocusedBorderColor = if (isOtpComplete) teal else Color.LightGray,
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
    )
}