package com.example.bethedonor.presentation.main_screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bethedonor.R
import com.example.bethedonor.constants.bloodGroupList2
import com.example.bethedonor.constants.getCityList
import com.example.bethedonor.constants.getDistrictList
import com.example.bethedonor.constants.getPinCodeList
import com.example.bethedonor.constants.getStateDataList
import com.example.bethedonor.ui.components.*
import com.example.bethedonor.ui.theme.fadeBlue11
import com.example.bethedonor.ui.utils.commons.showToast
import com.example.bethedonor.ui.utils.uievent.RegistrationUIEvent
import com.example.bethedonor.utils.NetworkConnectivityMonitor
import com.example.bethedonor.viewmodels.CreateRequestViewModel
import com.example.bethedonor.viewmodels.SharedViewModel

@Composable
fun CreateRequestScreen(
    navController: NavController,
    innerPaddingValues: PaddingValues,
    onDone: () -> Unit,
    createRequestViewModel: CreateRequestViewModel,
    sharedViewModel: SharedViewModel,
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val bloodGroupsList = bloodGroupList2

    val selectedState by createRequestViewModel.selectedState.collectAsState()
    val selectedDistrict by createRequestViewModel.selectedDistrict.collectAsState()
    val selectedCity by createRequestViewModel.selectedCity.collectAsState()
    val selectedPinCode by createRequestViewModel.selectedPinCode.collectAsState()
    val requestInProgress by createRequestViewModel.requestInProgress.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = fadeBlue11
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = fadeBlue11)
                .padding(vertical = 20.dp)
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = innerPaddingValues.calculateBottomPadding()
                    )
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Request for Donation",
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                        fontWeight = FontWeight.SemiBold
                    ), color = Color.White
                )
                SpacerComponent(dp = 8.dp)
                Text(
                    text = "Fill out the form to request for new donations.",
                    style = TextStyle(fontSize = MaterialTheme.typography.titleMedium.fontSize),
                    color = Color.Gray
                )
                SpacerComponent(dp = 16.dp)
                EditText(
                    onFiledValueChanged = {
                        createRequestViewModel.onEvent(
                            RegistrationUIEvent.DonationCenterValueChangeEvent(it)
                        )
                        createRequestViewModel.newRequestUiState.value.donationCenterErrorState
                    }, label = stringResource(id = R.string.label_donation_center),
                    labelIcon = Icons.Filled.LocationCity
                )
                SelectStateDistrictCityField(
                    label = stringResource(id = R.string.label_state),
                    options = getStateDataList(),
                    selectedValue = selectedState,
                    onSelection = {
                        createRequestViewModel.onEvent(
                            RegistrationUIEvent.StateValueChangeEvent(it)
                        )
                        createRequestViewModel.selectState(it)
                        createRequestViewModel.newRequestUiState.value.stateErrorState
                    },
                    modifier = Modifier.fillMaxWidth(),
                    onSearchTextFieldClicked = {
                        keyboardController?.show()
                    }
                )
                SelectStateDistrictCityField(
                    label = stringResource(id = R.string.label_district),
                    options = getDistrictList(selectedState),
                    selectedValue = selectedDistrict,
                    onSelection = {
                        createRequestViewModel.onEvent(
                            RegistrationUIEvent.DistrictValueChangeEvent(it)
                        )
                        createRequestViewModel.selectDistrict(it)
                        createRequestViewModel.newRequestUiState.value.districtErrorState
                    },
                    modifier = Modifier.fillMaxWidth(),
                    onSearchTextFieldClicked = {
                        keyboardController?.show()
                    }
                )
                SelectStateDistrictCityField(
                    label = stringResource(id = R.string.label_pin),
                    options = getPinCodeList(
                        selectedState,
                       selectedDistrict
                    ),
                    selectedValue = selectedPinCode,
                    onSelection = {
                        createRequestViewModel.onEvent(
                            RegistrationUIEvent.PinCodeValueChangeEvent(it)
                        )
                        createRequestViewModel.selectPin(it)
                        createRequestViewModel.newRequestUiState.value.pinCodeErrorState
                    },
                    modifier = Modifier.fillMaxWidth(),
                    onSearchTextFieldClicked = {
                        keyboardController?.show()
                    }
                )
                SelectStateDistrictCityField(
                    label = stringResource(id = R.string.label_city),
                    options = getCityList(
                        selectedState, selectedDistrict, selectedPinCode
                    ),
                    selectedValue = selectedCity,
                    onSelection = {
                        createRequestViewModel.onEvent(
                            RegistrationUIEvent.CityValueChangeEvent(it)
                        )
                        createRequestViewModel.selectCity(it)
                        createRequestViewModel.newRequestUiState.value.cityErrorState
                    },
                    modifier = Modifier.fillMaxWidth(),
                    onSearchTextFieldClicked = {
                        keyboardController?.show()
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumericOnlyField(
                        onFiledValueChanged = {
                            createRequestViewModel.onEvent(
                                RegistrationUIEvent.BloodUnitValueChangeEvent(it)
                            )
                            createRequestViewModel.newRequestUiState.value.bloodUnitErrorState
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                        label = stringResource(id = R.string.label_blood_unit)
                    )
                    SelectionField(
                        options = bloodGroupsList,
                        label = stringResource(id = R.string.label_blood_group),
                        onSelection = {
                            createRequestViewModel.onEvent(
                                RegistrationUIEvent.BloodGroupValueChangeEvent(it)
                            )
                            createRequestViewModel.newRequestUiState.value.bloodGroupErrorState
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    )
                }
                CalendarSelectField(
                    onFieldValueChanged = {
                        createRequestViewModel.onEvent(
                            RegistrationUIEvent.DateValueChangeEvent(it)
                        )
                        createRequestViewModel.newRequestUiState.value.deadLineErrorState
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(id = R.string.label_deadline)
                )
                SpacerComponent(dp = 20.dp)
                ButtonComponent(
                    onButtonClick = {
                        if (!createRequestViewModel.validateWithRulesForNewRequest()) {
                            showToast(context, context.getString(R.string.message))
                            return@ButtonComponent
                        }
                        createRequestViewModel.createNewBloodRequest(
                            onCreated = { response ->
                                onDone()
                                showToast(context, response.message.toString())
                            })

                    }, buttonText = stringResource(id = R.string.send_request),
                    isEnable = !requestInProgress
                )

                if (requestInProgress) {
                    ProgressIndicatorComponent(label = stringResource(id = R.string.creating_indicator))
                }
            }
        }
    }
}
