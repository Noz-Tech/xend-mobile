package com.noztek.xend.feature.welcome.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowRight
import com.composables.icons.heroicons.outline.ArrowTopRightOnSquare
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.Heart
import com.composables.icons.heroicons.outline.LockClosed
import com.composables.icons.heroicons.outline.ShieldCheck
import com.composables.icons.heroicons.outline.Sparkles
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import xend.shared.generated.resources.Res
import xend.shared.generated.resources.couple_1
import xend.shared.generated.resources.logo
import xend.shared.generated.resources.orbit_1
import xend.shared.generated.resources.orbit_2
import xend.shared.generated.resources.orbit_3
import kotlin.math.PI
import kotlin.math.roundToInt

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

private const val STORAGE_INFO_URL = "https://en.wikipedia.org/wiki/End-to-end_encryption"

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
                title = "Your private space\nfor two",
                highlightedTitlePart = "for two",
                body = "A secure place to message, play, share moments, and stay close with the one person who matters most.",
                action = "Continue",
                accent = Color(0xFF2E3A59),
                accentSoft = Color(0xFFFFEAA6),
                icon = Heroicons.Outline.Heart,
            ),
            OnboardingPage(
                title = "Only you two\nbelong here",
                highlightedTitlePart = "belong here",
                body = "Xend is built for private conversations, honest moments, silly talks, and memories you don't want mixed with the noise of everywhere else.",
                action = "Continue",
                accent = Color(0xFF26385C),
                accentSoft = Color(0xFFFFE6A0),
                icon = Heroicons.Outline.LockClosed,
            ),
            OnboardingPage(
                title = "Create your\nspace together",
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
    val uriHandler = LocalUriHandler.current

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
        Column(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { uriHandler.openUri(STORAGE_INFO_URL) }
                    .background(
                        color = primaryColor.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Heroicons.Outline.ShieldCheck,
                    contentDescription = null,
                    tint = primaryColor.copy(alpha = 0.82f),
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = "Zero-Knowledge Storage",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = primaryColor.copy(alpha = 0.92f),
                )
                Icon(
                    imageVector = Heroicons.Outline.ArrowTopRightOnSquare,
                    contentDescription = "Open link",
                    tint = primaryColor.copy(alpha = 0.76f),
                    modifier = Modifier.size(12.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
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
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Start a private place where your messages, memories, and playful moments can grow together.",
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
            ),
            color = bodyColor,
        )
        Spacer(modifier = Modifier.weight(1f))
        SwipeToStartButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            text = "Get Started",
            containerColor = primaryColor,
            onSwiped = onStart,
        )
    }
}

@Composable
private fun SwipeToStartButton(
    modifier: Modifier = Modifier,
    text: String,
    containerColor: Color,
    onSwiped: () -> Unit,
) {
    val thumbSize = 50.dp
    val outerPadding = 5.dp
    val textStartInset = 14.dp
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var buttonWidthPx by remember { mutableFloatStateOf(0f) }
    var textWidthPx by remember { mutableFloatStateOf(0f) }
    var isCompleted by remember { mutableStateOf(false) }
    val dragOffset = remember { Animatable(0f) }
    val thumbSizePx = with(density) { thumbSize.toPx() }
    val outerPaddingPx = with(density) { outerPadding.toPx() }
    val textStartInsetPx = with(density) { textStartInset.toPx() }
    val maxDragPx = remember(buttonWidthPx, thumbSizePx, outerPaddingPx) {
        (buttonWidthPx - thumbSizePx - (outerPaddingPx * 2f)).coerceAtLeast(0f)
    }
    val swipeProgress = if (maxDragPx > 0f) {
        (dragOffset.value / maxDragPx).coerceIn(0f, 1f)
    } else {
        0f
    }
    val innerWidthPx = (buttonWidthPx - (outerPaddingPx * 2f)).coerceAtLeast(0f)
    val textEndTranslation = if (innerWidthPx > 0f && textWidthPx > 0f) {
        -((innerWidthPx - textWidthPx) / 2f - textStartInsetPx)
    } else {
        0f
    }
    val textTranslationX = lerpValue(
        start = 0f,
        end = textEndTranslation,
        fraction = swipeProgress,
    )
    val chevronTransition = rememberInfiniteTransition(label = "swipeStartChevron")
    val chevronWaveProgress by chevronTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1150, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "swipeStartChevronWave",
    )

    Box(
        modifier = modifier
            .onSizeChanged { buttonWidthPx = it.width.toFloat() }
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .padding(horizontal = outerPadding),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .align(Alignment.Center)
                .onSizeChanged { textWidthPx = it.width.toFloat() }
                .graphicsLayer { translationX = textTranslationX },
        )

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            horizontalArrangement = Arrangement.spacedBy((-5).dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { index ->
                val phase = positiveModulo(
                    value = chevronWaveProgress - (index * 0.16f),
                    mod = 1f,
                )
                val emphasis = 1f - kotlin.math.abs((phase * 2f) - 1f)
                val chevronAlpha = lerpValue(
                    start = 0.24f,
                    end = 1f,
                    fraction = emphasis,
                )
                Icon(
                    imageVector = Heroicons.Outline.ChevronRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = chevronAlpha),
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = dragOffset.value.roundToInt(),
                        y = 0,
                    )
                }
                .size(thumbSize)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .pointerInput(maxDragPx, isCompleted) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            if (!isCompleted) {
                                val newValue = (dragOffset.value + dragAmount).coerceIn(0f, maxDragPx)
                                scope.launch {
                                    dragOffset.snapTo(newValue)
                                }
                            }
                        },
                        onDragEnd = {
                            val shouldComplete = dragOffset.value >= maxDragPx * 0.80f
                            if (shouldComplete && !isCompleted) {
                                isCompleted = true
                                scope.launch {
                                    dragOffset.animateTo(
                                        targetValue = maxDragPx,
                                        animationSpec = tween(
                                            durationMillis = 180,
                                            easing = FastOutSlowInEasing,
                                        ),
                                    )
                                    onSwiped()
                                }
                            } else {
                                scope.launch {
                                    dragOffset.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium,
                                        ),
                                    )
                                }
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Heroicons.Outline.ArrowRight,
                contentDescription = "Swipe to start",
                tint = containerColor,
                modifier = Modifier.size(22.dp),
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

private fun positiveModulo(
    value: Float,
    mod: Float,
): Float = ((value % mod) + mod) % mod

@Composable
private fun IllustrationPlaceholder(
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    val couplePainter = painterResource(Res.drawable.couple_1)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp),
        contentAlignment = Alignment.Center,
    ) {
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
            Image(
                painter = couplePainter,
                contentDescription = "Couple illustration",
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .height(240.dp),
                contentScale = ContentScale.Fit,
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
