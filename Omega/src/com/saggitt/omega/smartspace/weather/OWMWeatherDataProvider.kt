package com.saggitt.omega.smartspace.weather

import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.kwabenaberko.openweathermaplib.constants.Units
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper
import com.kwabenaberko.openweathermaplib.implementation.callbacks.CurrentWeatherCallback
import com.kwabenaberko.openweathermaplib.models.currentweather.CurrentWeather
import com.saggitt.omega.omegaApp
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.smartspace.OmegaSmartSpaceController
import com.saggitt.omega.smartspace.weather.icons.WeatherIconProvider
import com.saggitt.omega.util.Temperature
import com.saggitt.omega.util.checkLocationAccess
import kotlin.math.roundToInt

class OWMWeatherDataProvider(controller: OmegaSmartSpaceController) :
    OmegaSmartSpaceController.PeriodicDataProvider(controller),
    OmegaPreferences.OnPreferenceChangeListener, CurrentWeatherCallback {
    private val prefs = Utilities.getOmegaPrefs(context)
    private val owm by lazy { OpenWeatherMapHelper(prefs.smartspaceWeatherApiKey.onGetValue()) }
    private val iconProvider by lazy { WeatherIconProvider(context) }

    private val locationAccess get() = context.checkLocationAccess()
    private val locationManager: LocationManager? by lazy {
        if (locationAccess) {
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        } else null
    }

    init {
        prefs.addOnPreferenceChangeListener(
            this,
            "pref_weather_api_key",
            "pref_weather_city",
            "pref_weather_units"
        )
    }

    @SuppressLint("MissingPermission")
    override fun updateData() {
        // TODO: Create a search/dropdown for cities, make Auto the default
        if (prefs.smartspaceweatherCity.onGetValue() == "##Auto") {
            if (!locationAccess) {
                Utilities.requestLocationPermission(context.omegaApp.activityHandler.foregroundActivity)
                return
            } else {
                val locationProvider = locationManager?.getBestProvider(Criteria(), true)
                val location = locationProvider?.let { locationManager?.getLastKnownLocation(it) }
                if (location != null) {
                    owm.getCurrentWeatherByGeoCoordinates(
                        location.latitude,
                        location.longitude,
                        this
                    )
                }
            }
        } else {
            owm.getCurrentWeatherByCityName(prefs.smartspaceweatherCity.onGetValue(), this)
        }
    }

    override fun onSuccess(currentWeather: CurrentWeather) {
        val temp = currentWeather.main?.temp ?: return
        val icon = currentWeather.weather.getOrNull(0)?.icon ?: return
        updateData(
            OmegaSmartSpaceController.WeatherData(
                iconProvider.getIcon(icon),
                Temperature(
                    temp.roundToInt(),
                    if (Temperature.unitFromString(prefs.smartspaceWeatherUnit.onGetValue()) != Temperature.Unit.Fahrenheit) Temperature.Unit.Celsius
                    else Temperature.Unit.Fahrenheit
                ),
                "https://openweathermap.org/city/${currentWeather.id}"
            ), null
        )
    }

    override fun onFailure(throwable: Throwable?) {
        Log.w("OWM", "Updating weather data failed", throwable)
        if ((prefs.smartspaceWeatherApiKey.onGetValue() == context.getString(R.string.default_owm_key)
                    && !BuildConfig.APPLICATION_ID.contains("debug"))
            || throwable?.message == apiKeyError
        ) {
            Toast.makeText(context, R.string.owm_get_your_own_key, Toast.LENGTH_LONG).show()
        } else if (throwable != null) {
            Log.d("OWM", "Updating weather data failed", throwable)
            Toast.makeText(context, throwable.message, Toast.LENGTH_LONG).show()
        }
        updateData(null, null)
    }

    override fun stopListening() {
        super.stopListening()
        prefs.removeOnPreferenceChangeListener(
            this,
            "pref_weather_api_key",
            "pref_weather_city",
            "pref_weather_units"
        )
    }

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        if (key in arrayOf("pref_weather_api_key", "pref_weather_city", "pref_weather_units")) {
            if (key == "pref_weather_units") {
                owm.setUnits(
                    when (Temperature.unitFromString(prefs.smartspaceWeatherUnit.onGetValue())) {
                        Temperature.Unit.Celsius -> Units.METRIC
                        Temperature.Unit.Fahrenheit -> Units.IMPERIAL
                        else -> Units.METRIC
                    }
                )
            } else if (key == "pref_weather_api_key" && !force) {
                owm.setApiKey(prefs.smartspaceWeatherApiKey.onGetValue())
            }
            if (!force) updateNow()
        }
    }

    companion object {

        private const val apiKeyError = "UnAuthorized. Please set a valid OpenWeatherMap API KEY" +
                " by using the setApiKey method."
    }
}