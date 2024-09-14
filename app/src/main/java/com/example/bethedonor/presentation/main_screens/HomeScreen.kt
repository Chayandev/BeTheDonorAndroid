package com.example.bethedonor.presentation.main_screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.collectIsDraggedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavHostController
import com.example.bethedonor.R
import com.example.bethedonor.ui.theme.bgDarkBlue
import com.example.bethedonor.ui.theme.bgDarkBlue2
import com.example.bethedonor.ui.theme.bloodRed2
import com.example.bethedonor.ui.theme.bloodTransparent3
import com.example.bethedonor.ui.theme.fadeBlue1
import com.example.bethedonor.ui.theme.fadeBlue11
import com.example.bethedonor.ui.theme.fadeBlueTransparent
import com.example.bethedonor.viewmodels.HomeViewModel
import com.example.bethedonor.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

data class CarouselItem(
    val id: Int,
    val description: String,
    val backgroundImage: Int
)

@Composable
fun HomeScreen(
    navController: NavHostController,
    innerPadding: PaddingValues,
    homeViewModel: HomeViewModel,
    sharedViewModel: SharedViewModel
) {
    Scaffold(topBar = { AppTopBar() }, containerColor = bgDarkBlue) { padding ->
        Surface(
            color = bgDarkBlue, modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Carousel(
                        listOf(
                            CarouselItem(
                                id = 1,
                                description = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                                backgroundImage = R.drawable.ic_blood_donation
                            ),
                            CarouselItem(
                                id = 2,
                                description = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                                backgroundImage = R.drawable.ic_blood_donation
                            ),
                            CarouselItem(
                                id = 3,
                                description = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                                backgroundImage = R.drawable.ic_blood_donation
                            )
                        )

                    )
                }
                item {
                    Text(
                        text = "The easiest way to save a life",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                item {
                    HorizontalScrollListOfSteps(
                        listOf(
                            CarouselItem(
                                id = 1,
                                description = "Lorem Ipsum is simply dummy",
                                backgroundImage = R.drawable.ic_step_1_donate
                            ),
                            CarouselItem(
                                id = 2,
                                description = "Lorem Ipsum is simply dummy",
                                backgroundImage = R.drawable.ic_step_1_donate
                            ),
                            CarouselItem(
                                id = 3,
                                description = "Lorem Ipsum is simply dummy",
                                backgroundImage = R.drawable.ic_step_1_donate
                            ),
                            CarouselItem(
                                id = 4,
                                description = "Lorem Ipsum is simply dummy",
                                backgroundImage = R.drawable.ic_step_1_donate
                            )
                        )
                    )
                }
                item { BloodDonorInfo() }
                item { CommunityImpactInfo() }
                item { Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding())) }
            }
        }
    }
}


@Composable
fun CommunityImpactInfo() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
        HeadingText(text = "Community Impact")
        val donorStatistics = listOf(
            "Active Donors" to "5M",
            "Lifetime Donations" to "10M",
            "Total Lives Saved" to "20M",
            "Average Age" to "35"
        )
        // Define the grid or row based on screen width
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            val screenWidth = LocalConfiguration.current.screenWidthDp.dp

            if (screenWidth > 600.dp) {
                // Large screen: single row layout
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    donorStatistics.forEach { (title, value) ->
                        DonorStatisticsCard(title = title, value = value)
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .height(280.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DonorStatisticsCard(
                            title = donorStatistics[0].first,
                            value = donorStatistics[0].second
                        )
                        DonorStatisticsCard(
                            title = donorStatistics[1].first,
                            value = donorStatistics[1].second
                        )
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DonorStatisticsCard(
                            title = donorStatistics[2].first,
                            value = donorStatistics[2].second
                        )
                        DonorStatisticsCard(
                            title = donorStatistics[3].first,
                            value = donorStatistics[3].second
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DonorStatisticsCard(title: String, value: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = bgDarkBlue2 // Example dark color, adjust to your design
        ),
        modifier = Modifier
            .size(180.dp) // Consistent size for grid cells

    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun BloodDonorInfo() {
    var selectedBloodGroup by remember { mutableStateOf("A+") }

    val bloodGroups = listOf("A+", "B+", "AB+", "A-", "B-", "AB-", "O+", "O-", "I don't know")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = bgDarkBlue2
            ),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Active donors",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "12,000",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Create a 3-column grid for blood groups
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                modifier = Modifier
                    .height(180.dp)
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center,
                userScrollEnabled = true
            ) {
                items(bloodGroups.size) { index ->
                    BloodGroupButton(
                        bloodGroup = bloodGroups[index],
                        isSelected = selectedBloodGroup == bloodGroups[index],
                        onClick = { selectedBloodGroup = bloodGroups[index] }
                    )
                }
            }
        }
    }
}

@Composable
fun BloodGroupButton(
    bloodGroup: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor =
        if (isSelected) bgDarkBlue2 else Color.Transparent // Selected background color
    val contentColor = if (isSelected) Color.White else fadeBlue1 // Selected text color

    Box(
        modifier = Modifier
            .padding(4.dp)
            .background(backgroundColor, shape = CircleShape)
            .clip(shape = CircleShape)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = bloodGroup,
            color = contentColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DescriptionText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = Color.White)
}

@Composable
fun HeadingText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = Color.White,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun HorizontalScrollListOfSteps(items: List<CarouselItem>) {
    LazyRow(
        state = rememberLazyListState(), horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(items = items, key = { item -> item.id }) { item ->
            DonationStepItem(item = item)
        }
    }
}

@Composable
fun DonationStepItem(item: CarouselItem) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(width = 120.dp, 150.dp)
        ) {
            Image(
                painter = painterResource(id = item.backgroundImage),
                contentDescription = "Background Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Text(
            text = item.description,
            color = Color.White,
            modifier = Modifier.width(120.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }

}

@Composable
fun Carousel(pages: List<CarouselItem>) {
    // State for the pager
    val autoScrollDuration: Long = 3000L
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pages.size })
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    if (isDragged.not()) {
        LaunchedEffect(key1 = pagerState.currentPage) {
            delay(autoScrollDuration)
            val nextPage = (pagerState.currentPage + 1)%pages.size
            coroutineScope.launch {
                    pagerState.animateScrollToPage(nextPage)
            }
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
            modifier = Modifier
                .fillMaxWidth()

        ) { page ->
            val index = page % pages.size
            CarouselItemCard(
                item = pages[index],
                modifier = Modifier.carouselTransition(index, pagerState = pagerState)
            )
        }
      PagerIndicator(pagerState = pagerState )
    }
}
@Composable
fun PagerIndicator(
    pagerState: PagerState
) {
    // Draw the indicators
    Row(
        Modifier
            .wrapContentHeight()
            .wrapContentWidth()
            .padding(8.dp)
            .clip(shape = RoundedCornerShape(8.dp))
            .background(fadeBlueTransparent),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pagerState.pageCount) { iteration ->
            val isCurrentPage = pagerState.currentPage == iteration
            val color = if (isCurrentPage) bloodRed2 else bloodTransparent3

            // Animated width and offset
            val width by animateDpAsState(
                targetValue = if (isCurrentPage) 16.dp else 8.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy, // Adjust for bounce effect
                    stiffness = Spring.StiffnessMedium
                ), label = "" // Adjust duration as needed
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .clip(CircleShape)
                    .background(color)
                    .width(width)
                    .height(8.dp)
                    .graphicsLayer(
                        scaleX = if (isCurrentPage) 1.2f else 1f,
                        scaleY = 1f,
                        alpha = if (isCurrentPage) 0.8f else 1f
                    )
            )
        }
    }
}

fun Modifier.carouselTransition(page: Int, pagerState: PagerState) =
    graphicsLayer {
        val pageOffset =
            ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

        val transformation =
            lerp(
                start = 0.7f,
                stop = 1f,
                fraction = 1f - pageOffset.coerceIn(0f, 1f)
            )
        alpha = transformation
        scaleY = transformation
    }

@Composable
fun CarouselItemCard(item: CarouselItem, modifier: Modifier) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(250.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image
            Image(
                painter = painterResource(id = item.backgroundImage),
                contentDescription = "Background Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black // Adjust alpha for better text visibility
                            ),
                            startY = 300f
                        )
                    )
            )

            // Text content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = item.description,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
private fun AppTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(fadeBlue11)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.icon),
            contentDescription = "Custom Image",
            modifier = Modifier.size(50.dp)
        )
        Text(
            text = stringResource(id = R.string.app_name_for_logo),
            color = bloodRed2,
            style = MaterialTheme.typography.titleLarge,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.SemiBold
        )
    }
}


