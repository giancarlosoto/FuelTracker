package com.fueltracker.app.voice

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.fueltracker.app.FuelTrackerApplication
import com.fueltracker.app.data.FuelEntry
import kotlinx.coroutines.launch

/**
 * Transparent activity that receives voice commands from Google Assistant
 * via App Actions deep links.
 *
 * Example voice commands:
 *   "Hey Google, registrar carga en FuelTracker"
 *   "Hey Google, registrar 100 soles de gasolina en FuelTracker"
 *
 * Deep link format:
 *   app://fueltracker/record?cost=100&unitPrice=17.50&odometer=15000
 */
class VoiceActionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val data = intent.data

        if (data == null) {
            Toast.makeText(this, "No se recibieron datos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        when (data.path) {
            "/record" -> handleRecordFuel(data)
            "/status" -> handleStatusQuery()
            else -> {
                Toast.makeText(this, "Comando no reconocido", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun handleRecordFuel(data: android.net.Uri) {
        val costStr = data.getQueryParameter("cost")
        val unitPriceStr = data.getQueryParameter("unitPrice")
        val odometerStr = data.getQueryParameter("odometer")

        val cost = costStr?.toDoubleOrNull()
        val unitPrice = unitPriceStr?.toDoubleOrNull()
        val odometer = odometerStr?.toDoubleOrNull()

        if (cost == null || unitPrice == null) {
            Toast.makeText(
                this,
                "Necesito el monto y el precio unitario. Ejemplo: 'registrar 100 soles a 17.50 por galón'",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        val gallonsAdded = cost / unitPrice
        val repository = (application as FuelTrackerApplication).repository

        lifecycleScope.launch {
            val entry = FuelEntry(
                totalCost = cost,
                unitPrice = unitPrice,
                quantityAdded = gallonsAdded,
                odometerKm = odometer ?: 0.0,
                fuelUnit = "gallon",
                isFullTank = true,
                dataSource = "voice"
            )

            repository.insertEntry(entry)

            Toast.makeText(
                this@VoiceActionActivity,
                "✅ Registrado: S/ %.2f — %.2f galones".format(cost, gallonsAdded),
                Toast.LENGTH_LONG
            ).show()

            finish()
        }
    }

    private fun handleStatusQuery() {
        val repository = (application as FuelTrackerApplication).repository

        lifecycleScope.launch {
            val efficiency = repository.calculateCurrentEfficiency()
            val costPerKm = repository.calculateCostPerKm()

            val message = buildString {
                append("FuelTracker — ")
                if (efficiency != null) {
                    append("Rendimiento: %.1f km/gal".format(efficiency))
                }
                if (costPerKm != null) {
                    append(" | Costo: S/ %.2f/km".format(costPerKm))
                }
                if (efficiency == null && costPerKm == null) {
                    append("Necesitas al menos 2 registros para ver estadísticas.")
                }
            }

            Toast.makeText(this@VoiceActionActivity, message, Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
