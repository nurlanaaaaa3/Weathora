package com.nurlana.wathora.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nurlana.wathora.model.WeatherForecastResponse
import com.nurlana.wathora.model.WeatherHourlyResponse
import com.nurlana.wathora.model.WeatherResponse
import com.nurlana.wathora.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val _currentWeather = MutableStateFlow<WeatherResponse?>(null)
    val currentWeather: StateFlow<WeatherResponse?> = _currentWeather

    private val _forecast = MutableStateFlow<WeatherForecastResponse?>(null)
    val forecast: StateFlow<WeatherForecastResponse?> = _forecast

    private val _hourlyWeather = MutableStateFlow<WeatherHourlyResponse?>(null)
    val hourlyWeather: StateFlow<WeatherHourlyResponse?> = _hourlyWeather

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var lastLatitude: Double? = null
    private var lastLongitude: Double? = null

    fun fetchWeather(latitude: Double, longitude: Double) {
        lastLatitude = latitude
        lastLongitude = longitude
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _currentWeather.value = RetrofitInstance.api.getCurrentWeather(
                    latitude = latitude,
                    longitude = longitude,
                    current = "temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code,apparent_temperature,surface_pressure,visibility,uv_index"
                )
                _forecast.value = RetrofitInstance.api.getWeatherForecast(
                    latitude = latitude,
                    longitude = longitude,
                    daily = "weather_code,temperature_2m_max,temperature_2m_min",
                    forecastDays = 7
                )
                _hourlyWeather.value = RetrofitInstance.api.getHourlyWeather(
                    latitude = latitude,
                    longitude = longitude,
                    hourly = "temperature_2m,weather_code,precipitation_probability",
                    forecastDays = 1
                )
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat data cuaca: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        val lat = lastLatitude
        val lon = lastLongitude
        if (lat != null && lon != null) {
            fetchWeather(lat, lon)
        }
    }

    fun setError(message: String) {
        _errorMessage.value = message
    }
}
