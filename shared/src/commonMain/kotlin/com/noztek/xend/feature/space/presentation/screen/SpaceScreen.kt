package com.noztek.xend.feature.space.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ChatBubbleOvalLeftEllipsis
import com.composables.icons.heroicons.outline.Sparkles
import com.composables.icons.heroicons.outline.UserPlus
import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel
import com.noztek.xend.feature.space.presentation.viewmodel.SpaceViewModel
import org.koin.compose.koinInject

@Composable
fun SpaceScreen(
    onInviteClick: () -> Unit = {},
    onMessageClick: (String) -> Unit = {},
) {
    val vm = koinInject<SpaceViewModel>()
    val state by vm.state.collectAsState()
    val defaultSpace = state.defaultSpace

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AssistChip(
                    onClick = {},
                    shape = RoundedCornerShape(20.dp),
                    label = {
                        Text(
                            if (defaultSpace != null) "Current Space: ${defaultSpace.name}" else "No space yet",
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Heroicons.Outline.Sparkles,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
            }

            if (defaultSpace == null) {
                item {
                    val message = when {
                        state.isLoading -> "Loading spaces..."
                        !state.message.isNullOrBlank() -> state.message!!
                        else -> "Send an invite to get started."
                    }
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                item {
                    DefaultSpaceCard(
                        item = defaultSpace,
                        onMessageClick = { onMessageClick(defaultSpace.conversationId) },
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
        ) {
            SpaceActions(onInviteClick = onInviteClick)
        }
    }
}

@Composable
private fun SpaceActions(
    onInviteClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End,
    ) {
        FloatingActionButton(
            onClick = onInviteClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Icon(
                imageVector = Heroicons.Outline.UserPlus,
                contentDescription = "Invite",
            )
        }
    }
}

@Composable
internal fun DefaultSpaceCard(
    item: RelationshipSpaceCardModel,
    onMessageClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.name.take(1),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Level ${item.currentLevel} - ${item.currentLevelName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${item.currentPoints}/${item.requiredPoints}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Open your private chat",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onMessageClick,
                    enabled = item.conversationId.isNotBlank(),
                ) {
                    if (item.unreadCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(if (item.unreadCount > 99) "99+" else item.unreadCount.toString())
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Heroicons.Outline.ChatBubbleOvalLeftEllipsis,
                                contentDescription = "Messages",
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Heroicons.Outline.ChatBubbleOvalLeftEllipsis,
                            contentDescription = "Messages",
                        )
                    }
                }
            }
        }
    }
}
