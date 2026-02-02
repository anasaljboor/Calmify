package firebase.com.example.myapplication

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

@Composable
fun BlePermissionGate(
    onGranted: () -> Unit,
    onDenied: () -> Unit = {}
) {
    val context = LocalContext.current

    val neededPerms = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun hasAllPerms(): Boolean =
        neededPerms.all { p -> ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val ok = results.values.all { it }
        if (ok) onGranted() else onDenied()
    }

    LaunchedEffect(Unit) {
        if (hasAllPerms()) onGranted()
        else launcher.launch(neededPerms)
    }
}
