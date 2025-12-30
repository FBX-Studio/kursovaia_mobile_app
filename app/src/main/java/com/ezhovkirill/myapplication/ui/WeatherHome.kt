package com.ezhovkirill.myapplication.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ezhovkirill.myapplication.R
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherHome(viewModel: WeatherViewModel = viewModel(), innerPadding: PaddingValues = PaddingValues(0.dp)) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme

    val backgroundWeatherCode = remember(uiState.hourly, uiState.dailyForecast) {
        uiState.hourly.firstOrNull { it.isActive }?.iconRes
            ?: uiState.hourly.firstOrNull()?.iconRes
            ?: uiState.dailyForecast.firstOrNull()?.iconRes
            ?: 0
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.fetchLocationAndWeather()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            isDarkTheme = uiState.isDarkTheme,
            isImperialUnits = uiState.isImperialUnits,
            onToggleTheme = viewModel::toggleTheme,
            onToggleUnits = viewModel::toggleUnits,
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Favorites dialog intentionally not shown in this “photo-style” layout.

    if (showSearchDialog) {
        CitySearchDialog(
            query = query,
            onQueryChange = { query = it },
            suggested = uiState.suggestedCities,
            results = uiState.searchResults,
            favoriteIds = uiState.favoriteCities.map { it.id }.toSet(),
            onSearch = viewModel::searchCity,
            onSelect = {
                viewModel.selectCity(it)
                showSearchDialog = false
                query = ""
            },
            onToggleFavorite = { viewModel.addToFavorites(it) },
            onDismiss = { showSearchDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background),
        topBar = {}
    ) { scaffoldPadding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }

            AnimatedWeatherBackground(
                widthPx = widthPx,
                heightPx = heightPx,
                weatherCode = backgroundWeatherCode,
                modifier = Modifier.fillMaxSize()
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 28.dp, top = 12.dp)
            ) {
                if (uiState.isLoading) {
                    item {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }

                val errorMessage = uiState.error
                if (errorMessage != null) {
                    item {
                        ErrorCard(message = errorMessage)
                    }
                }

                item {
                    PhotoTopBar(
                        city = uiState.city,
                        onSearchClick = { showSearchDialog = true },
                        onMoreClick = { showSettingsDialog = true }
                    )
                }

                item {
                    PhotoHero(
                        city = uiState.city,
                        dateLabel = todayRuLabel(),
                        temp = uiState.currentTemp,
                        condition = uiState.condition,
                        weatherCode = backgroundWeatherCode
                    )
                }

                item {
                    PhotoMiniStatsRow(
                        wind = uiState.windSpeed,
                        humidity = uiState.humidity,
                        feelsLike = uiState.apparentTemp
                    )
                }

                item {
                    PhotoSectionTitle(title = "Почасовой прогноз")
                }

                item {
                    PhotoHourlyRow(hourly = uiState.hourly)
                }

                item {
                    PhotoSectionTitle(title = "Прогноз на неделю")
                }

                items(uiState.dailyForecast) { day ->
                    PhotoWeeklyRow(item = day)
                }
            }
        }
    }
}

private fun todayRuLabel(): String {
    val date = LocalDate.now()
    val monthRaw = date.month.getDisplayName(TextStyle.SHORT, Locale("ru"))
    val month = monthRaw.replace(".", "").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }
    return "Сегодня, ${date.dayOfMonth} $month"
}

@Composable
private fun PhotoTopBar(
    city: String,
    onSearchClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Поиск",
                tint = colorScheme.onBackground
            )
        }
        Surface(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 44.dp)
                .clickable(onClick = onSearchClick),
            shape = RoundedCornerShape(12.dp),
            color = colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ) {
            Text(
                text = city,
                color = colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Меню",
                tint = colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun PhotoHero(
    city: String,
    dateLabel: String,
    temp: String,
    condition: String,
    weatherCode: Int
) {
    val colorScheme = MaterialTheme.colorScheme
    val style = remember(weatherCode) { backgroundStyleForWeatherCode(weatherCode) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = city,
            color = colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = dateLabel,
            color = colorScheme.onBackground.copy(alpha = 0.70f),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(18.dp))

        Image(
            painter = painterResource(id = weatherIconRes(weatherCode)),
            contentDescription = null,
            modifier = Modifier.size(96.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = temp,
            color = colorScheme.onBackground,
            fontSize = 64.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = condition,
            color = colorScheme.onBackground.copy(alpha = 0.78f),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun PhotoMiniStatsRow(
    wind: String,
    humidity: String,
    feelsLike: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PhotoMiniStatCard(modifier = Modifier.weight(1f), value = wind, label = "Ветер")
        PhotoMiniStatCard(modifier = Modifier.weight(1f), value = humidity, label = "Влажность")
        PhotoMiniStatCard(modifier = Modifier.weight(1f), value = feelsLike, label = "Ощущается")
    }
}

@Composable
private fun PhotoMiniStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.heightIn(min = 72.dp),
        shape = RoundedCornerShape(14.dp),
        color = colorScheme.surfaceVariant.copy(alpha = 0.28f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = colorScheme.onBackground.copy(alpha = 0.70f),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PhotoSectionTitle(title: String) {
    val colorScheme = MaterialTheme.colorScheme
    Text(
        text = title,
        color = colorScheme.onBackground.copy(alpha = 0.86f),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun PhotoHourlyRow(hourly: List<HourlyUiItem>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(hourly) { item ->
            PhotoHourlyPill(item)
        }
    }
}

@Composable
private fun PhotoHourlyPill(item: HourlyUiItem) {
    val colorScheme = MaterialTheme.colorScheme
    val bg = if (item.isActive) {
        colorScheme.onBackground.copy(alpha = 0.16f)
    } else {
        colorScheme.surfaceVariant.copy(alpha = 0.26f)
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bg,
        modifier = Modifier
            .width(62.dp)
            .height(120.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.time,
                color = colorScheme.onBackground.copy(alpha = 0.86f),
                style = MaterialTheme.typography.labelMedium
            )
            Image(
                painter = painterResource(id = weatherIconRes(item.iconRes)),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = item.temp,
                color = colorScheme.onBackground,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PhotoWeeklyRow(item: DailyUiItem) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colorScheme.surfaceVariant.copy(alpha = 0.20f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.day,
                color = colorScheme.onBackground.copy(alpha = 0.90f),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.widthIn(min = 44.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Image(
                painter = painterResource(id = weatherIconRes(item.iconRes)),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = item.maxTemp,
                color = colorScheme.onBackground,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = item.minTemp,
                color = colorScheme.onBackground.copy(alpha = 0.70f),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

private enum class BackgroundStyle {
    Clear,
    Cloudy,
    Fog,
    Rain,
    Snow,
    Thunder
}

private fun backgroundStyleForWeatherCode(code: Int): BackgroundStyle {
    return when (code) {
        0 -> BackgroundStyle.Clear
        1, 2, 3 -> BackgroundStyle.Cloudy
        45, 48 -> BackgroundStyle.Fog
        71, 73, 75, 77, 85, 86 -> BackgroundStyle.Snow
        95, 96, 99 -> BackgroundStyle.Thunder
        in 51..67, in 80..82 -> BackgroundStyle.Rain
        else -> BackgroundStyle.Cloudy
    }
}

@Composable
private fun AnimatedWeatherBackground(
    widthPx: Float,
    heightPx: Float,
    weatherCode: Int,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val style = remember(weatherCode) { backgroundStyleForWeatherCode(weatherCode) }

    val transition = rememberInfiniteTransition(label = "weather-bg")
    val driftA by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftA"
    )
    val driftB by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 28000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftB"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val intensity = remember(style) {
        when (style) {
            BackgroundStyle.Clear -> 1.00f
            BackgroundStyle.Cloudy -> 0.95f
            BackgroundStyle.Fog -> 0.80f
            BackgroundStyle.Rain -> 1.10f
            BackgroundStyle.Snow -> 0.90f
            BackgroundStyle.Thunder -> 1.20f
        }
    }

    // 0..1 helper derived from pulse (0.96..1.06)
    val pulse01 = ((pulse - 0.96f) / 0.10f).coerceIn(0f, 1f)

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            // Photo-like dark gradient (uses theme primitives, no hard-coded palette)
                            colorScheme.surface,
                            colorScheme.background,
                            colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    )
                )
        )

        // Subtle vignette for depth (like the screenshot)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            colorScheme.scrim.copy(alpha = 0.45f)
                        ),
                        center = Offset(widthPx * 0.55f, heightPx * 0.35f),
                        radius = maxOf(widthPx, heightPx) * 0.95f
                    )
                )
        )

        // Literal weather decoration layer: photo-like (center watermark + particles)
        WeatherDecorationLayer(
            style = style,
            driftA = driftA,
            driftB = driftB,
            pulse01 = pulse01,
            intensity = intensity
        )
    }
}

@Composable
private fun WeatherDecorationLayer(
    style: BackgroundStyle,
    driftA: Float,
    driftB: Float,
    pulse01: Float,
    intensity: Float
) {
    val colorScheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "weather-decor")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Small "snow/star" dots (like the reference screenshot).
        val dotsCount = when (style) {
            BackgroundStyle.Snow -> (160 * intensity).toInt().coerceIn(110, 220)
            BackgroundStyle.Rain -> (110 * intensity).toInt().coerceIn(80, 160)
            else -> (120 * intensity).toInt().coerceIn(80, 170)
        }

        for (i in 0 until dotsCount) {
            val seed = (i * 1103515245 + 12345).toLong()
            val x01 = ((seed ushr 16) and 0xFF).toFloat() / 255f
            val s01 = ((seed ushr 24) and 0xFF).toFloat() / 255f
            val phase = ((seed ushr 8) and 0xFF).toFloat() / 255f

            val r = when (style) {
                BackgroundStyle.Snow -> 0.9f + 1.8f * s01
                else -> 0.6f + 1.2f * s01
            } * intensity

            val speed = when (style) {
                BackgroundStyle.Snow -> (0.10f + 0.35f * s01)
                else -> (0.03f + 0.10f * s01)
            } * intensity

            val x = size.width * x01
            val y = ((t * speed + phase) % 1f) * size.height

            drawCircle(
                color = colorScheme.onBackground.copy(alpha = (0.10f + 0.20f * s01) * intensity),
                radius = r,
                center = Offset(x, y)
            )
        }

        // Optional: a tiny bit of rain streaks for rain only (kept subtle like the photo)
        if (style == BackgroundStyle.Rain) {
            val count = (34 * intensity).toInt().coerceIn(20, 60)
            val dx = size.width * 0.05f
            val dy = size.height * 0.11f
            val stroke = (1.8f * intensity).coerceAtLeast(1.2f)
            for (i in 0 until count) {
                val seed = (i * 1664525 + 1013904223).toLong()
                val x01 = ((seed ushr 16) and 0xFF).toFloat() / 255f
                val phase = ((seed ushr 8) and 0xFF).toFloat() / 255f
                val x = size.width * x01
                val y = ((t + phase) % 1f) * (size.height + dy) - dy
                drawLine(
                    color = colorScheme.onBackground.copy(alpha = 0.06f * intensity),
                    start = Offset(x, y),
                    end = Offset(x + dx, y + dy),
                    strokeWidth = stroke
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@Composable
private fun ErrorCard(message: String) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            color = colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun CurrentWeatherCard(
    temp: String,
    highLow: String,
    apparentTemp: String
) {
    val colorScheme = MaterialTheme.colorScheme

    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            colorScheme.primaryContainer.copy(alpha = 0.75f),
            colorScheme.surface
        )
    )
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = temp,
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = highLow,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                AssistChip(
                    onClick = {},
                    label = { Text("Ощущается $apparentTemp") },
                    colors = AssistChipDefaults.assistChipColors(containerColor = colorScheme.surfaceVariant)
                )
            }
        }
    }
}

@Composable
private fun ForecastTabs(selectedTab: Int, onSelectedTabChange: (Int) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { onSelectedTabChange(0) },
                text = { Text("Почасовой") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { onSelectedTabChange(1) },
                text = { Text("На неделю") }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    val colorScheme = MaterialTheme.colorScheme
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 2.dp)
    )
}

@Composable
private fun ForecastSection(
    selectedTab: Int,
    onSelectedTabChange: (Int) -> Unit,
    hourly: List<HourlyUiItem>,
    daily: List<DailyUiItem>
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ForecastTabs(selectedTab = selectedTab, onSelectedTabChange = onSelectedTabChange)
            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                if (selectedTab == 0) {
                    HourlyForecastRow(items = hourly)
                } else {
                    DailyForecastList(items = daily)
                }
            }
        }
    }
}

@Composable
private fun HourlyForecastRow(items: List<HourlyUiItem>) {
    if (items.isEmpty()) return

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
    ) {
        items(items) { item ->
            HourlyForecastCard(item)
        }
    }
}

@Composable
private fun HourlyForecastCard(item: HourlyUiItem) {
    val colorScheme = MaterialTheme.colorScheme
    val container = if (item.isActive) colorScheme.primaryContainer else colorScheme.surfaceVariant.copy(alpha = 0.55f)
    val onContainer = if (item.isActive) colorScheme.onPrimaryContainer else colorScheme.onSurface

    val background = if (item.isActive) {
        Brush.linearGradient(
            colors = listOf(
                colorScheme.primaryContainer,
                colorScheme.secondaryContainer.copy(alpha = 0.65f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                container,
                colorScheme.surface
            )
        )
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isActive) 3.dp else 0.dp),
        modifier = Modifier
            .width(84.dp)
            .height(124.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
        ) {
            if (item.isActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .size(width = 26.dp, height = 3.dp)
                        .background(colorScheme.primary, RoundedCornerShape(99.dp))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp, horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.time,
                    style = MaterialTheme.typography.labelLarge,
                    color = onContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )

                Image(
                    painter = painterResource(id = weatherIconRes(item.iconRes)),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )

                Text(
                    text = item.temp,
                    style = MaterialTheme.typography.titleMedium,
                    color = onContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DailyForecastList(items: List<DailyUiItem>) {
    if (items.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            DailyForecastRow(item)
            if (index != items.lastIndex) {
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            }
        }
    }
}

@Composable
private fun DailyForecastRow(item: DailyUiItem) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = item.day,
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.onSurface,
            modifier = Modifier.widthIn(min = 96.dp)
        )

        Image(
            painter = painterResource(id = weatherIconRes(item.iconRes)),
            contentDescription = null,
            modifier = Modifier.size(26.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = item.maxTemp,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = item.minTemp,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailsRow(
    wind: String,
    humidity: String,
    pressure: String,
    sunrise: String,
    sunset: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DetailChip(label = "Ветер", value = wind)
        DetailChip(label = "Влажность", value = humidity)
        DetailChip(label = "Давление", value = pressure)
        DetailChip(label = "Восход", value = sunrise)
        DetailChip(label = "Закат", value = sunset)
    }
}

@Composable
private fun DetailsSection(
    wind: String,
    humidity: String,
    pressure: String,
    sunrise: String,
    sunset: String
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            DetailsRow(
                wind = wind,
                humidity = humidity,
                pressure = pressure,
                sunrise = sunrise,
                sunset = sunset
            )
        }
    }
}

@Composable
private fun DetailChip(label: String, value: String) {
    val colorScheme = MaterialTheme.colorScheme
    AssistChip(
        onClick = {},
        modifier = Modifier
            .widthIn(min = 110.dp)
            .heightIn(min = 44.dp),
        label = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        },
        colors = AssistChipDefaults.assistChipColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.55f))
    )
}

@Composable
private fun SettingsDialog(
    isDarkTheme: Boolean,
    isImperialUnits: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onToggleUnits: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройки") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Тёмная тема")
                    Switch(checked = isDarkTheme, onCheckedChange = onToggleTheme)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Имперские единицы (°F, mph)")
                    Switch(checked = isImperialUnits, onCheckedChange = onToggleUnits)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Готово") }
        }
    )
}

@Composable
private fun FavoritesDialog(
    favorites: List<com.ezhovkirill.myapplication.data.GeocodingResult>,
    onSelect: (com.ezhovkirill.myapplication.data.GeocodingResult) -> Unit,
    onRemove: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 520.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Избранное", style = MaterialTheme.typography.titleLarge)
                    Button(onClick = onDismiss) { Text("Закрыть") }
                }

                if (favorites.isEmpty()) {
                    Text(
                        "Список пуст",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                    return@Card
                }

                LazyColumn {
                    items(favorites) { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(city) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(city.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                val subtitle = listOfNotNull(city.admin1, city.country).joinToString(", ")
                                if (subtitle.isNotBlank()) {
                                    Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            IconButton(onClick = { onRemove(city.id) }) {
                                Icon(Icons.Default.Star, contentDescription = "Удалить из избранного", tint = colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CitySearchDialog(
    query: String,
    onQueryChange: (String) -> Unit,
    suggested: List<SearchResultItem>,
    results: List<SearchResultItem>,
    favoriteIds: Set<Int>,
    onSearch: (String) -> Unit,
    onSelect: (SearchResultItem) -> Unit,
    onToggleFavorite: (SearchResultItem) -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.scrim.copy(alpha = 0.6f))
                    .clickable { onDismiss() }
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                SearchBar(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    active = true,
                    onActiveChange = { if (!it) onDismiss() },
                    placeholder = { Text("Поиск города") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    colors = SearchBarDefaults.colors(containerColor = colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val itemsToShow = if (query.isBlank()) suggested else results
                    LazyColumn {
                        items(itemsToShow) { result ->
                            val isFav = favoriteIds.contains(result.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(result) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(result.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (result.description.isNotBlank()) {
                                        Text(result.description, style = MaterialTheme.typography.bodyLarge, color = colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                IconButton(onClick = { onToggleFavorite(result) }) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Default.Star else Icons.Outlined.Star,
                                        contentDescription = "В избранное",
                                        tint = if (isFav) colorScheme.primary else colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun weatherIconRes(code: Int): Int {
    return when (code) {
        0, 1 -> R.drawable.img_small_sun_cloud_mid_rain
        2, 3, 45, 48 -> R.drawable.img_small_moon_cloud_fast_wind
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> R.drawable.img_small_sun_cloud_angled_rain
        71, 73, 75, 77, 85, 86 -> R.drawable.img_big_moon_cloud_mid_rain
        95, 96, 99 -> R.drawable.img_big_moon_cloud_mid_rain
        else -> R.drawable.img_big_moon_cloud_mid_rain
    }
}
