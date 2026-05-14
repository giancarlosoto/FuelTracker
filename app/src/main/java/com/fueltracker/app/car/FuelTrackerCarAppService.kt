package com.fueltracker.app.car

import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/**
 * Entry point for the Android Auto projected experience.
 * This service is declared in the AndroidManifest and is what
 * Android Auto discovers when the phone is connected to the car.
 */
class FuelTrackerCarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator {
        // In production, validate against known Android Auto host signatures.
        // During development, allow all hosts for testing with DHU.
        return if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        } else {
            HostValidator.ALLOW_ALL_HOSTS_VALIDATOR // TODO: Restrict in production
        }
    }

    override fun onCreateSession(): Session {
        return FuelTrackerSession()
    }
}
