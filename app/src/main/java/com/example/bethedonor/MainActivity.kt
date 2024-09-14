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
import com.example.bethedonor.ui.theme.fadeBlue11
import com.example.bethedonor.constants.readJsonFromAssets
import com.example.bethedonor.constants.setAreaData
import com.example.bethedonor.utils.NetworkConnectivityMonitor
import com.example.bethedonor.viewmodels.LoginViewModel
import com.example.bethedonor.viewmodels.MainViewModel
import com.example.bethedonor.viewmodels.MainViewModelFactory
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    // Lazily initialize NetworkConnectivityMonitor
    private val networkMonitor by lazy { NetworkConnectivityMonitor(this@MainActivity) }

    // Initialize MainViewModel with a factory
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(application, networkMonitor)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load area data in a background thread
        loadAreaData()
        // Set content for the activity
        setContent {
            BeTheDonorTheme {
                // ViewModel initialization
                val loginViewModel: LoginViewModel = viewModel()
                val isUserLoggedIn = loginViewModel.isUserLoggedIn()

                // System UI controller to set the system bar color
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        fadeBlue11,
                        darkIcons = false
                    )
                }

                // Define the start destination based on login status
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = bgDarkBlue
                ) {
                    val startDestination = if (isUserLoggedIn) Destination.Home else Destination.Login
                    NavigationStack(
                        selectedDestination = startDestination,
                        navController = rememberNavController(),
                        mainViewModel = mainViewModel
                    )
                }
            }
        }
    }

    // Function to load area data asynchronously
    private fun loadAreaData() {
        lifecycleScope.launch {
            val areaData = withContext(Dispatchers.IO) {
                readJsonFromAssets(this@MainActivity, "Location.json")
            }
            areaData?.let {
                setAreaData(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the network callback to avoid memory leaks
        networkMonitor.unregisterCallback()
    }
}
