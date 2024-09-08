package com.example.bethedonor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.bethedonor.navigation.NavigationStack
import com.example.bethedonor.navigation.Destination
import com.example.bethedonor.ui.theme.BeTheDonorTheme
import com.example.bethedonor.ui.theme.bgDarkBlue
import com.example.bethedonor.ui.theme.fadeBlue1
import com.example.bethedonor.ui.theme.fadeBlue11
import com.example.bethedonor.ui.theme.fadeBlue2
import com.example.bethedonor.utils.readJsonFromAssets
import com.example.bethedonor.utils.setAreaData
import com.example.bethedonor.viewmodels.LoginViewModel
import com.example.bethedonor.viewmodels.MainViewModel
import com.example.bethedonor.viewmodels.MainViewModelFactory
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val areaData = withContext(Dispatchers.IO) {
                readJsonFromAssets(this@MainActivity, "Location.json")
            }
            areaData?.let {
                setAreaData(it)
            }
        }

        //    WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            BeTheDonorTheme {
                // Initialize the ViewModel
                val loginViewModel: LoginViewModel = viewModel()
                // Check if the user is logged in and determine the start destination
                val isUserLoggedIn = loginViewModel.isUserLoggedIn()
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        fadeBlue11,
                        darkIcons = false
                    )
                }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = bgDarkBlue
                ) {
                    // Navigate to the appropriate screen based on the user's login status
                    val selectedDestination =
                        if (isUserLoggedIn) Destination.Home else Destination.Login
                    NavigationStack(
                        selectedDestination = selectedDestination,
                        navController = rememberNavController(),
                        mainViewModel = mainViewModel
                    )
                }
            }
        }
    }

}
