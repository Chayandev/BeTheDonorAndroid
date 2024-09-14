package com.example.bethedonor.presentation.temporay_screen


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bethedonor.R
import com.example.bethedonor.ui.theme.Gray1
import com.example.bethedonor.ui.theme.Gray3
import com.example.bethedonor.ui.theme.bgDarkBlue
import com.example.bethedonor.ui.theme.bloodRed2
import com.example.bethedonor.ui.theme.fadeBlue11

@Composable
fun NetworkFailureScreen(onRetry: () -> Unit) {
    // Background gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(fadeBlue11,bgDarkBlue)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // WiFi Icon (Replace with your own vector resource)
            Image(
                painter = painterResource(id = R.drawable.ic_no_wifi), // Replace with your wifi icon resource
                contentDescription = "No Internet Icon",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Oops Text
            Text(
                text = "Oops!",
                fontSize = MaterialTheme.typography.displayMedium.fontSize,
                fontWeight = FontWeight.Bold,
                color = Gray1
            )

            Spacer(modifier = Modifier.height(10.dp))

            // No Internet Connection Text
            Text(
                text = "No Internet Connection",
                fontSize = 18.sp,
                color = Gray3
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Description Text
            Text(
                text = "Something went wrong. Please tap 'Try Again' or check your internet connection. We'll be with you in a moment!",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = { onRetry() },
                colors = ButtonDefaults.buttonColors(bloodRed2),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.wrapContentWidth().padding(end = 8.dp)
            ) {
                Text(text = "Try Again", color = Color.White)
            }
        }
    }
}

    @Preview(showBackground = true)
    @Composable
    fun NoInternetScreenPreview() {
        NetworkFailureScreen(onRetry = {})
    }

