package com.noztek.xend.core.ui.share

import androidx.compose.runtime.Composable

@Composable
expect fun rememberTextShareLauncher(): (String) -> Unit
