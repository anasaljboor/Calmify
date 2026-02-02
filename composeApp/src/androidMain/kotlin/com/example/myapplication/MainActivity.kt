package com.example.myapplication

import AppRoot
import Wearable_Hub.AppGraph
import Wearable_Hub.SourceMode
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {

    private lateinit var graph: AppGraph

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->

            val granted = result.values.all { it }

            graph.sourceMode.value =
                if (granted) SourceMode.AUTO_PREFER_BLE else SourceMode.SIM_ONLY

            // ❌ DO NOT start here
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        graph = AppGraph(platformContext = applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        } else {
            graph.sourceMode.value = SourceMode.AUTO_PREFER_BLE
            // ❌ DO NOT start here
        }

        setContent {
            AppRoot(graph)
        }
    }
}
