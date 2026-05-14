package com.fueltracker.app.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that abstracts the data source (Room database) from the UI layer.
 * All data operations go through here.
 */
class FuelRepository(private val fuelDao: FuelDao) {

    /** All entries as a reactive Flow */
    val allEntries: Flow<List<FuelEntry>> = fuelDao.getAllEntries()

    /** Total money spent on fuel */
    val totalSpent: Flow<Double?> = fuelDao.getTotalSpent()

    /** Total fuel consumed */
    val totalFuelConsumed: Flow<Double?> = fuelDao.getTotalFuelConsumed()

    /** Average price per unit */
    val averageUnitPrice: Flow<Double?> = fuelDao.getAverageUnitPrice()

    /** Total entry count */
    val entryCount: Flow<Int> = fuelDao.getEntryCount()

    /** Get recent entries */
    fun getRecentEntries(limit: Int = 10): Flow<List<FuelEntry>> {
        return fuelDao.getRecentEntries(limit)
    }

    /** Get entries within a date range */
    fun getEntriesBetween(startTime: Long, endTime: Long): Flow<List<FuelEntry>> {
        return fuelDao.getEntriesBetween(startTime, endTime)
    }

    /** Insert a new fuel entry and return its ID */
    suspend fun insertEntry(entry: FuelEntry): Long {
        return fuelDao.insertEntry(entry)
    }

    /** Update an existing entry */
    suspend fun updateEntry(entry: FuelEntry) {
        fuelDao.updateEntry(entry)
    }

    /** Delete an entry */
    suspend fun deleteEntry(entry: FuelEntry) {
        fuelDao.deleteEntry(entry)
    }

    /** Get a specific entry by ID */
    suspend fun getEntryById(id: Long): FuelEntry? {
        return fuelDao.getEntryById(id)
    }

    /** Get the most recent entry */
    suspend fun getLatestEntry(): FuelEntry? {
        return fuelDao.getLatestEntry()
    }

    /**
     * Calculate fuel efficiency between the two most recent entries.
     * Returns km per gallon (or km per liter depending on the unit).
     * Returns null if there are fewer than 2 entries.
     */
    suspend fun calculateCurrentEfficiency(): Double? {
        val lastTwo = fuelDao.getLastTwoEntries()
        if (lastTwo.size < 2) return null

        val current = lastTwo[0]  // Most recent
        val previous = lastTwo[1] // One before

        val distanceKm = current.odometerKm - previous.odometerKm
        if (distanceKm <= 0 || current.quantityAdded <= 0) return null

        return distanceKm / current.quantityAdded
    }

    /**
     * Calculate average cost per kilometer across all entries.
     * Returns null if there are fewer than 2 entries.
     */
    suspend fun calculateCostPerKm(): Double? {
        val lastTwo = fuelDao.getLastTwoEntries()
        if (lastTwo.size < 2) return null

        val current = lastTwo[0]
        val previous = lastTwo[1]

        val distanceKm = current.odometerKm - previous.odometerKm
        if (distanceKm <= 0) return null

        return current.totalCost / distanceKm
    }
}
