package com.example.bethedonor.presentation.main_screens

import PhoneNumberEditText
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DoNotDisturbAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Bloodtype
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Transgender
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bethedonor.R
import com.example.bethedonor.data.dataModels.UserProfile
import com.example.bethedonor.ui.components.AvailabilityCheckerField
import com.example.bethedonor.ui.components.ButtonComponent
import com.example.bethedonor.ui.components.EditText
import com.example.bethedonor.ui.components.ProgressIndicatorComponent
import com.example.bethedonor.ui.components.Retry
import com.example.bethedonor.ui.components.SelectStateDistrictCityField
import com.example.bethedonor.ui.components.SelectionField
import com.example.bethedonor.ui.components.WarningDialog
import com.example.bethedonor.ui.theme.activeColor1
import com.example.bethedonor.ui.theme.bgDarkBlue
import com.example.bethedonor.ui.theme.bloodRed2
import com.example.bethedonor.ui.theme.bloodTransparent2
import com.example.bethedonor.ui.theme.darkGray
import com.example.bethedonor.ui.theme.fadeBlue11
import com.example.bethedonor.ui.utils.commons.showToast
import com.example.bethedonor.ui.utils.uievent.RegistrationUIEvent
import com.example.bethedonor.utils.ValidationResult
import com.example.bethedonor.utils.formatDate
import com.example.bethedonor.constants.genderList
import com.example.bethedonor.constants.getCityList
import com.example.bethedonor.utils.getCountryCode
import com.example.bethedonor.constants.getDistrictList
import com.example.bethedonor.utils.getInitials
import com.example.bethedonor.utils.getPhoneNoWithoutCountryCode
import com.example.bethedonor.constants.getPinCodeList
import com.example.bethedonor.constants.getStateDataList
import com.example.bethedonor.viewmodels.ProfileViewModel
import com.example.bethedonor.viewmodels.SharedViewModel
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    innerPadding: PaddingValues,
    profileViewmodel: ProfileViewModel,
    sharedViewModel: SharedViewModel,
    onLogOutNavigate: () -> Unit,
    onEmailEditNavigate: () -> Unit,
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val profileResponse by profileViewmodel.profileResponse.collectAsState(null)
    val profileData = remember {
        mutableStateOf<UserProfile?>(null)
    }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var showBottomSheetForEditProfile by remember { mutableStateOf(false) }
    val retryFlag by profileViewmodel.retryFlag.collectAsState()
    val isRefreshing by profileViewmodel.isRefreshing.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    val selectedState by profileViewmodel.selectedState.collectAsState()
    val selectedDistrict by profileViewmodel.selectedDistrict.collectAsState()
    val selectedCity by profileViewmodel.selectedCity.collectAsState()
    val selectedPinCode by profileViewmodel.selectedPinCode.collectAsState()
    val availableToDonate by profileViewmodel.availableToDonate.collectAsState()
    //**********

    val hasFetchedProfile = profileViewmodel.getFetchedProfile()
    LaunchedEffect(Unit) {
        Log.d("retryFlagFromProfile",retryFlag.toString())
        if (retryFlag || !hasFetchedProfile) {
            networkCall(profileViewmodel)
            profileViewmodel.setFetchedProfile(true)
        }
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
                        color = bgDarkBlue
                    )
                    .nestedScroll(pullToRefreshState.nestedScrollConnection)
            ) {
                profileResponse?.let { result ->
                    profileData.value = if (result.getOrNull()?.myProfile != null) {
                        result.getOrNull()?.myProfile
                    } else {
                        profileViewmodel.setRetryFlag(true)
                        null
                    }
                    profileData.value?.let {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .verticalScroll(
                                    rememberScrollState()
                                )
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 30.dp,
                                    bottom = innerPadding.calculateBottomPadding() * 2
                                )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(bloodRed2, shape = RoundedCornerShape(60.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (profileData.value?.available == true) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val imageWidth = 80.dp.toPx()
                                            val radius = imageWidth / 2f
                                            val overlapAmount = radius / 3f
                                            val iconSize =
                                                overlapAmount * 1.5f // Reduce the size of the indicator

                                            val iconCenterX =
                                                (size.width / 2f) + (imageWidth / 2f) - overlapAmount
                                            val iconCenterY =
                                                (size.height / 2f) + (imageWidth / 2f) - overlapAmount

                                            val arcStartAngle =
                                                140f // Start angle for the arc in degrees
                                            val arcSweepAngle =
                                                2500f // Sweep angle for the arc in degrees

                                            val arcRect = androidx.compose.ui.geometry.Rect(
                                                left = (iconCenterX - iconSize / 2f),
                                                top = (iconCenterY - iconSize / 2f),
                                                right = (iconCenterX + iconSize / 2f),
                                                bottom = (iconCenterY + iconSize / 2f)
                                            )

                                            drawCircle(
                                                color = bloodRed2,
                                                radius = radius,
                                                center = Offset(size.width / 2f, size.height / 2f)
                                            )

                                            drawArc(
                                                color = bgDarkBlue,
                                                startAngle = arcStartAngle,
                                                sweepAngle = arcSweepAngle,
                                                useCenter = false,
                                                topLeft = arcRect.topLeft,
                                                size = Size(arcRect.width, arcRect.height),
                                                style = Stroke(width = 12f)
                                            )

                                            drawCircle(
                                                color = activeColor1,
                                                radius = iconSize / 2f,
                                                center = Offset(iconCenterX, iconCenterY)
                                            )
                                        }
                                    }
                                    // Green Dot
                                    Text(
                                        text = "${
                                            profileData.value?.name?.let { it1 ->
                                                getInitials(
                                                    it1
                                                )
                                            }
                                        }",
                                        fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }  // Green Dot positioned on top of the text

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier.weight(1f)

                                ) {
                                    TextComponent(
                                        value = profileData.value?.requests?.size ?: 0,
                                        label = "Requested"
                                    )
                                    TextComponent(
                                        value = profileData.value?.donates?.size ?: 0,
                                        label = "Donated"
                                    )
                                }

                            }
                            SpacerComponent(12.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BoldTextComponent(label = profileData.value?.name ?: "John Dao")
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    if (profileData.value?.available == false)
                                        Icon(
                                            //imageVector = Icons.Filled.ModeNight,
                                            imageVector = Icons.Filled.DoNotDisturbAlt,
                                            contentDescription = "Not Available",
                                            tint = bloodTransparent2
                                            // tint = moonNightColor,
                                            // modifier = Modifier.rotate(45F)
                                        )
                                    Text(
                                        text = if (profileData.value?.available == true) "Available" else "Not Available",
                                        color = if (profileData.value?.available == true) Color.White else Color.LightGray,
                                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                                    )
                                }
                            }
                            SpacerComponent(4.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = profileData.value?.email ?: "xyz@gmail.com",
                                    color = Color.LightGray,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Edit",
                                    color = bloodRed2,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        onEmailEditNavigate()
                                    }
                                )
                            }
                            SpacerComponent(16.dp)
                            ButtonElement(label = "Sign out",
                                onClick = {},
                                showDialog = true,
                                dialogTitle = "Confirm Logout",
                                dialogMessage = "Are you sure you want to logout? You will need to log in again to access your account.",
                                dialogIcon = Icons.AutoMirrored.Filled.Logout,
                                onConfirmAction = {
                                    coroutineScope.launch {
                                        profileViewmodel.logoutUser(onLogout = {
                                            onLogOutNavigate()
                                        })
                                        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                            )
                            SpacerComponent(dp = 24.dp)
                            BoldTextComponent(label = "Personal Information")
                            SpacerComponent(dp = 20.dp)
                            InformationComponent(
                                icon = Icons.Outlined.Home,
                                label = "Address",
                                value = "${profileData.value?.state}, ${profileData.value?.district}, ${profileData.value?.city}, ${
                                    profileData.value?.pin
                                }"
                            )
                            InformationComponent(
                                icon = Icons.Outlined.Phone,
                                label = "Phone",
                                value = profileData.value?.phoneNumber ?: "175483758"
                            )
                            InformationComponent(
                                icon = Icons.Outlined.Transgender,
                                label = "Gender",
                                value = profileData.value?.gender ?: "Male"
                            )
                            InformationComponent(
                                icon = Icons.Outlined.Bloodtype,
                                label = "Blood Group",
                                value = profileData.value?.bloodGroup ?: "B+"
                            )
                            InformationComponent(
                                icon = Icons.Outlined.DateRange,
                                label = "DOB",
                                value = formatDate(profileData.value?.dob ?: Date(0))
                            )
                            ButtonElement(label = "Edit profile", onClick = {
                                showBottomSheetForEditProfile = true
                            })
                            SpacerComponent(16.dp)
                            ButtonComponent(
                                isEnable = !profileViewmodel.requestInProgress.value,
                                buttonText = "Delete Account",
                                onButtonClick = {},
                                showDialog = true,
                                dialogTitle = "Confirm Deletion",
                                dialogMessage = "Are you sure you want to delete your account? This action is irreversible, and all your data will be permanently lost.",
                                dialogIcon = Icons.Filled.DeleteForever,
                                onConfirmAction = {
                                    profileViewmodel.deleteAccount() { result ->
                                        if (result.isSuccess) {
                                            // Handle the success case
                                            val response = result.getOrNull()
                                            Toast.makeText(
                                                context,
                                                response?.message ?: "Account Deleted",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // Proceed to logout after account deletion is successful
                                            coroutineScope.launch {
                                                profileViewmodel.logoutUser(onLogout = {
                                                    onLogOutNavigate()
                                                })
                                            }
                                        } else {
                                            val error = result.exceptionOrNull()
                                            Toast.makeText(
                                                context,
                                                error?.message ?: "Failed to delete account",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            )

                        }

                    }

                }
                LaunchedEffect(isRefreshing) {
                    if (isRefreshing) {
                        pullToRefreshState.startRefresh()
                    } else {
                        pullToRefreshState.endRefresh()
                    }
                }
                if (pullToRefreshState.isRefreshing) {
                    LaunchedEffect(true) {
                        profileViewmodel.setRefresherStatusTrue()
                        networkCall(profileViewmodel)
                    }
                }
                PullToRefreshContainer(
                    state = pullToRefreshState, modifier = Modifier.align(
                        Alignment.TopCenter
                    )
                )
            }

        }
        val recheckFiled by remember {
            mutableStateOf(false)
        }
        if (showBottomSheetForEditProfile) {
            profileViewmodel.setAllProfileDetails(
                profileData.value?.state?:"",
                profileData.value?.district?:"",
                profileData.value?.city?:"",
                profileData.value?.pin?:"",
                profileData.value?.available?:false
            )

            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheetForEditProfile = false
                },
                sheetState = sheetState,
                modifier = Modifier
                    .fillMaxSize(),
                containerColor = fadeBlue11,

                ) {
                val isFieldChanged = remember {
                    mutableStateOf(false)
                }
                LaunchedEffect(showBottomSheetForEditProfile) {
                    Log.d("modalSheetLaunchEffect", "InEffect")
                    profileViewmodel.onEvent(
                        RegistrationUIEvent.GenderValueChangeEvent(
                            profileData.value?.gender.toString()
                        )
                    )
                    profileViewmodel.onEvent(
                        RegistrationUIEvent.StateValueChangeEvent(
                            profileData.value?.state.toString()
                        )
                    )
                    profileViewmodel.onEvent(
                        RegistrationUIEvent.DistrictValueChangeEvent(
                            profileData.value?.district.toString()
                        )
                    )
                    profileViewmodel.onEvent(
                        RegistrationUIEvent.CityValueChangeEvent(
                            profileData.value?.city.toString()
                        )
                    )
                    profileViewmodel.onEvent(
                        RegistrationUIEvent.PinCodeValueChangeEvent(
                            profileData.value?.pin.toString()
                        )
                    )
                    profileViewmodel.onEvent(
                        RegistrationUIEvent.AvailabilityCheckerValueChangeEvent(
                            profileData.value?.available ?: false
                        )
                    )
                    isFieldChanged.value = false
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(
                            top = 16.dp,
                            bottom = innerPadding.calculateBottomPadding(),
                            start = 16.dp,
                            end = 16.dp
                        )
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            EditText(
                                label = stringResource(id = R.string.label_name),
                                value = profileData.value?.name.toString(),
                                labelIcon = Icons.Filled.Person,
                                onFiledValueChanged = {
                                    ValidationResult()
                                },
                                readOnly = true,
                                enable = false
                            )
                            EditText(
                                label = stringResource(id = R.string.label_emailId),
                                value = profileData.value?.email.toString(),
                                labelIcon = Icons.Filled.Email,
                                onFiledValueChanged = {
                                    ValidationResult()
                                },
                                readOnly = true
                            )
                            var code =
                                getCountryCode(profileData.value?.phoneNumber.toString())
                            Log.d("countryCode", code)
                            PhoneNumberEditText(
                                readOnly = true,
                                onFieldValueChanged = {
                                    profileViewmodel.onEvent(
                                        RegistrationUIEvent.PhoneNoChangeEvent(code + it)
                                    )
                                    profileViewmodel.updateProfileUiState.value.phoneNoErrorState
                                },
                                value = getPhoneNoWithoutCountryCode(profileData.value?.phoneNumber.toString()),
                                recheckField = recheckFiled,
                                // countryCode = code,
                                code = {
                                    code = it
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                            SelectionField(
                                options = genderList,
                                index = genderList.indexOf(profileData.value?.gender),
                                label = stringResource(id = R.string.label_gender),
                                onSelection = {
                                    isFieldChanged.value = true
                                    profileViewmodel.onEvent(
                                        RegistrationUIEvent.GenderValueChangeEvent(it)
                                    )
                                    profileViewmodel.updateProfileUiState.value.genderErrorState
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            SelectStateDistrictCityField(
                                label = stringResource(id = R.string.label_state),
                                options = getStateDataList(),
                                selectedValue =selectedState ,
                                onSelection = {
                                    isFieldChanged.value = true
                                    profileViewmodel.onEvent(
                                        RegistrationUIEvent.StateValueChangeEvent(
                                            it
                                        )
                                    )
                                    profileViewmodel.selectState(it)
                                    profileViewmodel.updateProfileUiState.value.stateErrorState
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
                                    isFieldChanged.value = true
                                    profileViewmodel.onEvent(
                                        RegistrationUIEvent.DistrictValueChangeEvent(
                                            it
                                        )
                                    )
                                    profileViewmodel.selectDistrict(it)
                                    profileViewmodel.updateProfileUiState.value.districtErrorState
                                },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                onSearchTextFieldClicked = {
                                    keyboardController?.show()
                                }
                            )
                            SelectStateDistrictCityField(
                                label = stringResource(id = R.string.label_pin),
                                options = getPinCodeList(
                                    selectedState = selectedState,
                                    selectedDistrict =selectedDistrict,
                                ),
                                selectedValue = selectedPinCode,
                                onSelection = {
                                    isFieldChanged.value = true
                                    profileViewmodel.onEvent(
                                        RegistrationUIEvent.PinCodeValueChangeEvent(
                                            it
                                        )
                                    )
                                    profileViewmodel.selectPin(it)
                                    profileViewmodel.updateProfileUiState.value.pinCodeErrorState
                                },
                                modifier = Modifier
                                    .fillMaxWidth(),
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
                                selectedValue =selectedCity,
                                onSelection = {
                                    isFieldChanged.value = true
                                    profileViewmodel.onEvent(
                                        RegistrationUIEvent.CityValueChangeEvent(
                                            it
                                        )
                                    )
                                    profileViewmodel.selectCity(it)
                                    profileViewmodel.updateProfileUiState.value.cityErrorState
                                },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                onSearchTextFieldClicked = {
                                    keyboardController?.show()
                                }
                            )

                            AvailabilityCheckerField(
                                value = availableToDonate,
                                onCheckerChange = {
                                    profileViewmodel.onEvent(
                                        RegistrationUIEvent.AvailabilityCheckerValueChangeEvent(it)
                                    )
                                    profileViewmodel.setAvailableToDonate(it)
                                    profileViewmodel.updateProfileUiState.value.checkedAvailabilityStatus
                                })

                            ButtonComponent(
                                buttonText = stringResource(id = R.string.apply),
                                onButtonClick = {
                                    if (isFieldChanged.value && !profileViewmodel.validateWithRulesForUpdate()) {
                                        showToast(
                                            context = context,
                                            context.getString(R.string.message)
                                        )
                                        return@ButtonComponent
                                    }
                                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                        if (!sheetState.isVisible) {
                                            showBottomSheetForEditProfile = false
                                        }
                                    }
                                    profileViewmodel.updateProfile(onUpdate = {
                                        if (it.first == "success") {
                                            coroutineScope.launch {
                                                networkCall(
                                                    profileViewmodel,
                                                )
                                            }
                                        }
                                        showToast(context = context, it.second)
                                    })
                                })
                        }
                    }
                }
            }
        }

        if (profileViewmodel.requestInProgress.value && !isRefreshing) {
            profileViewmodel.setRetryFlag(false)
            ProgressIndicatorComponent(label = stringResource(id = R.string.loading_indicator))
        }
        if (profileViewmodel.deletingAccountProgress.value) {
            profileViewmodel.setRetryFlag(false)
            ProgressIndicatorComponent(label = stringResource(id = R.string.delete_account_indicator))
        }
        if (profileViewmodel.updatingProfileInProgress.value) {
            profileViewmodel.setRetryFlag(false)
            ProgressIndicatorComponent(label = stringResource(id = R.string.updating_profile_indicator))
        }
        if (retryFlag) {
            Retry(message = stringResource(id = R.string.error), onRetry = {
                profileViewmodel.setRetryFlag(false)
                networkCall(
                    profileViewmodel,
                )
            })
        }
    }
}


private fun networkCall(
    profileViewmodel: ProfileViewModel,
) {
    profileViewmodel.getProfile() {}
}


@Composable
fun ButtonElement(
    label: String, onClick: () -> Unit,
    isEnable: Boolean = true,
    showDialog: Boolean = false,
    dialogTitle: String = "",
    dialogMessage: String = "",
    onConfirmAction: () -> Unit = {},
    dialogIcon: ImageVector = Icons.Filled.Warning,
) {
    var isDialogVisible by remember { mutableStateOf(false) }

    if (isDialogVisible && showDialog) {
        WarningDialog(
            icon = dialogIcon,
            dialogTitle = dialogTitle,
            dialogText = dialogMessage,
            onDismissRequest = { isDialogVisible = false },
            onConfirmation = {
                isDialogVisible = false
                onConfirmAction()
            }
        )
    }
    Button(
        onClick = {
            if (showDialog) {
                isDialogVisible = true
            } else {
                onClick()
            }
        },
        colors = ButtonColors(
            containerColor = darkGray,
            contentColor = Color.White,
            disabledContentColor = Color.White,
            disabledContainerColor = darkGray
        ), shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun InformationComponent(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(bottom = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(darkGray, shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = value,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun BoldTextComponent(label: String) {
    Text(
        text = label,
        fontSize = MaterialTheme.typography.titleLarge.fontSize,
        fontWeight = FontWeight.SemiBold,
        color = Color.White
    )
}

@Composable
fun SpacerComponent(dp: Dp) {
    Spacer(modifier = Modifier.height(dp))
}

@Composable
fun TextComponent(value: Int?, label: String) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            color = Color.White
        )
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        innerPadding = PaddingValues(0.dp),
        profileViewmodel = viewModel(),
        sharedViewModel = viewModel(),
        onLogOutNavigate = {
            //
        },
        onEmailEditNavigate = {
            //
        },
    )
}