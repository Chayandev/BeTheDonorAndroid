package com.example.bethedonor.presentation.main_screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bethedonor.R
import com.example.bethedonor.data.dataModels.Donor
import com.example.bethedonor.ui.components.AcceptorDetailsCard
import com.example.bethedonor.ui.components.ProgressIndicatorComponent
import com.example.bethedonor.ui.components.RequestHistoryCard
import com.example.bethedonor.ui.components.Retry
import com.example.bethedonor.ui.theme.bgDarkBlue
import com.example.bethedonor.ui.theme.bloodRed2
import com.example.bethedonor.ui.theme.fadeBlue11
import com.example.bethedonor.ui.theme.lightGray
import com.example.bethedonor.ui.utils.commons.showToast
import com.example.bethedonor.utils.dateDiffInDays
import com.example.bethedonor.utils.formatDate
import com.example.bethedonor.viewmodels.HistoryViewModel
import com.example.bethedonor.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class TabItem(
    val title: String
)

@Composable
fun HistoryScreen(
    navController: NavHostController,
    historyViewModel: HistoryViewModel,
    innerPadding: PaddingValues,
    sharedViewModel: SharedViewModel,
    onDonorScreenNavigate: (Donor) -> Unit
) {
    val context = LocalContext.current
    val tabItem = listOf(
        TabItem("Requests"),
        TabItem("Donations")
    )
    val pagerState = rememberPagerState {
        tabItem.size
    }
    var selectedTabIndex by remember {
        mutableIntStateOf(0)
    }


    LaunchedEffect(selectedTabIndex) {
        pagerState.scrollToPage(selectedTabIndex)
    }

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    historyViewModel.updateRecomposeTime()
    Box(
        modifier = Modifier
            .fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = bgDarkBlue),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = fadeBlue11,
                        indicator = { tabPositions ->
                            TabRowDefaults.PrimaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                width = LocalConfiguration.current.screenWidthDp.dp / 2,
                                height = 2.dp,
                                color = bloodRed2
                            )
                        },
                        divider = { HorizontalDivider(Modifier.height(1.dp), color = lightGray) }
                    ) {
                        tabItem.forEachIndexed { index, tab ->
                            Tab(
                                selected = index == selectedTabIndex,
                                onClick = { selectedTabIndex = index },
                                text = { Text(tab.title, color = Color.White) }
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { page ->
                        when (page) {
                            0 -> RequestScreen(
                                historyViewModel = historyViewModel,
                                innerPadding, onDonorScreenNavigate = {donor->
                                    onDonorScreenNavigate(donor)
                                }
                            )
                            //1 -> DonationScreen() // Create DonationScreen similar to RequestScreen
                        }
                    }
                }
            }
        }
    }
}

//@Composable
//fun DonationScreen() {
//    TODO("Not yet implemented")
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestScreen(historyViewModel: HistoryViewModel, innerPadding: PaddingValues,onDonorScreenNavigate: (Donor) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val isLoading by historyViewModel.isRequestFetching.collectAsState()
    val requestHistoryResponseList by historyViewModel.requestHistoryResponseList.collectAsState(
        null
    )
    var retryFlag by remember { mutableStateOf(false) }
    val deletingItemId = remember {
        mutableStateOf<String?>(null)
    }
    LaunchedEffect(Unit) {
        if (retryFlag || historyViewModel.shouldFetch())
            networkCall(historyViewModel = historyViewModel, id = 1)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                ProgressIndicatorComponent(label = stringResource(id = R.string.loading_indicator))
                retryFlag = false
            }

            retryFlag -> {
                Retry(message = stringResource(id = R.string.error), onRetry = {
                    retryFlag = false
                    networkCall(historyViewModel = historyViewModel, id = 1)
                })
            }

            else -> requestHistoryResponseList?.let { result ->
                val requestHistory = if (result.isSuccess) {
                    result.getOrNull()
                } else {
                    retryFlag = true
                    listOf()
                }
                requestHistory?.let {
                    LazyColumn(state = rememberLazyListState()) {
                        item {
                            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
                        }
                        items(
                            items = requestHistory,
                            key = { it.bloodRequest.id }
                        ) { requestHistory ->
                            // Check if the current item is being deleted
                            var isDeleting by remember { mutableStateOf(false) }

                            AnimatedVisibility(
                                visible = !isDeleting,
                                enter = fadeIn(),
                                exit = slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(200)
                                ) + fadeOut(), // Slide-out and fade-out effect
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RequestHistoryCard(
                                    historyViewModel = historyViewModel,
                                    id = requestHistory.bloodRequest.id,
                                    donationCenter = requestHistory.bloodRequest.donationCenter,
                                    state = requestHistory.bloodRequest.state,
                                    district = requestHistory.bloodRequest.district,
                                    pin = requestHistory.bloodRequest.pin,
                                    count = requestHistory.bloodRequest.donors.size,
                                    bloodGroup = requestHistory.bloodRequest.bloodGroup,
                                    bloodUnit = requestHistory.bloodRequest.bloodUnit,
                                    createdAt = dateDiffInDays(requestHistory.bloodRequest.createdAt).toString(),
                                    deadline = formatDate(requestHistory.bloodRequest.deadline),
                                    isClosed = requestHistory.bloodRequest.isClosed,
                                    onAcceptorIconClick = {
                                        showBottomSheet = true
                                        scope.launch {
                                            networkCall(
                                                historyViewModel,
                                                0,
                                                requestHistory.bloodRequest.id
                                            )
                                        }
                                    },
                                    onDeleteConfirmation = {
                                        Log.d("onDeleteConfirmation", "Clicked")
                                        deletingItemId.value=requestHistory.bloodRequest.id
                                        historyViewModel.deleteRequest(
                                            requestHistory.bloodRequest.id,
                                            onResponse = { message ->
                                                showToast(
                                                    context = context,
                                                    message = message.getOrNull()
                                                        ?: context.getString(R.string.error)
                                                )
                                                if (message.isSuccess && message.getOrNull().isNullOrEmpty()) {
                                                    // If the request was successfully deleted
                                                    scope.launch {
                                                        isDeleting =
                                                            true // Start the deletion animation
                                                        delay(300) // Give time for the animation to run
                                                        historyViewModel.updateAfterDeletion(deletingItemId.value)
                                                    }
                                                } else {
                                                    // If the deletion fails, do nothing
                                                    isDeleting = false // Ensure no animation occurs
                                                }

                                            }
                                        )
                                    },
                                    onToggleStatus = {
                                        showToast(context = context, message = it)
                                    }
                                )
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 16.dp))
                        }
                    }
                }

            }
        }
        if (historyViewModel.isDeletingRequest.collectAsState().value) {
            ProgressIndicatorComponent(label = stringResource(id = R.string.delete_request_indicator))
        }
    }
    if (showBottomSheet) {
        val isDonorListFetching by historyViewModel.isDonorListFetching.collectAsState()
        val donorListResponse by historyViewModel.donorListResponse.collectAsState(null)
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState,
            modifier = Modifier
                .fillMaxSize(),
            containerColor = fadeBlue11,

            ) {
            Box(contentAlignment = Alignment.Center) {
                donorListResponse?.let { response ->
                    if (response.isFailure || response.getOrNull()?.statusCode != stringResource(id = R.string.status_code_success)) {
                        showToast(context = context, message = stringResource(id = R.string.error))
                        return@let
                    }
                    val donors = response.getOrNull()?.donors
                    donors?.let {
                        LazyColumn {
                            items(items = donors, key = { it.phoneNumber }) { donor ->
                                AcceptorDetailsCard(donnerDetails = donor, onCall = { phoneNo ->
                                    moveToCallActivity(context, phoneNo)
                                }, onClick = {
                                    showBottomSheet=false
                                    onDonorScreenNavigate(donor)
                                })
                            }
                        }
                    }
                    if (donors.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.no_acceptors),
                                style = TextStyle(
                                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                                    color = Color.Gray, textAlign = TextAlign.Center
                                )
                            )
                        }

                    }
                }
                if (isDonorListFetching) {
                    ProgressIndicatorComponent(label = stringResource(id = R.string.loading_indicator))
                }
            }

        }
    }
}

fun moveToCallActivity(context: Context, phoneNo: String) {
    // Create an intent to open the dialer
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$phoneNo")
    }
    // Start the activity
    context.startActivity(intent)
}


fun networkCall(
    historyViewModel: HistoryViewModel,
    id: Int,
    requestId: String? = null
) {
    when (id) {
        1 -> {
            historyViewModel.fetchRequestHistory()
        }

        2 -> {

        }

        else -> {
            historyViewModel.fetchDonorList(requestId ?: "")
        }
    }
}
