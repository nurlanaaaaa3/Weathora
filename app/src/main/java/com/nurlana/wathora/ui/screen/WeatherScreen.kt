package com.nurlana.wathora.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nurlana.wathora.ui.theme.*
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

fun getWeatherGradient(code: Int): List<Color> {
    return when (code) {
        0 -> listOf(Color(0xFF1B72C0), Color(0xFF56CCF2))
        1, 2, 3 -> listOf(Color(0xFF4A6FA5), Color(0xFF89A4C7))
        45, 48 -> listOf(Color(0xFF6B7A8D), Color(0xFFB0BEC5))
        61, 63, 65, 80, 81, 82 -> listOf(Color(0xFF2C3E6B), Color(0xFF4A6FA5))
        95 -> listOf(Color(0xFF1A1A2E), Color(0xFF2C3E6B))
        else -> listOf(Color(0xFF1B72C0), Color(0xFF56CCF2))
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
    var cityName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val gradientColors = currentWeather?.let {
        getWeatherGradient(it.current.weatherCode)
    } ?: listOf(GradientStart, GradientEnd)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = gradientColors))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "WEATHORA",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhiteText,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cityInput,
                        onValueChange = { cityInput = it },
                        label = { Text("Cari Kota...", color = WhiteText) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WhiteText,
                            unfocusedBorderColor = WhiteText.copy(alpha = 0.6f),
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText
                        )
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                val coords = getCityCoordinates(cityInput)
                                if (coords != null) {
                                    cityName = cityInput
                                    viewModel.fetchWeather(coords.first, coords.second)
                                } else {
                                    viewModel.setError("Kota '$cityInput' tidak ditemukan")
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterVertically),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WhiteText.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cari", color = WhiteText)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = WhiteText,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            item {
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFFFFCDD2),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            item {
                if (currentWeather == null && !isLoading && errorMessage == null) {
                    Spacer(modifier = Modifier.height(60.dp))
                    Text(
                        text = "🌍",
                        fontSize = 80.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Selamat Datang!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cari nama kota untuk\nmelihat informasi cuaca",
                        fontSize = 16.sp,
                        color = WhiteText.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                currentWeather?.let { weather ->
                    Spacer(modifier = Modifier.height(16.dp))
                    if (cityName.isNotEmpty()) {
                        Text(
                            text = cityName.replaceFirstChar { it.uppercase() },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = WhiteText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = getWeatherEmoji(weather.current.weatherCode),
                        fontSize = 80.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${weather.current.temperature}°C",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = getWeatherDescription(weather.current.weatherCode),
                        fontSize = 20.sp,
                        color = WhiteText.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = WhiteText.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "💧", fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${weather.current.humidity}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WhiteText
                                )
                                Text(
                                    text = "Kelembapan",
                                    fontSize = 12.sp,
                                    color = WhiteText.copy(alpha = 0.8f)
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier
                                    .height(60.dp)
                                    .width(1.dp),
                                color = WhiteText.copy(alpha = 0.4f)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "💨", fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${weather.current.windSpeed} km/h",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WhiteText
                                )
                                Text(
                                    text = "Angin",
                                    fontSize = 12.sp,
                                    color = WhiteText.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                forecast?.let {
                    Text(
                        text = "Prakiraan 7 Hari",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
            }

            forecast?.let { forecastData ->
                items(forecastData.daily.dates.indices.toList()) { index ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = WhiteText.copy(alpha = 0.2f)
                        )
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
                                fontSize = 14.sp,
                                color = WhiteText,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = getWeatherEmoji(forecastData.daily.weatherCodes[index]),
                                fontSize = 24.sp
                            )
                            Text(
                                text = "${forecastData.daily.minTemps[index]}° / ${forecastData.daily.maxTemps[index]}°",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = WhiteText,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
