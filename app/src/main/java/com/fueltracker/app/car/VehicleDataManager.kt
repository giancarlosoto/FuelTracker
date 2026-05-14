package com.fueltracker.app.car

import android.util.Log
import androidx.car.app.hardware.CarHardwareManager
import androidx.car.app.hardware.common.CarValue
import androidx.car.app.hardware.common.OnCarDataAvailableListener
import androidx.car.app.hardware.info.EnergyLevel
import androidx.car.app.hardware.info.Mileage
import androidx.car.app.hardware.info.Model
import androidx.car.app.hardware.info.Speed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executor

/**
 * Manages reading vehicle telemetry data from the car's computer
 * via Android Auto's CarHardwareManager API.
 *
 * This is the "primary" data source. If the OEM (Toyota) blocks
 * these sensors, the data will remain null and the app will
 * fall back to manual/voice input.
 */
class VehicleDataManager(
    private val carHardwareManager: CarHardwareManager,
    private val executor: Executor
) {
    companion object {
        private const val TAG = "VehicleDataManager"
    }

    /** Data class holding all vehicle telemetry we care about */
    data class VehicleData(
        val odometerKm: Double? = null,
        val fuelLevelPercent: Float? = null,
        val speedKmh: Float? = null,
        val isDataAvailable: Boolean = false
    )

    private val _vehicleData = MutableStateFlow(VehicleData())
    val vehicleData: StateFlow<VehicleData> = _vehicleData.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    /**
     * Start listening to vehicle sensors.
     * Call this when the Android Auto session starts.
     */
    fun startListening() {
        Log.d(TAG, "Starting vehicle data listeners...")
        listenToMileage()
        listenToEnergyLevel()
        listenToSpeed()
    }

    /**
     * Stop listening to vehicle sensors.
     * Call this when the Android Auto session ends.
     */
    fun stopListening() {
        Log.d(TAG, "Stopping vehicle data listeners...")
        try {
            carHardwareManager.carInfo.removeMileageListener(mileageListener)
            carHardwareManager.carInfo.removeEnergyLevelListener(energyLevelListener)
            carHardwareManager.carInfo.removeSpeedListener(speedListener)
        } catch (e: Exception) {
            Log.w(TAG, "Error removing listeners: ${e.message}")
        }
        _isConnected.value = false
    }

    // ========================
    // Mileage (Odometer)
    // ========================

    private val mileageListener = OnCarDataAvailableListener<Mileage> { mileage ->
        try {
            val odometerValue = mileage.odometerMeters
            if (odometerValue.status == CarValue.STATUS_SUCCESS) {
                val odometerKm = (odometerValue.value ?: 0.0) / 1000.0
                Log.i(TAG, "✅ Odometer reading: $odometerKm km")
                _vehicleData.value = _vehicleData.value.copy(
                    odometerKm = odometerKm,
                    isDataAvailable = true
                )
                _isConnected.value = true
            } else {
                Log.w(TAG, "⚠️ Odometer status: ${odometerValue.status} (not available from this vehicle)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error reading odometer: ${e.message}")
        }
    }

    private fun listenToMileage() {
        try {
            carHardwareManager.carInfo.addMileageListener(executor, mileageListener)
            Log.d(TAG, "Mileage listener registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register mileage listener: ${e.message}")
        }
    }

    // ========================
    // Energy Level (Fuel)
    // ========================

    private val energyLevelListener = OnCarDataAvailableListener<EnergyLevel> { energyLevel ->
        try {
            val fuelPercent = energyLevel.fuelPercent
            if (fuelPercent.status == CarValue.STATUS_SUCCESS) {
                val level = fuelPercent.value
                Log.i(TAG, "✅ Fuel level: $level%")
                _vehicleData.value = _vehicleData.value.copy(
                    fuelLevelPercent = level,
                    isDataAvailable = true
                )
                _isConnected.value = true
            } else {
                Log.w(TAG, "⚠️ Fuel level status: ${fuelPercent.status} (not available from this vehicle)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error reading fuel level: ${e.message}")
        }
    }

    private fun listenToEnergyLevel() {
        try {
            carHardwareManager.carInfo.addEnergyLevelListener(executor, energyLevelListener)
            Log.d(TAG, "Energy level listener registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register energy level listener: ${e.message}")
        }
    }

    // ========================
    // Speed
    // ========================

    private val speedListener = OnCarDataAvailableListener<Speed> { speed ->
        try {
            val rawSpeed = speed.rawSpeedMetersPerSecond
            if (rawSpeed.status == CarValue.STATUS_SUCCESS) {
                val speedKmh = (rawSpeed.value ?: 0f) * 3.6f
                _vehicleData.value = _vehicleData.value.copy(
                    speedKmh = speedKmh,
                    isDataAvailable = true
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading speed: ${e.message}")
        }
    }

    private fun listenToSpeed() {
        try {
            carHardwareManager.carInfo.addSpeedListener(executor, speedListener)
            Log.d(TAG, "Speed listener registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register speed listener: ${e.message}")
        }
    }
}
