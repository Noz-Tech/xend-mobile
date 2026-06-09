package com.noztek.xend.feature.welcome.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowRight
import com.noztek.xend.core.ui.components.AppButton

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(52.dp))
            HeroArt(
                blobColor = colorScheme.primaryContainer,
                mutedColor = colorScheme.surfaceVariant,
                accentColor = colorScheme.primary,
                onBlobColor = colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.weight(1f))
            PagerIndicator()
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Xend: Intimacy,\nAnytime, Anywhere",
                style = MaterialTheme.typography.displaySmall.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(28.dp))
            AppButton(
                text = "",
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Spacer(modifier = Modifier.size(1.dp))
                        Text(
                            text = "Get Started",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Heroicons.Outline.ArrowRight,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeroArt(
    blobColor: Color,
    mutedColor: Color,
    accentColor: Color,
    onBlobColor: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
    ) {
        // Organic dark shape cluster.
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = blobColor,
                radius = 92.dp.toPx(),
                center = center.copy(x = size.width * 0.30f, y = size.height * 0.28f)
            )
            drawCircle(
                color = blobColor,
                radius = 96.dp.toPx(),
                center = center.copy(x = size.width * 0.72f, y = size.height * 0.22f)
            )
            drawCircle(
                color = blobColor,
                radius = 98.dp.toPx(),
                center = center.copy(x = size.width * 0.24f, y = size.height * 0.66f)
            )
            drawCircle(
                color = blobColor,
                radius = 72.dp.toPx(),
                center = center.copy(x = size.width * 0.78f, y = size.height * 0.72f)
            )
            drawCircle(
                color = blobColor,
                radius = 58.dp.toPx(),
                center = center.copy(x = size.width * 0.48f, y = size.height * 0.92f)
            )
        }

        Box(
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(mutedColor)
                .align(Alignment.TopStart)
                .padding(14.dp),
        ) {
            Text(text = "🤖", fontSize = 42.sp, modifier = Modifier.align(Alignment.Center))
        }

        Box(
            modifier = Modifier
                .size(134.dp)
                .clip(CircleShape)
                .background(blobColor)
                .align(Alignment.TopEnd),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(78.dp, 36.dp)) {
                val segment = size.width / 12f
                for (i in 0..11) {
                    val h = if (i % 2 == 0) size.height * 0.95f else size.height * 0.55f
                    drawLine(
                        color = accentColor,
                        start = androidx.compose.ui.geometry.Offset(x = i * segment, y = size.height / 2f - h / 2f),
                        end = androidx.compose.ui.geometry.Offset(x = i * segment, y = size.height / 2f + h / 2f),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(blobColor)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "🎙", fontSize = 34.sp)
        }

        Text(
            text = "✧",
            color = onBlobColor,
            fontSize = 42.sp,
            modifier = Modifier.align(Alignment.CenterStart),
        )

        Box(
            modifier = Modifier
                .size(94.dp)
                .clip(CircleShape)
                .background(mutedColor)
                .align(Alignment.CenterEnd),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "🌫", fontSize = 34.sp)
        }
    }
}

@Composable
private fun PagerIndicator() {
    val colorScheme = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(width = 28.dp, height = 6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colorScheme.primaryContainer),
        )
        Box(
            modifier = Modifier
                .size(width = 28.dp, height = 6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colorScheme.surfaceVariant),
        )
        Box(
            modifier = Modifier
                .size(width = 28.dp, height = 6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colorScheme.surfaceVariant),
        )
    }
}
