package com.fueltracker.app.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.hardware.CarHardwareManager
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import com.fueltracker.app.FuelTrackerApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.NumberFormat
import java.util.Locale

/**
 * Main dashboard screen displayed on the car's head unit (Android Auto).
 * Shows current fuel efficiency stats and provides a button to
 * register a new fuel refueling.
 */
class DashboardScreen(carContext: CarContext) : Screen(carContext) {

    private var vehicleDataManager: VehicleDataManager? = null
    private val repository by lazy {
        (carContext.applicationContext as FuelTrackerApplication).repository
    }
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "PE"))

    init {
        // Try to initialize vehicle data manager
        try {
            val hardwareManager = carContext.getCarService(CarHardwareManager::class.java)
            vehicleDataManager = VehicleDataManager(
                hardwareManager,
                carContext.mainExecutor
            )
            vehicleDataManager?.startListening()
        } catch (e: Exception) {
            // CarHardwareManager not available — will use manual input
        }
    }

    override fun onGetTemplate(): Template {
        val paneBuilder = Pane.Builder()

        // ---- Build information rows ----

        // Row 1: Current Efficiency
        val efficiency = runBlocking { repository.calculateCurrentEfficiency() }
        val efficiencyText = if (efficiency != null) {
            String.format(Locale.getDefault(), "%.1f km/gal", efficiency)
        } else {
            "Sin datos aún"
        }
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("⛽ Rendimiento Actual")
                .addText(efficiencyText)
                .build()
        )

        // Row 2: Cost per KM
        val costPerKm = runBlocking { repository.calculateCostPerKm() }
        val costText = if (costPerKm != null) {
            String.format(Locale.getDefault(), "S/ %.2f por km", costPerKm)
        } else {
            "Registra 2 cargas para calcular"
        }
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("💰 Costo por Kilómetro")
                .addText(costText)
                .build()
        )

        // Row 3: Vehicle Data Status
        val vehicleData = vehicleDataManager?.vehicleData?.value
        val connectionStatus = if (vehicleData?.isDataAvailable == true) {
            "✅ Conectado — Odómetro: ${String.format("%.0f", vehicleData.odometerKm)} km"
        } else {
            "📱 Modo manual — Datos del auto no disponibles"
        }
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("🚗 Estado del Vehículo")
                .addText(connectionStatus)
                .build()
        )

        // Row 4: Last refueling info
        val lastEntry = runBlocking { repository.getLatestEntry() }
        val lastRefuelText = if (lastEntry != null) {
            val date = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(java.util.Date(lastEntry.timestamp))
            "${String.format("%.1f", lastEntry.quantityAdded)} gal — $date"
        } else {
            "No hay registros"
        }
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("📋 Última Carga")
                .addText(lastRefuelText)
                .build()
        )

        // ---- Action Button: Register fuel ----
        paneBuilder.addAction(
            Action.Builder()
                .setTitle("🔵 Registrar Carga")
                .setOnClickListener {
                    screenManager.push(
                        RecordFuelScreen(
                            carContext,
                            vehicleDataManager
                        )
                    )
                }
                .build()
        )

        // ---- Action Button: Refresh ----
        paneBuilder.addAction(
            Action.Builder()
                .setTitle("🔄 Actualizar")
                .setOnClickListener { invalidate() }
                .build()
        )

        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle("FuelTracker")
            .setHeaderAction(Action.APP_ICON)
            .build()
    }

    override fun onDestroy(owner: androidx.lifecycle.LifecycleOwner) {
        super.onDestroy(owner)
        vehicleDataManager?.stopListening()
    }
}
