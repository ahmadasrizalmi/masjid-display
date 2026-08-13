package com.asridigital.masjiddisplay.designsystem.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview

/**
 * Edge-to-edge media region used by NORMAL TV layouts.
 *
 * The image loader/storage layer is intentionally outside the design system. The caller supplies
 * media content and must render imagery with [contentScale] (SSOT default: [ContentScale.Crop]).
 * No card inset, rounded container, carousel timing, file IO or network behavior belongs here.
 */
@Composable
fun MediaSurface(
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    content: @Composable BoxScope.(ContentScale) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        content(contentScale)
    }
}

@Preview(widthDp = 1920, heightDp = 720, showBackground = true)
@Composable
private fun MediaSurfacePreview() {
    MediaSurface { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFD9DDD8)),
        )
    }
}
