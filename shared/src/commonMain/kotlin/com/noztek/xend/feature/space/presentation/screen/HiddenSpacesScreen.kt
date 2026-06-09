package com.noztek.xend.feature.space.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.noztek.xend.core.ui.components.AppButton
import com.noztek.xend.core.ui.components.AppOutlinedButton
import com.noztek.xend.core.ui.components.AppTextField
import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel
import com.noztek.xend.feature.space.presentation.viewmodel.HiddenSpacesViewModel
import org.koin.compose.koinInject

@Composable
fun HiddenSpacesScreen(
    onUnlocked: (String) -> Unit,
) {
    val vm = koinInject<HiddenSpacesViewModel>()
    val state by vm.state.collectAsState()
    var passphrase by remember { mutableStateOf("") }
    val editModes = remember { mutableStateMapOf<String, Boolean>() }
    val hints = remember { mutableStateMapOf<String, String>() }
    val secrets = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(state.unlockedSpaceId) {
        val unlocked = state.unlockedSpaceId ?: return@LaunchedEffect
        onUnlocked(unlocked)
        vm.clearUnlockedSpace()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Access hidden space",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    AppTextField(
                        value = passphrase,
                        onValueChange = { passphrase = it },
                        label = "Passphrase",
                    )
                    AppButton(
                        text = "Unlock",
                        onClick = { vm.unlock(passphrase) },
                        enabled = passphrase.isNotBlank(),
                    )
                }
            }

            if (!state.message.isNullOrBlank()) {
                item {
                    Text(
                        text = state.message.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                Text(
                    text = "Hidden spaces",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            items(state.items, key = { it.relationshipSpaceId }) { item ->
                val isEditing = editModes[item.relationshipSpaceId] == true
                HiddenSpaceCard(
                    item = item,
                    hint = hints[item.relationshipSpaceId] ?: item.accessHint.orEmpty(),
                    passphrase = secrets[item.relationshipSpaceId].orEmpty(),
                    isEditing = isEditing,
                    onHintChange = { hints[item.relationshipSpaceId] = it },
                    onPassphraseChange = { secrets[item.relationshipSpaceId] = it },
                    onSetDefault = { vm.setDefault(item.relationshipSpaceId) },
                    onToggleEdit = { editModes[item.relationshipSpaceId] = !isEditing },
                    onSaveAccess = {
                        vm.saveAccess(
                            spaceId = item.relationshipSpaceId,
                            passphrase = secrets[item.relationshipSpaceId].orEmpty(),
                            hint = hints[item.relationshipSpaceId].takeUnless { it.isNullOrBlank() },
                        )
                        editModes[item.relationshipSpaceId] = false
                    },
                )
            }
        }
    }
}

@Composable
private fun HiddenSpaceCard(
    item: RelationshipSpaceCardModel,
    hint: String,
    passphrase: String,
    isEditing: Boolean,
    onHintChange: (String) -> Unit,
    onPassphraseChange: (String) -> Unit,
    onSetDefault: () -> Unit,
    onToggleEdit: () -> Unit,
    onSaveAccess: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow, shape = MaterialTheme.shapes.large)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = item.accessHint?.takeIf { it.isNotBlank() }
                ?: if (item.accessConfigured) "Secret configured" else "No secret yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppOutlinedButton(
                text = "Make Default",
                onClick = onSetDefault,
                modifier = Modifier.weight(1f),
            )
            AppOutlinedButton(
                text = if (item.accessConfigured) "Edit Secret" else "Set Secret",
                onClick = onToggleEdit,
                modifier = Modifier.weight(1f),
            )
        }
        if (isEditing) {
            Spacer(modifier = Modifier.height(4.dp))
            AppTextField(
                value = hint,
                onValueChange = onHintChange,
                label = "Hint",
            )
            AppTextField(
                value = passphrase,
                onValueChange = onPassphraseChange,
                label = "Passphrase",
            )
            AppButton(
                text = "Save Secret",
                onClick = onSaveAccess,
                enabled = passphrase.length >= 4,
            )
        }
    }
}
