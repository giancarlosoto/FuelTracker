package com.fueltracker.app

import android.app.Application
import com.fueltracker.app.data.FuelDatabase
import com.fueltracker.app.data.FuelRepository

/**
 * Application class that initializes the database and repository
 * as singletons available throughout the app lifecycle.
 */
class FuelTrackerApplication : Application() {

    /** Lazy-initialized database instance */
    val database: FuelDatabase by lazy {
        FuelDatabase.getDatabase(this)
    }

    /** Lazy-initialized repository instance */
    val repository: FuelRepository by lazy {
        FuelRepository(database.fuelDao())
    }
}
