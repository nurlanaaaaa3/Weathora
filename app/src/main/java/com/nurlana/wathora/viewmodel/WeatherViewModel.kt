package com.nurlana.wathora.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nurlana.wathora.model.WeatherForecastResponse
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _currentWeather.value = RetrofitInstance.api.getCurrentWeather(
                    latitude = latitude,
                    longitude = longitude,
                    current = "temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code"
                )
                _forecast.value = RetrofitInstance.api.getWeatherForecast(
                    latitude = latitude,
                    longitude = longitude,
                    daily = "weather_code,temperature_2m_max,temperature_2m_min",
                    forecastDays = 7
                )
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat data cuaca: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setError(message: String) {
        _errorMessage.value = message
    }
}
