package com.example.bethedonor.presentation.main_screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Bloodtype
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Transgender
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bethedonor.data.dataModels.Donor
import com.example.bethedonor.data.dataModels.UserProfile
import com.example.bethedonor.ui.components.ButtonComponent
import com.example.bethedonor.ui.theme.Gray3
import com.example.bethedonor.ui.theme.bgDarkBlue
import com.example.bethedonor.ui.theme.bloodRed2
import com.example.bethedonor.utils.getInitials


@Composable
fun DonorProfileScreen(
    name: String,
    email: String,
    state: String,
    city: String,
    district: String,
    pin: String,
    phoneNumber: String,
    bloodGroup: String
) {
    Scaffold(
        topBar = { TopBarComponent() },
        modifier = Modifier.fillMaxSize(),
        containerColor = bgDarkBlue
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding() * 3/2, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(bloodRed2, shape = RoundedCornerShape(60.dp)),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = getInitials(
                            name
                        ),
                        fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$state, $district",
                        style = MaterialTheme.typography.titleMedium,
                        color = Gray3
                    )
                    Text(
                        text = "$city, $pin",
                        style = MaterialTheme.typography.titleMedium,
                        color = Gray3
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                InformationComponent(
                    icon = Icons.Outlined.Phone,
                    label = "Phone",
                    value = phoneNumber
                )
                InformationComponent(
                    icon = Icons.Outlined.Transgender,
                    label = "Gender",
                    value = "NA"
                )
                InformationComponent(
                    icon = Icons.Outlined.Bloodtype,
                    label = "Blood Group",
                    value = bloodGroup
                )
                InformationComponent(
                    icon = Icons.Filled.Favorite,
                    label = "Acknowledgements",
                    value = "89"
                )
            }
            ButtonComponent(buttonText = "Acknowledge", onButtonClick = {
                //Acknowledge
            })
        }
    }
}

@Composable
fun TopBarComponent() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = "back",
            tint = Color.White,
            modifier = Modifier.size(30.dp)
        )
        Text(
            text = "Donor Profile",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
    }
}
