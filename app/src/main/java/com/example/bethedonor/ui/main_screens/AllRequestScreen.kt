package com.example.bethedonor.ui.main_screens

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.bethedonor.R
import com.example.bethedonor.domain.model.RequestCardDetails
import com.example.bethedonor.ui.components.AllRequestCard
import com.example.bethedonor.ui.components.FilterItemComponent
import com.example.bethedonor.ui.components.Retry
import com.example.bethedonor.ui.components.SearchBarComponent
import com.example.bethedonor.ui.temporay_screen.LoadingScreen
import com.example.bethedonor.ui.theme.bgDarkBlue
import com.example.bethedonor.ui.theme.fadeBlue11
import com.example.bethedonor.ui.theme.teal
import com.example.bethedonor.ui.theme.transparentGray
import com.example.bethedonor.utils.dateDiffInDays
import com.example.bethedonor.utils.formatDate
import com.example.bethedonor.utils.getCityList
import com.example.bethedonor.utils.getDistrictList
import com.example.bethedonor.utils.getPinCodeList
import com.example.bethedonor.utils.getStateDataList
import com.example.bethedonor.viewmodels.AllRequestViewModel
import com.example.bethedonor.viewmodels.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllRequestScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    allRequestViewModel: AllRequestViewModel,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allBloodRequestResponseList by allRequestViewModel.allBloodRequestResponseList.collectAsState(
        null
    )
    val currentUserDetails by allRequestViewModel.currentUserDetails.collectAsState(null)

    val isLoading by allRequestViewModel.isRequestFetching.collectAsState()
    val searchText by allRequestViewModel.searchText.collectAsState()
    val filterState by allRequestViewModel.filterState.collectAsState()
    val filterDistrict by allRequestViewModel.filterDistrict.collectAsState()
    val filterCity by allRequestViewModel.filterCity.collectAsState()
    val filterPin by allRequestViewModel.filterPin.collectAsState()
    val retryFlag by allRequestViewModel.retryFlag.collectAsState()
    val switchStatus by allRequestViewModel.switchChecked.collectAsState()
    //*** Recomposition Count ***//
    val isRefreshing by allRequestViewModel.isRefreshing.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    val lazyListState = rememberLazyListState()
    //**********
    val hasFetchedRequests by allRequestViewModel.hasFetchedResult.collectAsState()
    LaunchedEffect(hasFetchedRequests) {
        if (retryFlag || !hasFetchedRequests) {
            networkCall(
                allRequestViewModel = allRequestViewModel,
            )
            allRequestViewModel.setFetchedResult(true)
            allRequestViewModel.setRetryFlag(false)
        }
    }
//    // Use LaunchedEffect to reset the scroll position when data changes
//    LaunchedEffect(isRefreshing) {
//        // Reset the scroll state to the top when the list changes
//        lazyListState.scrollToItem(0)
//    }
    Scaffold(
        topBar = {
            TopAppBarComponent(
                searchText,
                allRequestViewModel,
                filterState,
                filterDistrict,
                filterCity,
                filterPin,
                switchStatus = switchStatus
            )
        },
        modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection),
        containerColor = bgDarkBlue
    ) { padding ->
        Box(
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(color = bgDarkBlue) {
                allBloodRequestResponseList?.let { result ->
                    val bloodRequestsWithUsers = if (result.isSuccess) {
                        result.getOrNull()
                    } else {
                        allRequestViewModel.setRetryFlag(true)
                        listOf()
                    }
                    bloodRequestsWithUsers?.let {
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
                                currentUserDetails?.let { userResult ->
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
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 8.dp))
                            }
                        }
                    } ?: EmptyStateComponent()
                }
            }
            if (retryFlag) {
                Retry(message = stringResource(id = R.string.retry), onRetry = {
                    allRequestViewModel.setRetryFlag(false)
                    networkCall(
                        allRequestViewModel = allRequestViewModel,
                    )
                })
            }
            if (isRefreshing)
                PullToRefreshContainer(
                    state = pullToRefreshState,
                    modifier = Modifier.padding(innerPadding.calculateBottomPadding() + 20.dp)
                )
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
                allRequestViewModel.setRefresherStatusTrue()
                networkCall(
                    allRequestViewModel = allRequestViewModel,
                )
            }
        }
    }
    if (isLoading && !isRefreshing) {
        LoadingScreen()
    }
}

@Composable
fun EmptyStateComponent() {
    Text(text = "Empty", color = Color.White)
}

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
                        enabled = !allRequestViewModel.isRequestFetching.collectAsState().value && !allRequestViewModel.isRefreshing.collectAsState().value,
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
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Launch both network calls in parallel using async
            val getAllBloodRequestDeferred = async {
                allRequestViewModel.getAllBloodRequest()
            }
            val fetchCurrentUserDetailsDeferred = async {
                allRequestViewModel.fetchCurrentUserDetails()
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

@Preview
@Composable
fun AllRequestScreenPreview() {
    AllRequestScreen(
        navController = NavController(LocalContext.current),
        innerPadding = PaddingValues(0.dp),
        allRequestViewModel = viewModel(),
        sharedViewModel = viewModel()
    )
}
