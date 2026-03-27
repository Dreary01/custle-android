@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ru.custle.mobile.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.custle.mobile.core.model.SearchResultDto
import ru.custle.mobile.core.ui.components.AppHeroCard
import ru.custle.mobile.core.ui.components.AppSectionCard
import ru.custle.mobile.core.ui.components.EmptyStateCard

@Composable
fun SearchScreen(
    lastQuery: String,
    results: List<SearchResultDto>,
    onSearch: (String) -> Unit,
    onOpenResult: (String, String) -> Unit,
) {
    var query by rememberSaveable(lastQuery) { mutableStateOf(lastQuery) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SearchHero(resultsCount = results.size)
        }
        item {
            AppSectionCard(
                title = "Поисковый запрос",
                hint = "Один явный ввод и один явный action. Без лишних фильтров на первом экране.",
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    label = { Text("Что ищем") },
                )
                Button(
                    onClick = { onSearch(query.trim()) },
                    enabled = query.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Искать")
                }
            }
        }
        if (lastQuery.isNotBlank()) {
            item {
                AppSectionCard(
                    title = if (results.isEmpty()) "По запросу \"$lastQuery\" ничего не найдено" else "Результаты по запросу \"$lastQuery\"",
                    hint = if (results.isEmpty()) "Попробуй уточнить название объекта, документа, обсуждения или задачи." else "Открой нужный результат прямо из списка ниже.",
                ) {
                    Text(
                        text = if (results.isEmpty()) "0 результатов" else "${results.size} результатов",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (results.isEmpty()) {
            item {
                SearchEmptyState(hasQuery = query.isNotBlank() || lastQuery.isNotBlank())
            }
        } else {
            items(results, key = { "${it.type}-${it.id}" }) { item ->
                SearchResultCard(item = item, onOpenResult = onOpenResult)
            }
        }
    }
}

@Composable
private fun SearchHero(
    resultsCount: Int,
) {
    AppHeroCard(
        title = "Глобальный поиск",
        subtitle = "Один поиск по объектам, документам, обсуждениям и задачам. С телефона здесь важны быстрый вход и понятный тип результата.",
        chips = listOf(
            "${resultsCount} результатов" to Icons.Outlined.Search,
            "Объекты" to Icons.Outlined.Source,
            "Документы" to Icons.Outlined.Description,
            "Обсуждения" to Icons.Outlined.Forum,
        ),
    )
}

@Composable
private fun SearchResultCard(
    item: SearchResultDto,
    onOpenResult: (String, String) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenResult(item.id, item.type) },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                item.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    listOfNotNull(resultTypeLabel(item.type), item.source.takeIf { it.isNotBlank() }).joinToString(" • "),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            if (item.snippet.isNotBlank()) {
                Text(
                    item.snippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SearchEmptyState(
    hasQuery: Boolean,
) {
    EmptyStateCard(
        title = if (hasQuery) "Ничего не найдено" else "Поиск ждёт запрос",
        message = if (hasQuery) {
            "Попробуй искать по названию объекта, фрагменту документа, имени участника или теме обсуждения."
        } else {
            "Введи запрос выше. Хороший мобильный сценарий тут должен начинаться с одного поля, а не с набора фильтров."
        },
    )
}

@Composable
private fun SearchMetaChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    AssistChip(
        onClick = {},
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
    )
}

private fun resultTypeLabel(type: String): String =
    when (type.lowercase()) {
        "object" -> "Объект"
        "document" -> "Документ"
        "discussion" -> "Обсуждение"
        "todo" -> "Задача"
        else -> type.ifBlank { "Результат" }
    }
