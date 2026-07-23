package com.icure.cardinal.compose.multiplatform.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.AppIntent
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.AppViewModel
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.WelcomeIntent
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.WelcomeViewModel
import com.icure.cardinal.sdk.CardinalSdk

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    sdkId: String,
    sdk: CardinalSdk,
    viewModel: WelcomeViewModel = viewModel(key = sdkId) { WelcomeViewModel(sdk) },
    appViewModel: AppViewModel
) {
    val busy by viewModel.busy.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Welcome") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                showMenu = false
                                appViewModel.processIntent(AppIntent.Logout)
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "You have logged in!",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Try some demo actions below.\nCheck the logcat/console for the output!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            ActionButton(
                text = "Create 1 demo patient",
                busy = busy,
                onClick = { viewModel.processIntent(WelcomeIntent.Demo.CreatePatients(1)) }
            )

            ActionButton(
                text = "Create 10 demo patient",
                busy = busy,
                onClick = { viewModel.processIntent(WelcomeIntent.Demo.CreatePatients(10)) }
            )

            ActionButton(
                text = "Get all demo patients",
                busy = busy,
                onClick = { viewModel.processIntent(WelcomeIntent.Demo.GetPatients) }
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    busy: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !busy,
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

