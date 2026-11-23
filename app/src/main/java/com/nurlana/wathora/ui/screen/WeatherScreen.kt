package com.nurlana.wathora.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nurlana.wathora.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch

fun getWeatherDescription(code: Int): String {
    return when (code) {
        0 -> "Cerah"
        1, 2, 3 -> "Berawan"
        45, 48 -> "Berkabut"
        51, 53, 55 -> "Gerimis"
        61, 63, 65 -> "Hujan"
        71, 73, 75 -> "Hujan Salju"
        80, 81, 82 -> "Hujan Lebat"
        95 -> "Badai Petir"
        else -> "Tidak Diketahui"
    }
}

fun getWeatherEmoji(code: Int): String {
    return when (code) {
        0 -> "☀️"
        1, 2, 3 -> "⛅"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌦️"
        61, 63, 65 -> "🌧️"
        71, 73, 75 -> "❄️"
        80, 81, 82 -> "⛈️"
        95 -> "🌩️"
        else -> "🌈"
    }
}

suspend fun getCityCoordinates(cityName: String): Pair<Double, Double>? {
    return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=$cityName&count=1&language=id&format=json"
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.requestMethod = "GET"
            val response = connection.inputStream.bufferedReader().readText()
            val json = org.json.JSONObject(response)
            val results = json.optJSONArray("results")
            if (results != null && results.length() > 0) {
                val city = results.getJSONObject(0)
                val lat = city.getDouble("latitude")
                val lon = city.getDouble("longitude")
                Pair(lat, lon)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}


@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val currentWeather by viewModel.currentWeather.collectAsState()
    val forecast by viewModel.forecast.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var cityInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "🌤 WEATHORA",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = cityInput,
                    onValueChange = { cityInput = it },
                    label = { Text("Cari Kota...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Button(
                    onClick = {
                        scope.launch {
                            val coords = getCityCoordinates(cityInput)
                            if (coords != null) {
                                viewModel.fetchWeather(coords.first, coords.second)
                            } else {
                                viewModel.setError("Kota '$cityInput' tidak ditemukan")
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("Cari")
                }

            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        item {
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        item {
            currentWeather?.let { weather ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = getWeatherEmoji(weather.current.weatherCode),
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${weather.current.temperature}°C",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getWeatherDescription(weather.current.weatherCode),
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "💧", fontSize = 24.sp)
                                Text(text = "${weather.current.humidity}%")
                                Text(text = "Kelembapan", fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "💨", fontSize = 24.sp)
                                Text(text = "${weather.current.windSpeed} km/h")
                                Text(text = "Angin", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            forecast?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Prakiraan 7 Hari",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }

        forecast?.let { forecastData ->
            items(forecastData.daily.dates.indices.toList()) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = forecastData.daily.dates[index],
                            fontSize = 14.sp
                        )
                        Text(
                            text = getWeatherEmoji(forecastData.daily.weatherCodes[index]),
                            fontSize = 24.sp
                        )
                        Text(
                            text = "${forecastData.daily.minTemps[index]}° / ${forecastData.daily.maxTemps[index]}°",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
