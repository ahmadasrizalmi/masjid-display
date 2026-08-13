package com.asridigital.masjiddisplay.designsystem.tv

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/** Typed presentation wrappers keep each focus-state hierarchy explicit and SSOT-safe. */
@Composable
fun ApproachingScreen(
    prayerName: String,
    countdown: String,
    adhanTime: String,
    modifier: Modifier = Modifier,
    reminder: String? = null,
) {
    require(!countdown.startsWith("-")) { "Approaching countdown must never be negative" }
    FocusStateContent(
        modifier = modifier,
        stateLabel = "SHOLAT BERIKUTNYA",
        primaryLabel = prayerName,
        heroValue = countdown,
        contextTime = "Adzan · $adhanTime",
        secondaryMessage = reminder,
    )
}

@Composable
fun AdhanScreen(
    prayerName: String,
    adhanTime: String,
    modifier: Modifier = Modifier,
    reminder: String? = null,
) {
    FocusStateContent(
        modifier = modifier,
        stateIcon = "◇",
        primaryLabel = prayerName,
        heroValue = adhanTime,
        secondaryMessage = reminder ?: "WAKTU ADZAN",
    )
}

@Composable
fun IqamahScreen(
    prayerName: String,
    countdown: String,
    modifier: Modifier = Modifier,
    message: String = "Persiapkan dan rapatkan shaf",
) {
    require(!countdown.startsWith("-")) { "Iqamah countdown must never be negative" }
    FocusStateContent(
        modifier = modifier,
        stateIcon = "◇",
        primaryLabel = "IQAMAH $prayerName",
        heroValue = countdown,
        secondaryMessage = message,
    )
}

@Composable
fun PrayerScreen(
    prayerName: String,
    modifier: Modifier = Modifier,
    message: String = "Luruskan dan rapatkan shaf",
) {
    FocusStateContent(
        modifier = modifier,
        stateIcon = "◇",
        primaryLabel = "SHOLAT $prayerName",
        secondaryMessage = message,
    )
}

@Composable
fun FridayScreen(
    modifier: Modifier = Modifier,
    message: String = "Dengarkan khutbah dengan tenang",
) {
    FocusStateContent(
        modifier = modifier,
        stateIcon = "◇",
        primaryLabel = "SHOLAT JUMAT",
        secondaryMessage = message,
    )
}

@Composable
fun NoticeScreen(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    FocusStateContent(
        modifier = modifier,
        stateIcon = "◇",
        primaryLabel = title,
        secondaryMessage = message,
    )
}

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun ApproachingScreenPreview() = ApproachingScreen("MAGHRIB", "09:54", "17:42")

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun AdhanScreenPreview() = AdhanScreen("MAGHRIB", "17:42")

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun IqamahScreenPreview() = IqamahScreen("MAGHRIB", "08:42")

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun PrayerScreenPreview() = PrayerScreen("MAGHRIB")

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun FridayScreenPreview() = FridayScreen()

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun NoticeScreenPreview() = NoticeScreen(
    title = "SENYAPKAN PERANGKAT",
    message = "Mohon aktifkan mode senyap sebelum sholat dimulai",
)
