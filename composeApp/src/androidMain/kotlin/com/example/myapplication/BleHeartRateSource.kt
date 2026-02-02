package com.example.calmify

import Wearable_Hub.BiometricSample
import Wearable_Hub.WearableSource
import Wearable_Hub.WearableSourceType
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

class BleHeartRateSource(
    private val context: Context
) : WearableSource {

    override val type: WearableSourceType = WearableSourceType.BLE

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private val adapter: BluetoothAdapter? get() = bluetoothManager.adapter

    @RequiresPermission(
        allOf = [
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        ]
    )
    override fun stream(): Flow<BiometricSample> = callbackFlow {
        val btAdapter = adapter ?: run {
            // No BT on device/emulator → keep flow alive but do nothing
            // (You can log here if you want)
            awaitClose { }
            return@callbackFlow
        }

        val scanner = btAdapter.bluetoothLeScanner ?: run {
            // BLE scanner not available → keep flow alive but do nothing
            awaitClose { }
            return@callbackFlow
        }

        val HR_SERVICE = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")
        val HR_MEASUREMENT = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")
        val CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        var gatt: BluetoothGatt? = null
        var retryJob: Job? = null
        var scanning = false
        var connected = false

        fun parseHrAndRr(value: ByteArray): Pair<Int?, List<Int>> {
            if (value.isEmpty()) return null to emptyList()

            val flags = value[0].toInt() and 0xFF
            val hrIs16Bit = (flags and 0x01) != 0
            val rrPresent = (flags and 0x10) != 0
            val eePresent = (flags and 0x08) != 0

            var index = 1

            val hr = if (hrIs16Bit) {
                if (value.size >= index + 2) {
                    val lo = value[index].toInt() and 0xFF
                    val hi = value[index + 1].toInt() and 0xFF
                    index += 2
                    (hi shl 8) or lo
                } else null
            } else {
                if (value.size >= index + 1) {
                    val v = value[index].toInt() and 0xFF
                    index += 1
                    v
                } else null
            }

            if (eePresent && value.size >= index + 2) index += 2

            val rrMs = mutableListOf<Int>()
            if (rrPresent) {
                while (value.size >= index + 2) {
                    val lo = value[index].toInt() and 0xFF
                    val hi = value[index + 1].toInt() and 0xFF
                    val rr1024 = (hi shl 8) or lo
                    val ms = (rr1024 * 1000) / 1024
                    rrMs.add(ms)
                    index += 2
                }
            }
            return hr to rrMs
        }

        fun stopScanSafe(cb: ScanCallback) {
            if (!scanning) return
            scanning = false
            try { scanner.stopScan(cb) } catch (_: Throwable) {}
        }

        fun buildFilters(): List<ScanFilter> =
            listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(HR_SERVICE)).build())

        fun buildSettings(): ScanSettings =
            ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

        val gattCallback = object : BluetoothGattCallback() {

            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    connected = false
                    try { g.close() } catch (_: Throwable) {}
                    gatt = null
                    // Don’t close the flow — just allow retries
                    return
                }

                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        connected = true
                        g.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        connected = false
                        try { g.close() } catch (_: Throwable) {}
                        gatt = null
                        // Don’t close the flow — retry loop will scan again
                    }
                }
            }

            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) return

                val service = g.getService(HR_SERVICE) ?: return
                val ch = service.getCharacteristic(HR_MEASUREMENT) ?: return

                g.setCharacteristicNotification(ch, true)

                val desc = ch.getDescriptor(CLIENT_CONFIG) ?: return
                desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                g.writeDescriptor(desc)
            }

            override fun onCharacteristicChanged(
                g: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                if (characteristic.uuid != HR_MEASUREMENT) return
                val raw = characteristic.value ?: return
                val (hr, rr) = parseHrAndRr(raw)

                trySend(
                    BiometricSample(
                        tsMs = System.currentTimeMillis(),
                        hrBpm = hr,
                        rrMs = rr,
                        source = WearableSourceType.BLE
                    )
                )
            }
        }

        // We need scanCallback in scope for stopScanSafe
        val scanCallback = object : ScanCallback() {

            @RequiresPermission(
                allOf = [
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                ]
            )
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device ?: return

                // Stop scanning & connect
                stopScanSafe(this)

                // Close any old gatt before connecting again
                try { gatt?.close() } catch (_: Throwable) {}
                gatt = null

                gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
                } else {
                    device.connectGatt(context, false, gattCallback)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                // Don’t close the flow — the retry loop will try again
                scanning = false
            }
        }

        fun startScanSafe() {
            if (connected) return          // already connected
            if (scanning) return           // already scanning

            scanning = true
            try {
                scanner.startScan(buildFilters(), buildSettings(), scanCallback)
            } catch (_: Throwable) {
                scanning = false
            }
        }

        // ✅ Retry loop: scan in 15s windows, but NEVER close the flow if nothing is found
        retryJob = launch {
            while (isActive) {
                if (!connected) {
                    startScanSafe()
                    delay(15_000)
                    // If still not connected after 15s, stop scan and try again after a short pause
                    if (!connected) {
                        stopScanSafe(scanCallback)
                        delay(1_000)
                    }
                } else {
                    // If connected, just chill
                    delay(1_000)
                }
            }
        }

        // Start immediately
        startScanSafe()

        awaitClose {
            retryJob?.cancel()
            stopScanSafe(scanCallback)
            try { gatt?.close() } catch (_: Throwable) {}
            gatt = null
        }
    }
}
