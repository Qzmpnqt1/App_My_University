package com.example.app_my_university.ui.components.picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.app_my_university.ui.designsystem.AppSpacing

data class PickerListItem(
    val id: Long,
    val primary: String,
    val secondary: String? = null,
)

/**
 * Bottom sheet с поиском для выбора сущности по человекочитаемым полям (id только внутри [PickerListItem]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuSearchablePickerSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    items: List<PickerListItem>,
    onSelect: (PickerListItem) -> Unit,
    searchPlaceholder: String = "Поиск…",
    /** Пока идёт загрузка и список ещё пуст — индикатор вместо «Ничего не найдено». */
    listLoading: Boolean = false,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember(visible) { mutableStateOf("") }

    LaunchedEffect(visible) {
        if (visible) query = ""
    }

    val q = query.trim().lowercase()
    // Не используем remember(items): пустой список часто тот же singleton emptyList() —
    // после загрузки данных ключ не меняется и список в sheet остаётся пустым.
    val filtered = if (q.isEmpty()) {
        items
    } else {
        items.filter { row ->
            row.primary.lowercase().contains(q) ||
                (row.secondary?.lowercase()?.contains(q) == true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.m)
                .padding(bottom = AppSpacing.l),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = AppSpacing.xs),
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(searchPlaceholder) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (listLoading && items.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(vertical = AppSpacing.l)
                        .size(40.dp),
                )
            } else if (filtered.isEmpty()) {
                Text(
                    text = if (q.isNotEmpty()) {
                        "Ничего не найдено"
                    } else {
                        "Список пуст. Проверьте права доступа и данные на сервере."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = AppSpacing.l),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = AppSpacing.s),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(filtered, key = { it.id }) { row ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = row.primary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            },
                            supportingContent = row.secondary?.let { sub ->
                                {
                                    Text(
                                        text = sub,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(row)
                                    onDismiss()
                                },
                        )
                    }
                }
            }
        }
    }
}
