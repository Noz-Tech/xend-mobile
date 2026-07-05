package com.noztek.xend.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    content: (@Composable () -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val disabledContainerColor = containerColor.copy(alpha = 0.42f)
    val disabledContentColor = contentColor.copy(alpha = 0.82f)

    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        enabled = enabled,
        shape = RoundedCornerShape(35.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        ),
    ) {
        if (content != null) {
            content()
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLoading) {
                    ButtonLoadingIndicator(color = contentColor)
                } else {
                    Text(text)
                }
            }
        }
    }
}

@Composable
private fun ButtonLoadingIndicator(
    color: Color,
) {
    val transition = rememberInfiniteTransition(label = "buttonLoading")
    val rotation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "buttonLoadingRotation",
    )

    Box(
        modifier = Modifier.size(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { 0.72f },
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer { rotationZ = rotation.value },
            color = color,
            trackColor = Color.Transparent,
            strokeWidth = 2.dp,
            strokeCap = StrokeCap.Round,
        )
    }
}
