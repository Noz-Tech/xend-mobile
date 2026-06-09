package com.noztek.xend.core.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BackTopBar(
    title: String,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Heroicons.Outline.ArrowLeft,
                    contentDescription = "Back",
                )
            }
        },
    )
}
