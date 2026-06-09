package com.noztek.xend.feature.space.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.noztek.xend.feature.space.presentation.viewmodel.SpaceDetailsViewModel
import org.koin.compose.koinInject

@Composable
fun SpaceDetailScreen(
    spaceId: String,
    onMessageClick: (String) -> Unit,
) {
    val vm = koinInject<SpaceDetailsViewModel>()
    val state by vm.state.collectAsState()

    LaunchedEffect(spaceId) {
        vm.load(spaceId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        val item = state.defaultSpace
        if (item == null) {
            Text(
                text = state.message ?: "Space not found",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        } else {
            DefaultSpaceCard(
                item = item,
                onMessageClick = { onMessageClick(item.conversationId) },
            )
        }
    }
}
