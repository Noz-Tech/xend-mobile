package com.noztek.xend

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.noztek.xend.app.AppNavHost
import com.noztek.xend.core.ui.theme.AppTheme

@Composable
@Preview
fun App() {
    AppTheme {
        AppNavHost()
    }
}
