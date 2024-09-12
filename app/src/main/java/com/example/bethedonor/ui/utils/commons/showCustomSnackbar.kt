package com.example.bethedonor.ui.utils.commons

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun CustomSnackBar(
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    message: String,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector? = null, // Optional icon parameter
    iconTint: Color = Color.White,
    messageColor: Color = Color.White
) {
    SnackbarHost(
        hostState = snackBarHostState,
        modifier = modifier
    ) { data ->
        Snackbar(
            containerColor = backgroundColor, // Set the background color
            contentColor = messageColor, // Set the message color
            content = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = "Snackbar Icon",
                            tint = iconTint,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Text(text = data.visuals.message, color = messageColor)
                }
            }
        )
    }
}
