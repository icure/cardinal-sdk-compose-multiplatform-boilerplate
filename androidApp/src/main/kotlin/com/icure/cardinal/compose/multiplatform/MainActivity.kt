package com.icure.cardinal.compose.multiplatform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.icure.cardinal.compose.multiplatform.context.PlatformContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        PlatformContext.setupValues(
            applicationContext = this.application,
            applicationId = BuildConfig.applicationId,
            processId = BuildConfig.processId,
            specId = BuildConfig.externalServicesSpecId
        )

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}