package com.nurlana.wathora.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeather
)

data class CurrentWeather(
    @SerializedName("temperature_2m")
    val temperature: Double,
    @SerializedName("relative_humidity_2m")
    val humidity: Int,
    @SerializedName("wind_speed_10m")
    val windSpeed: Double,
    @SerializedName("weather_code")
    val weatherCode: Int,
    @SerializedName("apparent_temperature")
    val feelsLike: Double,
    @SerializedName("surface_pressure")
    val pressure: Double,
    @SerializedName("visibility")
    val visibility: Double,
    @SerializedName("uv_index")
    val uvIndex: Double
)

data class DailyWeather(
    @SerializedName("time")
    val dates: List<String>,
    @SerializedName("weather_code")
    val weatherCodes: List<Int>,
    @SerializedName("temperature_2m_max")
    val maxTemps: List<Double>,
    @SerializedName("temperature_2m_min")
    val minTemps: List<Double>
)

data class WeatherForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val daily: DailyWeather
)

data class HourlyWeather(
    @SerializedName("time")
    val times: List<String>,
    @SerializedName("temperature_2m")
    val temperatures: List<Double>,
    @SerializedName("weather_code")
    val weatherCodes: List<Int>,
    @SerializedName("precipitation_probability")
    val precipitationProbability: List<Int>
)

data class WeatherHourlyResponse(
    val latitude: Double,
    val longitude: Double,
    val hourly: HourlyWeather
)
