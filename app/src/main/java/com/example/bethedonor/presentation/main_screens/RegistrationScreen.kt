package com.example.bethedonor.presentation.main_screens

import PhoneNumberEditText
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bethedonor.R
import com.example.bethedonor.ui.utils.uievent.RegistrationUIEvent
import com.example.bethedonor.ui.components.AvailabilityCheckerField
import com.example.bethedonor.ui.components.ButtonComponent
import com.example.bethedonor.ui.components.CalendarSelectField
import com.example.bethedonor.ui.components.EditText
import com.example.bethedonor.ui.components.GreetingText
import com.example.bethedonor.ui.components.PasswordFiled
import com.example.bethedonor.ui.components.ProgressIndicatorComponent
import com.example.bethedonor.ui.components.SelectStateDistrictCityField
import com.example.bethedonor.ui.components.SelectionField
import com.example.bethedonor.ui.components.SimpleTextWithSpan
import com.example.bethedonor.ui.components.SubGreetText
import com.example.bethedonor.ui.utils.commons.linearGradientBrush
import com.example.bethedonor.ui.utils.commons.showToast
import com.example.bethedonor.constants.bloodGroupList1
import com.example.bethedonor.constants.genderList
import com.example.bethedonor.constants.getCityList
import com.example.bethedonor.constants.getDistrictList
import com.example.bethedonor.constants.getPinCodeList
import com.example.bethedonor.constants.getStateDataList
import com.example.bethedonor.utils.NetworkConnectivityMonitor
import com.example.bethedonor.viewmodels.RegistrationViewModel

@Composable
fun RegistrationScreen(
    onRegisterNavigate: () -> Unit,
    registrationViewModel: RegistrationViewModel = viewModel(),
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val bloodGroupsList = bloodGroupList1
    val genderList = genderList

    val selectedState by registrationViewModel.selectedState.collectAsState()
    val selectedDistrict by registrationViewModel.selectedDistrict.collectAsState()
    val selectedCity by registrationViewModel.selectedCity.collectAsState()
    val selectedPinCode by registrationViewModel.selectedPinCode.collectAsState()
    val requestInProgress by registrationViewModel.requestInProgress.collectAsState()
    val availableToDonate by registrationViewModel.availableToDonate.collectAsState()
    val registrationResponse by registrationViewModel.registrationResponse.observeAsState()

    val recheckFiled by remember {
        mutableStateOf(false)
    }
    Box(
        modifier = Modifier
            .fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        linearGradientBrush()
                    ), contentAlignment = Alignment.Center
            )
            {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            GreetingText()
                            Spacer(modifier = Modifier.height(8.dp))
                            SubGreetText(text = "Register as a Donor")
                            Spacer(modifier = Modifier.size(20.dp))
                            EditText(
                                label = stringResource(id = R.string.label_name),
                                labelIcon = Icons.Filled.Person,
                                onFiledValueChanged = {
                                    registrationViewModel.onEvent(
                                        RegistrationUIEvent.NameValueChangeEvent(
                                            it
                                        )
                                    )
                                    registrationViewModel.printState()
                                    registrationViewModel.registrationUIState.value.nameErrorState
                                },
                                recheckFiled = recheckFiled
                            )
                            EditText(
                                label = stringResource(id = R.string.label_emailId),
                                labelIcon = Icons.Filled.Email,
                                onFiledValueChanged = {
                                    registrationViewModel.onEvent(
                                        RegistrationUIEvent.EmailValueChangeEvent(
                                            it
                                        )
                                    )
                                    registrationViewModel.printState()
                                    registrationViewModel.registrationUIState.value.emailIdErrorState
                                },
                                recheckFiled = recheckFiled
                            )
                            var code by remember { mutableStateOf("") }
                            PhoneNumberEditText(
                                onFieldValueChanged = {
                                    registrationViewModel.onEvent(
                                        RegistrationUIEvent.PhoneNoChangeEvent(code + it)
                                    )
                                    registrationViewModel.printState()
                                    registrationViewModel.registrationUIState.value.phoneNoErrorState
                                },
                                recheckField = recheckFiled,
                                code = {
                                    code = it
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                            CalendarSelectField(
                                onFieldValueChanged = {
                                    registrationViewModel.onEvent(
                                        RegistrationUIEvent.DateValueChangeEvent(it)
                                    )
                                    registrationViewModel.printState()
                                    registrationViewModel.registrationUIState.value.ageErrorState
                                },
                                recheckField = recheckFiled,
                                label = stringResource(id = R.string.label_DOB),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding()
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                //Gender
                                SelectionField(
                                    options = genderList,
                                    label = stringResource(id = R.string.label_gender),
                                    onSelection = {
                                        registrationViewModel.onEvent(
                                            RegistrationUIEvent.GenderValueChangeEvent(it)
                                        )
                                        registrationViewModel.printState()
                                        registrationViewModel.registrationUIState.value.genderErrorState
                                    },
                                    //  recheckFiled = recheckFiled,
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .weight(1f)
                                        .padding(end = 4.dp)

                                )
                                //Blood Group
                                SelectionField(
                                    options = bloodGroupsList,
                                    label = stringResource(id = R.string.label_blood_group),
                                    onSelection = {
                                        registrationViewModel.onEvent(
                                            RegistrationUIEvent.BloodGroupValueChangeEvent(it)
                                        )
                                        registrationViewModel.printState()
                                        registrationViewModel.registrationUIState.value.bloodGroupErrorState
                                    },
                                    //   recheckFiled = recheckFiled,
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .weight(1f)
                                        .padding(start = 4.dp)

                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SelectStateDistrictCityField(
                                    label = stringResource(id = R.string.label_state),
                                    options = getStateDataList(),
                                    selectedValue = selectedState,
                                    onSelection = {
                                        registrationViewModel.onEvent(
                                            RegistrationUIEvent.StateValueChangeEvent(
                                                it
                                            )
                                        )
                                        registrationViewModel.selectState(it)
                                        registrationViewModel.printState()
                                        registrationViewModel.registrationUIState.value.stateErrorState
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp),
                                    //  recheckFiled = recheckFiled
                                    onSearchTextFieldClicked = {
                                        keyboardController?.show()
                                    }
                                )
                                SelectStateDistrictCityField(
                                    label = stringResource(id = R.string.label_district),
                                    options = getDistrictList(selectedState = selectedState),
                                    selectedValue = selectedDistrict,
                                    onSelection = {
                                        registrationViewModel.onEvent(
                                            RegistrationUIEvent.DistrictValueChangeEvent(
                                                it
                                            )
                                        )
                                        registrationViewModel.selectDistrict(it)
                                        registrationViewModel.printState()
                                        registrationViewModel.registrationUIState.value.districtErrorState
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 4.dp),
                                    // recheckFiled = recheckFiled,
                                    onSearchTextFieldClicked = {
                                        keyboardController?.show()
                                    })

                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SelectStateDistrictCityField(
                                    label = stringResource(id = R.string.label_pin),
                                    options = getPinCodeList(
                                        selectedState = selectedState,
                                        selectedDistrict = selectedDistrict
                                    ),
                                    selectedValue = selectedPinCode,
                                    onSelection = {
                                        registrationViewModel.onEvent(
                                            RegistrationUIEvent.PinCodeValueChangeEvent(
                                                it
                                            )
                                        )
                                        registrationViewModel.selectPin(it)
                                        registrationViewModel.printState()
                                        registrationViewModel.registrationUIState.value.pinCodeErrorState
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp),
                                    //   recheckFiled = recheckFiled,
                                    onSearchTextFieldClicked = {
                                        keyboardController?.show()
                                    }
                                )
                                SelectStateDistrictCityField(
                                    label = stringResource(id = R.string.label_city),
                                    options = getCityList(
                                        selectedState = selectedState,
                                        selectedDistrict = selectedDistrict,
                                        selectedPinCode = selectedPinCode
                                    ),
                                    selectedValue = selectedCity,
                                    onSelection = {
                                        registrationViewModel.onEvent(
                                            RegistrationUIEvent.CityValueChangeEvent(
                                                it
                                            )
                                        )
                                        registrationViewModel.selectCity(it)
                                        registrationViewModel.printState()
                                        registrationViewModel.registrationUIState.value.cityErrorState
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 4.dp),
                                    // recheckFiled = recheckFiled,
                                    onSearchTextFieldClicked = {
                                        keyboardController?.show()
                                    }
                                )
                            }
                        }
                        PasswordFiled(
                            label = stringResource(id = R.string.label_password),
                            labelIcon = Icons.Filled.Password,
                            onFiledValueChanged = {
                                registrationViewModel.onEvent(
                                    RegistrationUIEvent.PasswordValueChangeEvent(
                                        it
                                    )
                                )
                                registrationViewModel.printState()
                                registrationViewModel.registrationUIState.value.passwordErrorState
                            },
                            recheckFiled = recheckFiled
                        )

                        PasswordFiled(
                            label = stringResource(id = R.string.label_confirm_password),
                            Icons.Filled.Password,
                            isConfirmPasswordField = true,
                            onFiledValueChanged = {
                                registrationViewModel.onEvent(
                                    RegistrationUIEvent.ConfirmPasswordValueChangeEvent(
                                        it
                                    )
                                )
                                registrationViewModel.printState()
                                registrationViewModel.registrationUIState.value.confirmPasswordState
                            }, recheckFiled = recheckFiled
                        )
                        AvailabilityCheckerField(
                            value = availableToDonate,
                            onCheckerChange = {
                                registrationViewModel.onEvent(
                                    RegistrationUIEvent.AvailabilityCheckerValueChangeEvent(it)
                                )
                                registrationViewModel.setAvailableToDonate(it)
                                registrationViewModel.printState()
                                registrationViewModel.registrationUIState.value.checkedAvailabilityStatus
                            })
                    }
                }
                Box(contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        ButtonComponent(
                            "Register",
                            onButtonClick = {
                                Log.d(
                                    "validity",
                                    registrationViewModel.validateWithRulesForRegister()
                                        .toString()
                                )
                                if (registrationViewModel.validateWithRulesForRegister()) {
                                    registrationViewModel.registerUser(onRegister = {
                                        registrationResponse?.let {
                                            if (it.isSuccess) {
                                                if (it.getOrNull()?.statusCode == null && it.getOrNull()?.message != "timeout") {
                                                    onRegisterNavigate()
                                                }
                                                showToast(
                                                    context,
                                                    it.getOrNull()?.message.toString(),
                                                )
                                            } else {
                                                showToast(
                                                    context,
                                                    it.exceptionOrNull()?.message.toString()
                                                )
                                            }
                                        }
                                    })
                                } else {
                                    showToast(context, "Fill all the required fields!")
                                }
                            },
                            isEnable = registrationViewModel.validateWithRulesForRegister() && !requestInProgress
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        SimpleTextWithSpan(
                            "Already have an account? ",
                            "Login",
                            onTextClicked = {
                                onRegisterNavigate()
                            }, modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                    }
                }

            }
            if (requestInProgress) {
                ProgressIndicatorComponent(label = stringResource(id = R.string.registering_indicator))
            }
        }
    }
}

@Preview
@Composable
fun RegistrationScreenPreview() {
    RegistrationScreen(
        onRegisterNavigate = {},
        registrationViewModel = RegistrationViewModel(),
    )
}