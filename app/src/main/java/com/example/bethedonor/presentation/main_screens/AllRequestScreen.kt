package com.example.bethedonor.presentation.main_screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.privacysandbox.tools.core.model.Types.unit
import com.example.bethedonor.R
import com.example.bethedonor.domain.model.RequestCardDetails
import com.example.bethedonor.ui.components.AllRequestCard
import com.example.bethedonor.ui.components.FilterItemComponent
import com.example.bethedonor.ui.components.Retry
import com.example.bethedonor.ui.components.SearchBarComponent
import com.example.bethedonor.presentation.temporay_screen.LoadingScreen
import com.example.bethedonor.ui.theme.bgDarkBlue
import com.example.bethedonor.ui.theme.fadeBlue11
import com.example.bethedonor.ui.theme.teal
import com.example.bethedonor.ui.theme.transparentGray
import com.example.bethedonor.utils.dateDiffInDays
import com.example.bethedonor.utils.formatDate
import com.example.bethedonor.constants.getCityList
import com.example.bethedonor.constants.getDistrictList
import com.example.bethedonor.constants.getPinCodeList
import com.example.bethedonor.constants.getStateDataList
import com.example.bethedonor.presentation.temporay_screen.NetworkFailureScreen
import com.example.bethedonor.presentation.temporay_screen.NoResultFoundScreen
import com.example.bethedonor.ui.theme.bloodRed2
import com.example.bethedonor.viewmodels.AllRequestViewModel
import com.example.bethedonor.viewmodels.SharedViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllRequestScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    allRequestViewModel: AllRequestViewModel,
    sharedViewModel: SharedViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by allRequestViewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    val onRefresh: () -> Unit = {
        scope.launch {
            allRequestViewModel.setRefresherStatusTrue()
            networkCall(
                allRequestViewModel = allRequestViewModel,
            )
        }
    }
    LaunchedEffect(uiState.isNetworkConnected) {
        if (uiState.isNetworkConnected && (uiState.retryFlag || !uiState.hasFetchedResult)) {
            networkCall(
                allRequestViewModel = allRequestViewModel,
            )
        }
    }
    Scaffold(
        topBar = {
            TopAppBarComponent(
                searchText = uiState.searchText,
                allRequestViewModel = allRequestViewModel,
                filterState = uiState.filterState,
                filterDistrict = uiState.filterDistrict,
                filterCity = uiState.filterCity,
                filterPin = uiState.filterPin,
                switchStatus = uiState.switchChecked
            )
        },
        containerColor = bgDarkBlue
    ) { padding ->
        Box(
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(color = bgDarkBlue) {
                uiState.allBloodRequestResponseList?.let { result ->
                    val bloodRequestsWithUsers = if (result.isSuccess) {
                        result.getOrNull()
                    } else {
                        null
                    }
                    if (bloodRequestsWithUsers != null && bloodRequestsWithUsers.isEmpty()) {
                        NoResultFoundScreen()
                        return@Surface
                    }
                    if (uiState.isFiltered) {
                        scope.launch {
                            lazyListState.scrollToItem(0)
                            allRequestViewModel.setIsFiltered(false)
                        }
                    }
//                    PullToRefreshBox(
//                        isRefreshing = uiState.isRefreshing, onRefresh = onRefresh,
//                        state = pullToRefreshState,
//                        indicator = {
//                            PullToRefreshDefaults.Indicator(
//                                state = pullToRefreshState,
//                                isRefreshing = uiState.isRefreshing,
//                                containerColor = fadeBlue11,
//                                color = bloodRed2,
//                                modifier = Modifier.align(Alignment.TopCenter),
//                            )
//                        }
//                    )
                    // {
                    bloodRequestsWithUsers?.let {
                        PullToRefreshBox(
                            isRefreshing = uiState.isRefreshing, onRefresh = onRefresh,
                            state = pullToRefreshState,
                            indicator = {
                                PullToRefreshDefaults.Indicator(
                                    state = pullToRefreshState,
                                    isRefreshing = uiState.isRefreshing,
                                    containerColor = fadeBlue11,
                                    color = bloodRed2,
                                    modifier = Modifier.align(Alignment.TopCenter).padding(
                                        if (uiState.isRefreshing) {
                                            innerPadding.calculateBottomPadding() + 20.dp
                                        } else 0.dp
                                    ),
                                )
                            }
                        ) {
                            LazyColumn(state = lazyListState) {
                                item {
                                    Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
                                }
                                items(
                                    items = bloodRequestsWithUsers,
                                    key = { requestWithUser -> requestWithUser.bloodRequest.id }
                                ) { requestWithUser ->
                                    val isDonor = remember { mutableStateOf(false) }
                                    val iSUserCreation = remember {
                                        mutableStateOf(false)
                                    }
                                    uiState.currentUserDetails?.let { userResult ->
                                        if (userResult.isSuccess) {
                                            userResult.getOrNull()?.let { userResponse ->
                                                isDonor.value =
                                                    userResponse.myProfile?.donates?.contains(
                                                        requestWithUser.bloodRequest.id
                                                    )
                                                        ?: false

                                                iSUserCreation.value =
                                                    requestWithUser.bloodRequest.userId == (userResponse.myProfile?.id
                                                        ?: false)
                                            }
                                        }
                                    }

                                    val cardDetails = RequestCardDetails(
                                        name = requestWithUser.user.name ?: "",
                                        emailId = requestWithUser.user.email ?: "",
                                        phoneNo = requestWithUser.user.phoneNumber ?: "",
                                        address = "${requestWithUser.bloodRequest.state}, ${requestWithUser.bloodRequest.district}, ${requestWithUser.bloodRequest.city}, ${requestWithUser.bloodRequest.pin}",
                                        exactPlace = requestWithUser.bloodRequest.donationCenter,
                                        bloodUnit = requestWithUser.bloodRequest.bloodUnit,
                                        bloodGroup = requestWithUser.bloodRequest.bloodGroup,
                                        noOfAcceptors = requestWithUser.bloodRequest.donors.size,
                                        dueDate = formatDate(requestWithUser.bloodRequest.deadline),
                                        postDate = dateDiffInDays(requestWithUser.bloodRequest.createdAt).toString(),
                                        isOpen = !requestWithUser.bloodRequest.isClosed,
                                        isAcceptor = isDonor.value,
                                        isMyCreation = iSUserCreation.value
                                    )
                                    AllRequestCard(
                                        details = cardDetails,
                                        allRequestViewModel,
                                        id = requestWithUser.bloodRequest.id,
                                        onDonationClickResponse = {
                                            Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    )
                                }
                                item {
                                    Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 8.dp))
                                }
                            }
                        } ?: NoResultFoundScreen()
                    }
                }
            }
            if (uiState.retryFlag) {
                Retry(message = stringResource(id = R.string.error), onRetry = {
                    networkCall(
                        allRequestViewModel = allRequestViewModel,
                    )
                })
            }
        }
    }
    if (uiState.isRequestFetching && !uiState.isRefreshing) {
        LoadingScreen()
    }
    if (!uiState.isNetworkConnected) {
        NetworkFailureScreen(onRetry = {
            networkCall(
                allRequestViewModel = allRequestViewModel,
            )
        })
    }
}
//}

@Composable
fun TopAppBarComponent(
    searchText: String,
    allRequestViewModel: AllRequestViewModel,
    filterState: String,
    filterDistrict: String,
    filterCity: String,
    filterPin: String,
    switchStatus: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(fadeBlue11),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(0.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SearchBarComponent(
                        searchQuery = searchText,
                        onSearchQueryChange = {
                            allRequestViewModel.onSearchTextChange(it)
                        },
                        modifier = Modifier.weight(1f)//.border(1.dp, bloodRed2, shape = RoundedCornerShape(8.dp)))
                    )
                    Switch(
                        checked = switchStatus,
                        onCheckedChange = {
                            allRequestViewModel.setSwitchChecked(it)
                        },
                        enabled = (!allRequestViewModel.uiState.collectAsState().value.isRequestFetching
                                && !allRequestViewModel.uiState.collectAsState().value.isRefreshing),
                        colors = SwitchDefaults.colors(
                            checkedBorderColor = Color.Transparent,
                            checkedThumbColor = Color.White,
                            checkedTrackColor = teal,
                            uncheckedBorderColor = transparentGray,
                            uncheckedTrackColor = Color.Transparent,
                            uncheckedThumbColor = transparentGray,
                            disabledUncheckedThumbColor = transparentGray,
                            disabledUncheckedTrackColor = Color.Transparent,
                            disabledUncheckedBorderColor = transparentGray,
                        ),
                        interactionSource = remember {
                            MutableInteractionSource()
                        }
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                FilterItemComponent(
                    label = stringResource(id = R.string.label_state),
                    options = getStateDataList(),
                    selectedValue = filterState,
                    onSelection = {
                        allRequestViewModel.updateFilterState(it)
                    }, onResetClick = {
                        allRequestViewModel.clearStateFilter()
                    })
                FilterItemComponent(
                    label = stringResource(id = R.string.label_district),
                    options = getDistrictList(filterState),
                    selectedValue = filterDistrict,
                    onSelection = {
                        allRequestViewModel.updateFilterDistrict(it)
                    }, onResetClick = {
                        allRequestViewModel.clearDistrictFilter()
                    })
                FilterItemComponent(
                    label = stringResource(id = R.string.label_pin),
                    options = getPinCodeList(filterState, filterDistrict),
                    selectedValue = filterPin,
                    onSelection = {
                        allRequestViewModel.updateFilterPin(it)
                    }, onResetClick = {
                        allRequestViewModel.clearPinFilter()
                    })
                FilterItemComponent(
                    label = stringResource(id = R.string.label_city),
                    options = getCityList(filterState, filterDistrict, filterPin),
                    selectedValue = filterCity,
                    onSelection = {
                        allRequestViewModel.updateFilterCity(it)
                    }, onResetClick = {
                        allRequestViewModel.clearCityFilter()
                    })
            }
        }
    }
}

fun networkCall(allRequestViewModel: AllRequestViewModel) {
    allRequestViewModel.parallelNetworkCall()
}

@Preview
@Composable
fun AllRequestScreenPreview() {
    AllRequestScreen(
        navController = NavController(LocalContext.current),
        innerPadding = PaddingValues(0.dp),
        allRequestViewModel = viewModel(),
        sharedViewModel = viewModel(),
    )
}
