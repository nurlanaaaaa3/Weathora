package com.nurlana.wathora.network

import com.nurlana.wathora.model.WeatherForecastResponse
import com.nurlana.wathora.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code"
    ): WeatherResponse

    @GET("forecast")
    suspend fun getWeatherForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String = "weather_code,temperature_2m_max, temperature_2m_min",
        @Query("forecast_days") forecastDays: Int = 7
    ): WeatherForecastResponse
}