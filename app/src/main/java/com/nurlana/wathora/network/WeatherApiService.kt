package com.nurlana.wathora.network

import com.nurlana.wathora.model.WeatherForecastResponse
import com.nurlana.wathora.model.WeatherHourlyResponse
import com.nurlana.wathora.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String
    ): WeatherResponse

    @GET("forecast")
    suspend fun getWeatherForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String,
        @Query("forecast_days") forecastDays: Int
    ): WeatherForecastResponse

    @GET("forecast")
    suspend fun getHourlyWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String,
        @Query("forecast_days") forecastDays: Int
    ): WeatherHourlyResponse
}
