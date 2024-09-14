package com.example.bethedonor.presentation.main_screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bethedonor.R
import com.example.bethedonor.ui.utils.uievent.LoginUIEvent
import com.example.bethedonor.ui.components.ButtonComponent
import com.example.bethedonor.ui.components.EditText
import com.example.bethedonor.ui.components.ForgotPassword
import com.example.bethedonor.ui.components.GreetingText
import com.example.bethedonor.ui.components.PasswordFiled
import com.example.bethedonor.ui.components.ProgressIndicatorComponent
import com.example.bethedonor.ui.components.SimpleTextWithSpan
import com.example.bethedonor.ui.components.SubGreetText
import com.example.bethedonor.ui.theme.fadeBlue1
import com.example.bethedonor.ui.theme.fadeBlue2
import com.example.bethedonor.utils.ValidationResult
import com.example.bethedonor.viewmodels.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    onLoginNavigate: () -> Unit,
    onRegisterNavigate: () -> Unit
) {
    val context = LocalContext.current
    var recheckFiled by remember { mutableStateOf(false) }
    val loginResponse by loginViewModel.loginResponse.observeAsState()

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(fadeBlue1, fadeBlue2),
                                start = Offset.Zero,
                                end = Offset.Infinite
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .padding(vertical = 20.dp, horizontal = 20.dp)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                GreetingText()
                                Spacer(modifier = Modifier.size(8.dp))
                                SubGreetText(text = "Welcome Back")
                                Spacer(modifier = Modifier.size(20.dp))
                                EditText(
                                    label = stringResource(id = R.string.label_emailId),
                                    labelIcon = Icons.Filled.Email,
                                    onFiledValueChanged = {
                                        loginViewModel.onEvent(LoginUIEvent.EmailValueChangeEvent(it))
                                        loginViewModel.printState()
                                        loginViewModel.loginUIState.value.emailIdErrorState
                                    },
                                    recheckFiled = recheckFiled
                                )
                                PasswordFiled(
                                    stringResource(id = R.string.label_password),
                                    Icons.Filled.Password,
                                    true,
                                    onFiledValueChanged = {
                                        loginViewModel.onEvent(
                                            LoginUIEvent.PasswordValueChangeEvent(it)
                                        )
                                        loginViewModel.printState()
                                        ValidationResult(true)
                                    },
                                    recheckFiled = recheckFiled
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ForgotPassword(onResetProcessResult = { message ->
                                    scope.launch {
                                        snackBarHostState.showSnackbar(
                                            message = message,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                })
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                ButtonComponent(
                                    buttonText = stringResource(id = R.string.button_login),
                                    onButtonClick = {
                                        recheckFiled = true
                                        if (loginViewModel.validateWithRulesForLogIn()) {
                                            loginViewModel.logInUser(
                                                onLogin = {
                                                    loginResponse?.let {
                                                        if (it.isSuccess) {
                                                            if (it.getOrNull()?.statusCode == null && it.getOrNull()?.message != "timeout") {
                                                                onLoginNavigate()
                                                            }
                                                            scope.launch {
                                                                snackBarHostState.showSnackbar(
                                                                    message = it.getOrNull()?.message ?: "Unknown error",
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }
                                                        } else {
                                                            scope.launch {
                                                                snackBarHostState.showSnackbar(
                                                                    message = it.exceptionOrNull()?.message ?: "Unknown error",
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }
                                                        }
                                                    }
                                                })
                                        } else {
                                            scope.launch {
                                                snackBarHostState.showSnackbar(
                                                    message = context.getString(R.string.message),
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    },
                                    isEnable = loginViewModel.validateWithRulesForLogIn() && !loginViewModel.requestInProgress.value
                                )
                                Spacer(modifier = Modifier.size(20.dp))
                                SimpleTextWithSpan(
                                    stringResource(id = R.string.don_t_have_account) + " ",
                                    stringResource(id = R.string.button_register),
                                    onTextClicked = {
                                        onRegisterNavigate()
                                    }, modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                        }

                    }
                    if (loginViewModel.requestInProgress.value)
                        ProgressIndicatorComponent(label = stringResource(id = R.string.login_indicator))
                }

            }

        }
    }
}


@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onRegisterNavigate = {},
        onLoginNavigate = {},
        loginViewModel = LoginViewModel(application = Application())
    )
}