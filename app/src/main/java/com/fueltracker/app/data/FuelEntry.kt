package com.fueltracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single fuel refueling entry.
 * Each time the user fills up their tank, a new FuelEntry is created.
 */
@Entity(tableName = "fuel_entries")
data class FuelEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Timestamp of the refueling (epoch milliseconds) */
    val timestamp: Long = System.currentTimeMillis(),

    /** Odometer reading at time of refueling (in kilometers) */
    val odometerKm: Double,

    /** Total amount paid for the fuel (in local currency, e.g. Soles) */
    val totalCost: Double,

    /** Unit price of fuel (per gallon or per liter) */
    val unitPrice: Double,

    /** Quantity of fuel added (calculated: totalCost / unitPrice) */
    val quantityAdded: Double,

    /** Unit of fuel measurement: "gallon" or "liter" */
    val fuelUnit: String = "gallon",

    /** Whether this was a full tank fill-up */
    val isFullTank: Boolean = true,

    /** Optional: GPS latitude of the gas station */
    val latitude: Double? = null,

    /** Optional: GPS longitude of the gas station */
    val longitude: Double? = null,

    /** Optional: Name or address of the gas station */
    val stationName: String? = null,

    /** Data source: "auto" (from CarHardwareManager), "voice" (Google Assistant), "manual" */
    val dataSource: String = "manual",

    /** Optional: Notes about this refueling */
    val notes: String? = null
)
