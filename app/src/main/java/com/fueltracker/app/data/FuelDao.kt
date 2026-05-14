package com.fueltracker.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for fuel entries.
 * Provides all database operations for the fuel tracking history.
 */
@Dao
interface FuelDao {

    /** Get all fuel entries ordered by most recent first */
    @Query("SELECT * FROM fuel_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<FuelEntry>>

    /** Get the last N entries for recent history display */
    @Query("SELECT * FROM fuel_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEntries(limit: Int): Flow<List<FuelEntry>>

    /** Get a specific entry by ID */
    @Query("SELECT * FROM fuel_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): FuelEntry?

    /** Get the most recent entry (for calculating distance since last fill-up) */
    @Query("SELECT * FROM fuel_entries ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEntry(): FuelEntry?

    /** Get the two most recent entries (for calculating current efficiency) */
    @Query("SELECT * FROM fuel_entries ORDER BY timestamp DESC LIMIT 2")
    suspend fun getLastTwoEntries(): List<FuelEntry>

    /** Insert a new fuel entry */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: FuelEntry): Long

    /** Update an existing fuel entry */
    @Update
    suspend fun updateEntry(entry: FuelEntry)

    /** Delete a fuel entry */
    @Delete
    suspend fun deleteEntry(entry: FuelEntry)

    /** Get total amount spent on fuel */
    @Query("SELECT SUM(totalCost) FROM fuel_entries")
    fun getTotalSpent(): Flow<Double?>

    /** Get total fuel consumed */
    @Query("SELECT SUM(quantityAdded) FROM fuel_entries")
    fun getTotalFuelConsumed(): Flow<Double?>

    /** Get average unit price over all entries */
    @Query("SELECT AVG(unitPrice) FROM fuel_entries")
    fun getAverageUnitPrice(): Flow<Double?>

    /** Get entries within a date range */
    @Query("SELECT * FROM fuel_entries WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getEntriesBetween(startTime: Long, endTime: Long): Flow<List<FuelEntry>>

    /** Count total entries */
    @Query("SELECT COUNT(*) FROM fuel_entries")
    fun getEntryCount(): Flow<Int>
}
