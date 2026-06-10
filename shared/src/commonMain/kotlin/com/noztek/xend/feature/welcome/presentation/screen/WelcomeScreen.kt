package com.noztek.xend.feature.welcome.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowRight
import com.composables.icons.heroicons.outline.Heart
import com.composables.icons.heroicons.outline.LockClosed
import com.composables.icons.heroicons.outline.Sparkles
import org.jetbrains.compose.resources.painterResource
import xend.shared.generated.resources.Res
import xend.shared.generated.resources.couple_1
import xend.shared.generated.resources.logo
import xend.shared.generated.resources.orbit_1
import xend.shared.generated.resources.orbit_2
import xend.shared.generated.resources.orbit_3
import kotlin.math.PI

private data class OnboardingPage(
    val title: String,
    val highlightedTitlePart: String,
    val body: String,
    val action: String,
    val accent: Color,
    val accentSoft: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private data class OrbitSpec(
    val painter: Painter,
    val size: Dp,
    val radiusX: Dp,
    val radiusY: Dp,
    val phaseOffset: Float,
)

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val headlineColor = Color(0xFF111111)
    val bodyColor = Color(0xFF111111).copy(alpha = 0.72f)
    val pages = remember {
        listOf(
            OnboardingPage(
                title = "Your private space for two.",
                highlightedTitlePart = "space for two",
                body = "A secure place to message, play, share moments, and stay close with the one person who matters most.",
                action = "Continue",
                accent = Color(0xFF2E3A59),
                accentSoft = Color(0xFFFFEAA6),
                icon = Heroicons.Outline.Heart,
            ),
            OnboardingPage(
                title = "Only you two belong here.",
                highlightedTitlePart = "belong here",
                body = "Xend is built for private conversations, honest moments, silly talks, and memories you don't want mixed with the noise of everywhere else.",
                action = "Continue",
                accent = Color(0xFF26385C),
                accentSoft = Color(0xFFFFE6A0),
                icon = Heroicons.Outline.LockClosed,
            ),
            OnboardingPage(
                title = "Create your space together.",
                highlightedTitlePart = "space together",
                body = "Set up your private space, invite your partner, and start building a place that belongs only to both of you.",
                action = "Create Our Space",
                accent = Color(0xFF24344B),
                accentSoft = Color(0xFFFFD56A),
                icon = Heroicons.Outline.Sparkles,
            ),
        )
    }
    var hasStartedStory by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var currentPage by rememberSaveable { androidx.compose.runtime.mutableIntStateOf(0) }
    val progress by animateFloatAsState(
        targetValue = (currentPage + 1) / pages.size.toFloat(),
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "onboardingProgress",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 22.dp, vertical = 18.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WelcomeHeader(
                isLastPage = !hasStartedStory || currentPage == pages.lastIndex,
                showSkip = hasStartedStory,
                onSkip = { onGetStarted() }
            )
            Spacer(modifier = Modifier.height(18.dp))
            if (!hasStartedStory) {
                IntroWelcomeContent(
                    headlineColor = headlineColor,
                    bodyColor = bodyColor,
                    primaryColor = colorScheme.primary,
                    onStart = { hasStartedStory = true },
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        AnimatedContent(
                            targetState = currentPage,
                            transitionSpec = {
                                fadeIn(
                                    animationSpec = tween(
                                        durationMillis = 420,
                                        easing = FastOutSlowInEasing,
                                    ),
                                ) togetherWith fadeOut(
                                    animationSpec = tween(
                                        durationMillis = 220,
                                        easing = FastOutSlowInEasing,
                                    ),
                                )
                            },
                            label = "welcomePager",
                        ) { index ->
                            StoryPageContent(
                                page = pages[index],
                                primaryColor = colorScheme.primary,
                                headlineColor = headlineColor,
                                bodyColor = bodyColor,
                            )
                        }
                    }
                    WelcomeFooter(
                        currentPage = currentPage,
                        pageCount = pages.size,
                        buttonColor = colorScheme.primary,

                        onNext = {
                            if (currentPage == pages.lastIndex) {
                                onGetStarted()
                            } else {
                                currentPage += 1
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeHeader(
    isLastPage: Boolean,
    showSkip: Boolean,
    onSkip: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.logo),
            contentDescription = "Xend",
            modifier = Modifier
                .height(28.dp),
        )
        if (showSkip && !isLastPage) {
            Text(
                text = "Skip",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.clickable(onClick = onSkip),
            )
        } else {
            Spacer(modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun IntroWelcomeContent(
    headlineColor: Color,
    bodyColor: Color,
    primaryColor: Color,
    onStart: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            OrbitingIntroIllustration(primaryColor = primaryColor)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Welcome to Xend",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 44.sp,
                textAlign = TextAlign.Center
            ),
            color = headlineColor,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Before anything else, this is your place to slow down, connect, and make something that belongs only to both of you.",
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
            ),
            color = bodyColor,
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = "Get Started",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun OrbitingIntroIllustration(
    primaryColor: Color,
) {
    val orbit1 = painterResource(Res.drawable.orbit_1)
    val orbit2 = painterResource(Res.drawable.orbit_2)
    val orbit3 = painterResource(Res.drawable.orbit_3)
    val couplePainter = painterResource(Res.drawable.couple_1)
    val infiniteTransition = rememberInfiniteTransition(label = "orbitTransition")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbitAngle",
    )
    val orbitSpecs = remember(orbit1, orbit2, orbit3) {
        listOf(
            OrbitSpec(
                painter = orbit1,
                size = 54.dp,
                radiusX = 132.dp,
                radiusY = 84.dp,
                phaseOffset = -18f,
            ),
            OrbitSpec(
                painter = orbit2,
                size = 66.dp,
                radiusX = 148.dp,
                radiusY = 98.dp,
                phaseOffset = 106f,
            ),
            OrbitSpec(
                painter = orbit3,
                size = 50.dp,
                radiusX = 118.dp,
                radiusY = 76.dp,
                phaseOffset = 222f,
            ),
        )
    }

    Box(
        modifier = Modifier
            .size(400.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.34f),
                        primaryColor.copy(alpha = 0.20f),
                        primaryColor.copy(alpha = 0.09f),
                        Color.Transparent,
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        OrbitLayer(
            orbitSpecs = orbitSpecs,
            orbitAngle = orbitAngle,
            renderFront = false,
        )
        Image(
            painter = couplePainter,
            contentDescription = "Couple illustration",
            modifier = Modifier
                .fillMaxWidth(0.84f)
                .height(260.dp),
            contentScale = ContentScale.Fit,
        )
        OrbitLayer(
            orbitSpecs = orbitSpecs,
            orbitAngle = orbitAngle,
            renderFront = true,
        )
    }
}

@Composable
private fun OrbitLayer(
    orbitSpecs: List<OrbitSpec>,
    orbitAngle: Float,
    renderFront: Boolean,
) {
    orbitSpecs.forEach { spec ->
        val angleInRadians = ((orbitAngle + spec.phaseOffset) * (PI / 180.0)).toFloat()
        val x = (kotlin.math.cos(angleInRadians) * spec.radiusX.value)
        val y = (kotlin.math.sin(angleInRadians) * spec.radiusY.value)
        val isFront = y >= 0f

        if (isFront == renderFront) {
            val depth = ((y / spec.radiusY.value) + 1f) / 2f
            val scale = lerpValue(start = 0.76f, end = 1.08f, fraction = depth)
            val alpha = lerpValue(start = 0.54f, end = 1f, fraction = depth)

            Image(
                painter = spec.painter,
                contentDescription = null,
                modifier = Modifier
                    .size(spec.size)
                    .graphicsLayer {
                        translationX = x.dp.toPx()
                        translationY = y.dp.toPx()
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    },
                contentScale = ContentScale.Fit,
            )
        }
    }
}

private fun lerpValue(
    start: Float,
    end: Float,
    fraction: Float,
): Float = start + ((end - start) * fraction.coerceIn(0f, 1f))

@Composable
private fun IllustrationPlaceholder(
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp)
            .clip(RoundedCornerShape(34.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color = Color(0xFFF8FAFD),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(34.dp.toPx()),
            )
        }

        Box(
            modifier = Modifier
                .size(380.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            FloatingStoryToken(
                modifier = Modifier,
                size = 88.dp,
                background = Color.White.copy(alpha = 0.92f),
                tint = Color(0xFF111111),
                icon = icon,
            )
        }
    }
}

@Composable
private fun StoryPageContent(
    page: OnboardingPage,
    primaryColor: Color,
    headlineColor: Color,
    bodyColor: Color,
) {
    var reveal by remember(page.title) { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(page.title) {
        reveal = true
    }

    val heroAlpha by animateFloatAsState(
        targetValue = if (reveal) 1f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "storyHeroAlpha",
    )
    val heroScale by animateFloatAsState(
        targetValue = if (reveal) 1f else 0.98f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "storyHeroScale",
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (reveal) 1f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "storyTextAlpha",
    )
    val textOffset by animateDpAsState(
        targetValue = if (reveal) 0.dp else 12.dp,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "storyTextOffset",
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.graphicsLayer {
                alpha = heroAlpha
                scaleX = heroScale
                scaleY = heroScale
            },
        ) {
            IllustrationPlaceholder(
                accent = page.accent,
                icon = page.icon,
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Spacer(modifier = Modifier.height(28.dp))
        Column(
            modifier = Modifier.graphicsLayer {
                alpha = textAlpha
                translationY = textOffset.toPx()
            },
        ) {
            Text(
                text = buildAnnotatedString {
                    val title = page.title
                    val highlighted = page.highlightedTitlePart
                    val start = title.indexOf(highlighted)
                    if (start >= 0) {
                        append(title)
                        addStyle(
                            style = SpanStyle(color = primaryColor),
                            start = start,
                            end = start + highlighted.length,
                        )
                    } else {
                        append(title)
                    }
                },
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 44.sp,
                ),
                color = headlineColor,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = page.body,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 31.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = bodyColor,
            )
        }
    }
}

@Composable
private fun FloatingStoryToken(
    modifier: Modifier,
    size: androidx.compose.ui.unit.Dp,
    background: Color,
    tint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(24.dp))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.44f),
        )
    }
}

@Composable
private fun WelcomeFooter(
    currentPage: Int,
    pageCount: Int,
    buttonColor: Color,
    onNext: () -> Unit,
) {
    val progressActive = Color(0xFF111111)
    val progressInactive = Color(0xFF111111).copy(alpha = 0.14f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PagerIndicatorRow(
                currentPage = currentPage,
                pageCount = pageCount,
                activeColor = progressActive,
                inactiveColor = progressInactive,
            )

        }

        FilledIconButton(
            onClick = onNext,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .size(60.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = buttonColor,
                contentColor = Color.White,
            ),
        ) {
            Icon(
                imageVector = Heroicons.Outline.ArrowRight,
                contentDescription = "Next",
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun PagerIndicatorRow(
    currentPage: Int,
    pageCount: Int,
    activeColor: Color,
    inactiveColor: Color,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(pageCount) { page ->
            val targetWidth: Dp = if (page == currentPage) 30.dp else 12.dp
            val width by animateDpAsState(
                targetValue = targetWidth,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                label = "pagerWidth$page",
            )
            Box(
                modifier = Modifier
                    .size(width = width, height = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (page == currentPage) activeColor else inactiveColor),
            )
        }
    }
}
