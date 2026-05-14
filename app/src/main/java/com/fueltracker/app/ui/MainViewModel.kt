package com.fueltracker.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fueltracker.app.FuelTrackerApplication
import com.fueltracker.app.data.FuelEntry
import com.fueltracker.app.data.FuelRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the main phone UI.
 * Provides reactive state for the dashboard, history list, and statistics.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FuelRepository =
        (application as FuelTrackerApplication).repository

    /** All fuel entries */
    val allEntries: StateFlow<List<FuelEntry>> = repository.allEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Recent entries (last 10) */
    val recentEntries: StateFlow<List<FuelEntry>> = repository.getRecentEntries(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Total spent */
    val totalSpent: StateFlow<Double> = repository.totalSpent
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /** Total fuel consumed */
    val totalFuelConsumed: StateFlow<Double> = repository.totalFuelConsumed
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /** Average unit price */
    val averageUnitPrice: StateFlow<Double> = repository.averageUnitPrice
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /** Entry count */
    val entryCount: StateFlow<Int> = repository.entryCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Current efficiency (km/gal) */
    private val _currentEfficiency = MutableStateFlow<Double?>(null)
    val currentEfficiency: StateFlow<Double?> = _currentEfficiency.asStateFlow()

    /** Cost per km */
    private val _costPerKm = MutableStateFlow<Double?>(null)
    val costPerKm: StateFlow<Double?> = _costPerKm.asStateFlow()

    // Dialog state for adding a new entry
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    init {
        refreshCalculations()
    }

    fun refreshCalculations() {
        viewModelScope.launch {
            _currentEfficiency.value = repository.calculateCurrentEfficiency()
            _costPerKm.value = repository.calculateCostPerKm()
        }
    }

    fun showAddDialog() {
        _showAddDialog.value = true
    }

    fun hideAddDialog() {
        _showAddDialog.value = false
    }

    fun addEntry(
        totalCost: Double,
        unitPrice: Double,
        odometerKm: Double,
        stationName: String? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val entry = FuelEntry(
                totalCost = totalCost,
                unitPrice = unitPrice,
                quantityAdded = totalCost / unitPrice,
                odometerKm = odometerKm,
                fuelUnit = "gallon",
                isFullTank = true,
                stationName = stationName,
                notes = notes,
                dataSource = "manual"
            )
            repository.insertEntry(entry)
            refreshCalculations()
            _showAddDialog.value = false
        }
    }

    fun deleteEntry(entry: FuelEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            refreshCalculations()
        }
    }

    /**
     * Calculate efficiency history for charting.
     * Returns a list of pairs: (entry index, km/gallon).
     */
    fun calculateEfficiencyHistory(entries: List<FuelEntry>): List<Pair<Int, Double>> {
        if (entries.size < 2) return emptyList()

        val sorted = entries.sortedBy { it.timestamp }
        val result = mutableListOf<Pair<Int, Double>>()

        for (i in 1 until sorted.size) {
            val prev = sorted[i - 1]
            val curr = sorted[i]
            val distance = curr.odometerKm - prev.odometerKm
            if (distance > 0 && curr.quantityAdded > 0) {
                result.add(i to (distance / curr.quantityAdded))
            }
        }
        return result
    }
}
