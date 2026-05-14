package com.fueltracker.app.car

import android.location.Location
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.*
import com.fueltracker.app.FuelTrackerApplication
import com.fueltracker.app.data.FuelEntry
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.runBlocking
import java.util.Locale

/**
 * Screen displayed on Android Auto for recording a new fuel refueling.
 * Designed for use while the car is PARKED at a gas station.
 *
 * Uses a step-by-step approach with preset values (Android Auto
 * does not allow free-form text input on the car display for safety).
 *
 * Flow:
 * 1. Select total cost (preset amounts or custom via voice)
 * 2. Select unit price (recent prices or custom via voice)
 * 3. Enter/confirm odometer (auto-read from car if available)
 * 4. Confirm and save
 */
class RecordFuelScreen(
    carContext: CarContext,
    private val vehicleDataManager: VehicleDataManager?
) : Screen(carContext) {

    private val repository by lazy {
        (carContext.applicationContext as FuelTrackerApplication).repository
    }

    // Current step in the recording flow
    private var currentStep = Step.SELECT_COST

    // Collected data
    private var selectedCost: Double? = null
    private var selectedUnitPrice: Double? = null
    private var selectedOdometer: Double? = null
    private var currentLocation: Location? = null

    // Common fuel prices in Peru (Soles per gallon)
    private val commonPrices = listOf(16.50, 17.00, 17.50, 18.00, 18.50, 19.00)
    // Common refuel amounts (Soles)
    private val commonAmounts = listOf(50.0, 80.0, 100.0, 120.0, 150.0, 200.0)

    private enum class Step {
        SELECT_COST,
        SELECT_UNIT_PRICE,
        CONFIRM_ODOMETER,
        REVIEW_AND_SAVE
    }

    init {
        // Try to get current GPS location for the gas station
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(carContext)
            fusedClient.lastLocation.addOnSuccessListener { location ->
                currentLocation = location
            }
        } catch (e: SecurityException) {
            // Location permission not granted
        }

        // Try to pre-fill odometer from vehicle data
        vehicleDataManager?.vehicleData?.value?.odometerKm?.let {
            selectedOdometer = it
        }
    }

    override fun onGetTemplate(): Template {
        return when (currentStep) {
            Step.SELECT_COST -> buildCostSelectionTemplate()
            Step.SELECT_UNIT_PRICE -> buildUnitPriceSelectionTemplate()
            Step.CONFIRM_ODOMETER -> buildOdometerConfirmTemplate()
            Step.REVIEW_AND_SAVE -> buildReviewTemplate()
        }
    }

    // ========================
    // Step 1: Select Total Cost
    // ========================
    private fun buildCostSelectionTemplate(): Template {
        val listBuilder = ItemList.Builder()

        for (amount in commonAmounts) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(String.format(Locale.getDefault(), "S/ %.2f", amount))
                    .setOnClickListener {
                        selectedCost = amount
                        currentStep = Step.SELECT_UNIT_PRICE
                        invalidate()
                    }
                    .build()
            )
        }

        return ListTemplate.Builder()
            .setTitle("¿Cuánto pagaste?")
            .setHeaderAction(Action.BACK)
            .setSingleList(listBuilder.build())
            .build()
    }

    // ========================
    // Step 2: Select Unit Price
    // ========================
    private fun buildUnitPriceSelectionTemplate(): Template {
        val listBuilder = ItemList.Builder()

        for (price in commonPrices) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(String.format(Locale.getDefault(), "S/ %.2f / galón", price))
                    .setOnClickListener {
                        selectedUnitPrice = price
                        currentStep = Step.CONFIRM_ODOMETER
                        invalidate()
                    }
                    .build()
            )
        }

        return ListTemplate.Builder()
            .setTitle("Precio por galón")
            .setHeaderAction(Action.BACK)
            .setSingleList(listBuilder.build())
            .build()
    }

    // ========================
    // Step 3: Confirm Odometer
    // ========================
    private fun buildOdometerConfirmTemplate(): Template {
        val paneBuilder = Pane.Builder()

        val odometerSource = if (vehicleDataManager?.vehicleData?.value?.isDataAvailable == true) {
            "Leído del vehículo automáticamente"
        } else {
            "Ingresa por voz: 'Hey Google, actualizar kilometraje FuelTracker'"
        }

        val odometerText = selectedOdometer?.let {
            String.format(Locale.getDefault(), "%.0f km", it)
        } ?: "No disponible — usa el comando de voz"

        paneBuilder.addRow(
            Row.Builder()
                .setTitle("📏 Kilometraje Actual")
                .addText(odometerText)
                .build()
        )

        paneBuilder.addRow(
            Row.Builder()
                .setTitle("ℹ️ Fuente")
                .addText(odometerSource)
                .build()
        )

        // Quick adjust buttons
        if (selectedOdometer != null) {
            paneBuilder.addAction(
                Action.Builder()
                    .setTitle("✅ Confirmar")
                    .setOnClickListener {
                        currentStep = Step.REVIEW_AND_SAVE
                        invalidate()
                    }
                    .build()
            )
            paneBuilder.addAction(
                Action.Builder()
                    .setTitle("+10 km")
                    .setOnClickListener {
                        selectedOdometer = (selectedOdometer ?: 0.0) + 10.0
                        invalidate()
                    }
                    .build()
            )
        } else {
            // If no odometer data, allow skipping (will need to be entered on phone later)
            paneBuilder.addAction(
                Action.Builder()
                    .setTitle("⏭️ Saltar (editar después)")
                    .setOnClickListener {
                        selectedOdometer = 0.0
                        currentStep = Step.REVIEW_AND_SAVE
                        invalidate()
                    }
                    .build()
            )
        }

        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle("Kilometraje")
            .setHeaderAction(Action.BACK)
            .build()
    }

    // ========================
    // Step 4: Review & Save
    // ========================
    private fun buildReviewTemplate(): Template {
        val cost = selectedCost ?: 0.0
        val unitPrice = selectedUnitPrice ?: 1.0
        val odometer = selectedOdometer ?: 0.0
        val gallonsAdded = cost / unitPrice

        val paneBuilder = Pane.Builder()

        paneBuilder.addRow(
            Row.Builder()
                .setTitle("💵 Monto Total")
                .addText(String.format(Locale.getDefault(), "S/ %.2f", cost))
                .build()
        )

        paneBuilder.addRow(
            Row.Builder()
                .setTitle("⛽ Precio Unitario")
                .addText(String.format(Locale.getDefault(), "S/ %.2f / galón", unitPrice))
                .build()
        )

        paneBuilder.addRow(
            Row.Builder()
                .setTitle("🔢 Galones Agregados")
                .addText(String.format(Locale.getDefault(), "%.2f galones", gallonsAdded))
                .build()
        )

        paneBuilder.addRow(
            Row.Builder()
                .setTitle("📏 Kilometraje")
                .addText(String.format(Locale.getDefault(), "%.0f km", odometer))
                .build()
        )

        // Save button
        paneBuilder.addAction(
            Action.Builder()
                .setTitle("💾 Guardar Registro")
                .setOnClickListener {
                    saveEntry(cost, unitPrice, gallonsAdded, odometer)
                }
                .build()
        )

        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle("Confirmar Registro")
            .setHeaderAction(Action.BACK)
            .build()
    }

    private fun saveEntry(
        totalCost: Double,
        unitPrice: Double,
        gallonsAdded: Double,
        odometerKm: Double
    ) {
        val dataSource = if (vehicleDataManager?.vehicleData?.value?.isDataAvailable == true) {
            "auto"
        } else {
            "manual"
        }

        val entry = FuelEntry(
            totalCost = totalCost,
            unitPrice = unitPrice,
            quantityAdded = gallonsAdded,
            odometerKm = odometerKm,
            fuelUnit = "gallon",
            isFullTank = true,
            latitude = currentLocation?.latitude,
            longitude = currentLocation?.longitude,
            dataSource = dataSource
        )

        runBlocking {
            repository.insertEntry(entry)
        }

        CarToast.makeText(
            carContext,
            "✅ Registro guardado: %.2f gal".format(gallonsAdded),
            CarToast.LENGTH_LONG
        ).show()

        // Go back to dashboard
        screenManager.pop()
    }
}
