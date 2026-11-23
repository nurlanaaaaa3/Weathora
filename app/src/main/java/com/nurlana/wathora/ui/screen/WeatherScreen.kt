package com.nurlana.wathora.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nurlana.wathora.R
import com.nurlana.wathora.ui.theme.*
import com.nurlana.wathora.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch

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
fun getWeatherDescription(code: Int): String {
    return when (code) {
        0 -> stringResource(R.string.weather_sunny)
        1, 2, 3 -> stringResource(R.string.weather_cloudy)
        45, 48 -> stringResource(R.string.weather_foggy)
        51, 53, 55 -> stringResource(R.string.weather_drizzle)
        61, 63, 65 -> stringResource(R.string.weather_rain)
        71, 73, 75 -> stringResource(R.string.weather_snow)
        80, 81, 82 -> stringResource(R.string.weather_heavy_rain)
        95 -> stringResource(R.string.weather_storm)
        else -> stringResource(R.string.weather_unknown)
    }
}

@Composable
fun WeatherInfoCard(
    emoji: String,
    label: String,
    value: String,
    description: String = "",
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            },
            title = {
                Text(
                    text = "$emoji $label",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = value,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = description)
                }
            }
        )
    }

    Card(
        modifier = modifier.clickable { showDialog = true },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = WhiteText.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = WhiteText.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WhiteText
            )
        }
    }
}

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = viewModel(),
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    onChangeLanguage: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val currentWeather by viewModel.currentWeather.collectAsState()
    val forecast by viewModel.forecast.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val hourlyWeather by viewModel.hourlyWeather.collectAsState()

    var cityInput by remember { mutableStateOf("") }
    var cityName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val gradientColors = if (isDarkMode) {
        listOf(Color(0xFF0D1B2A), Color(0xFF1B2B3A))
    } else {
        listOf(Color(0xFF1A9ED4), Color(0xFF3DB8E8))
    }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (cityName.isEmpty()) "WEATHORA" else cityName.replaceFirstChar { it.uppercase() },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = WhiteText
                        )
                        if (cityName.isNotEmpty()) {
                            Text(
                                text = "📍 $cityName",
                                fontSize = 14.sp,
                                color = WhiteText.copy(alpha = 0.7f)
                            )
                        }
                    }
                    var showSettings by remember { mutableStateOf(false) }

                    if (showSettings) {
                        AlertDialog(
                            onDismissRequest = { showSettings = false },
                            confirmButton = {
                                TextButton(onClick = { showSettings = false }) {
                                    Text("Tutup")
                                }
                            },
                            title = {
                                Text(
                                    text = "⚙️ Pengaturan",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            text = {
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Bahasa",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                onChangeLanguage("id")
                                                showSettings = false
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF1A9ED4)
                                            )
                                        ) {
                                            Text("🇮🇩 Indonesia")
                                        }
                                        Button(
                                            onClick = {
                                                onChangeLanguage("en")
                                                showSettings = false
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF1A9ED4)
                                            )
                                        ) {
                                            Text("🇬🇧 English")
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Tampilan",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            onToggleDarkMode()
                                            showSettings = false
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF1A9ED4)
                                        )
                                    ) {
                                        Text(
                                            text = if (isDarkMode) "☀️ Mode Terang" else "🌙 Mode Gelap"
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.refresh()
                                            showSettings = false
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF1A9ED4)
                                        )
                                    ) {
                                        Text("🔄 Refresh Data")
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            showSettings = false
                                            onLogout()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Red
                                        )
                                    ) {
                                        Text("🚪 Logout", color = Color.White)
                                    }
                                }
                            }
                        )
                    }

                    IconButton(onClick = { showSettings = true }) {
                        Text(text = "⚙️", fontSize = 24.sp)
                    }

                }
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
                        label = { Text(stringResource(R.string.search_hint), color = WhiteText) },
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
                        Text(stringResource(R.string.search_button), color = WhiteText)
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
                        text = stringResource(R.string.welcome_title),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.welcome_subtitle),
                        fontSize = 16.sp,
                        color = WhiteText.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                currentWeather?.let { weather ->
                    Text(
                        text = getWeatherEmoji(weather.current.weatherCode),
                        fontSize = 80.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${weather.current.temperature}°C",
                        fontSize = 72.sp,
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
                    Text(
                        text = "${weather.current.temperature - 2}° / ${weather.current.temperature + 2}°",
                        fontSize = 14.sp,
                        color = WhiteText.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WeatherInfoCard(
                            emoji = "🌡️",
                            label = "Terasa seperti",
                            value = "${weather.current.feelsLike}°",
                            description = "Suhu yang dirasakan tubuh berdasarkan kelembapan dan angin.",
                            modifier = Modifier.weight(1f)
                        )
                        WeatherInfoCard(
                            emoji = "💧",
                            label = stringResource(R.string.humidity),
                            value = "${weather.current.humidity}%",
                            description = when {
                                weather.current.humidity < 30 -> "Kelembapan rendah. Udara terasa kering."
                                weather.current.humidity < 60 -> "Kelembapan normal. Kondisi nyaman."
                                weather.current.humidity < 80 -> "Kelembapan tinggi. Udara terasa lembap."
                                else -> "Kelembapan sangat tinggi. Tidak nyaman."
                            },
                            modifier = Modifier.weight(1f)
                        )
                        WeatherInfoCard(
                            emoji = "☀️",
                            label = "UV Index",
                            value = "${weather.current.uvIndex}",
                            description = when {
                                weather.current.uvIndex < 3 -> "UV Rendah. Aman beraktivitas di luar."
                                weather.current.uvIndex < 6 -> "UV Sedang. Gunakan tabir surya."
                                weather.current.uvIndex < 8 -> "UV Tinggi. Hindari paparan langsung."
                                weather.current.uvIndex < 11 -> "UV Sangat Tinggi. Gunakan pelindung."
                                else -> "UV Ekstrem. Hindari keluar ruangan."
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WeatherInfoCard(
                            emoji = "💨",
                            label = stringResource(R.string.wind),
                            value = "${weather.current.windSpeed} km/h",
                            description = when {
                                weather.current.windSpeed < 20 -> "Angin lemah. Kondisi tenang."
                                weather.current.windSpeed < 40 -> "Angin sedang. Terasa di wajah."
                                weather.current.windSpeed < 60 -> "Angin kencang. Hati-hati berkendara."
                                else -> "Angin sangat kencang. Berbahaya!"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        WeatherInfoCard(
                            emoji = "🌬️",
                            label = "Tekanan",
                            value = "${weather.current.pressure} hPa",
                            description = when {
                                weather.current.pressure < 1000 -> "Tekanan rendah. Kemungkinan hujan."
                                weather.current.pressure < 1013 -> "Tekanan normal. Cuaca stabil."
                                else -> "Tekanan tinggi. Cuaca cerah."
                            },
                            modifier = Modifier.weight(1f)
                        )
                        WeatherInfoCard(
                            emoji = "👁️",
                            label = "Visibilitas",
                            value = "${"%.1f".format(weather.current.visibility / 1000)} km",
                            description = when {
                                weather.current.visibility < 1000 -> "Visibilitas sangat rendah. Kabut tebal."
                                weather.current.visibility < 5000 -> "Visibilitas rendah. Hati-hati berkendara."
                                weather.current.visibility < 10000 -> "Visibilitas sedang. Cukup jelas."
                                else -> "Visibilitas baik. Pandangan jelas."
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                hourlyWeather?.let { hourlyData ->
                    Text(
                        text = "Prakiraan Per Jam",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = WhiteText.copy(alpha = 0.15f)
                        )
                    ) {
                        val currentHour = java.util.Calendar.getInstance()
                            .get(java.util.Calendar.HOUR_OF_DAY)
                        val startIndex = hourlyData.hourly.times.indexOfFirst {
                            it.substring(11, 13).toInt() >= currentHour
                        }.coerceAtLeast(0)
                        val endIndex = (startIndex + 8)
                            .coerceAtMost(hourlyData.hourly.times.size)

                        LazyRow(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(endIndex - startIndex) { i ->
                                val index = startIndex + i
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Text(
                                        text = hourlyData.hourly.times[index].substring(11, 16),
                                        fontSize = 12.sp,
                                        color = WhiteText.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = getWeatherEmoji(hourlyData.hourly.weatherCodes[index]),
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${hourlyData.hourly.temperatures[index]}°",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WhiteText
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "💧${hourlyData.hourly.precipitationProbability[index]}%",
                                        fontSize = 11.sp,
                                        color = WhiteText.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                forecast?.let {
                    Text(
                        text = stringResource(R.string.forecast_title),
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
                            containerColor = WhiteText.copy(alpha = 0.15f)
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
