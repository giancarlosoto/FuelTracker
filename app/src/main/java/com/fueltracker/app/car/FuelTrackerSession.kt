package com.fueltracker.app.car

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

/**
 * Represents a single session of the FuelTracker app on Android Auto.
 * A new session is created each time the user opens the app on the car's display.
 */
class FuelTrackerSession : Session() {

    override fun onCreateScreen(intent: Intent): Screen {
        return DashboardScreen(carContext)
    }
}
