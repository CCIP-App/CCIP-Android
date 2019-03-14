package app.opass.ccip.application

import android.app.Application
import app.opass.ccip.util.BeaconReceiver
import com.onesignal.OneSignal
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.powersave.BackgroundPowerSaver
import org.altbeacon.beacon.startup.RegionBootstrap
import java.util.*

class CCIPApplication : Application() {
    companion object {
        private val SITCON_BEACON_ID = Region(
            "SITCON",
            Identifier.fromUuid(UUID.fromString("014567cf-d0ef-4b74-8161-47ce52f3df64")),
            Identifier.fromInt(8778),
            Identifier.fromInt(7887)
        )
    }

    private lateinit var backgroundScanService: RegionBootstrap
    private lateinit var backgroundPowerSaver: BackgroundPowerSaver

    override fun onCreate() {
        super.onCreate()
        OneSignal.startInit(this).init()

        // Beacon background service
        BeaconManager.getInstanceForApplication(this).apply {
            beaconParsers.clear()
            beaconParsers += BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24") //iBeacon
        }

        backgroundScanService = RegionBootstrap(this, BeaconReceiver(this), SITCON_BEACON_ID)

        backgroundPowerSaver = BackgroundPowerSaver(this)
    }
}
