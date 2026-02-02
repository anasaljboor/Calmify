package com.example.calmify

import android.Manifest
import android.os.Build
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun rememberBlePermissionsState(): State<Boolean> {
    val context = LocalContext.current

    val needed = remember {
        buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                // Pre-Android 12
                add(Manifest.permission.ACCESS_FINE_LOCATION)
                // (BLUETOOTH / BLUETOOTH_ADMIN are normal perms pre-12, no runtime request)
            }
        }
    }

    fun hasAll(): Boolean =
        needed.all { p -> ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED }

    val grantedState = remember { mutableStateOf(hasAll()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        grantedState.value = result.values.all { it }
    }

    // Re-check if user returns from settings, etc.
    LaunchedEffect(Unit) {
        grantedState.value = hasAll()
    }

    // Expose a way to trigger requesting permissions
    PermissionRequestBus.current = {
        launcher.launch(needed.toTypedArray())
    }

    return grantedState
}

private object PermissionRequestBus {
    var current: (() -> Unit)? = null
}

@Composable
fun RequestBlePermissionsButton(
    enabled: Boolean = true,
    text: String = "Grant Bluetooth Permissions"
) {
    androidx.compose.material3.Button(
        enabled = enabled,
        onClick = { PermissionRequestBus.current?.invoke() }
    ) {
        androidx.compose.material3.Text(text)
    }
}
