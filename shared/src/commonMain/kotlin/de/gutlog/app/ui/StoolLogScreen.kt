package de.gutlog.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import de.gutlog.shared.generated.resources.*
import de.gutlog.app.domain.MealEntry
import de.gutlog.app.domain.BuiltInFoodCatalog
import de.gutlog.app.domain.PortionSize
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource

@Composable
fun StoolLogScreen(state: StoolLogState, german: Boolean, mealState: MealLogState? = null) {
    val content by state.content.collectAsState()
    val draft by state.draft.collectAsState()
    val saving by state.saving.collectAsState()
    val saveError by state.saveError.collectAsState()
    val timeError by state.timeError.collectAsState()
    if (draft != null) {
        StoolEntryForm(requireNotNull(draft), saving, saveError, timeError, state)
    } else if (mealState != null) {
        val mealContent by mealState.content.collectAsState()
        val mealDraft by mealState.draft.collectAsState()
        if (mealDraft != null) {
            MealEntryForm(requireNotNull(mealDraft), mealState, german)
        } else {
            StoolList(content, german, state::openDraft, state::reload, mealState, mealContent)
        }
    } else {
        StoolList(content, german, state::openDraft, state::reload, null, null)
    }
}

@Composable
private fun StoolList(
    content: StoolLogContent,
    german: Boolean,
    openDraft: () -> Unit,
    retry: () -> Unit,
    mealState: MealLogState?,
    mealContent: MealLogContent?,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("stool-log"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Text(stringResource(Res.string.stool_log_title)) }
            when (content) {
                StoolLogContent.Loading -> item { Text(stringResource(Res.string.loading)) }
                StoolLogContent.Error -> item {
                    Text(stringResource(Res.string.load_error))
                    TextButton(onClick = retry) { Text(stringResource(Res.string.retry)) }
                }
                is StoolLogContent.Loaded -> {
                    if (content.entries.isEmpty()) {
                        item { Text(stringResource(Res.string.empty_log)) }
                    }
                    items(content.entries, key = { it.id }) { entry ->
                        val shapeDescription = stringResource(
                            if (entry.hasBlood) Res.string.bristol_log_shape_blood else Res.string.bristol_log_shape,
                            entry.bristolType,
                        )
                        Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
                            Text(formatEntryTime(entry.occurredAtEpochMillis, TimeZone.currentSystemDefault(), german))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BristolDrawing(
                                    type = entry.bristolType,
                                    hasBlood = entry.hasBlood,
                                    modifier = Modifier
                                        .testTag("stool-shape-${entry.id}")
                                        .semantics { contentDescription = shapeDescription },
                                )
                                Text("${stringResource(Res.string.bristol_title)}: ${entry.bristolType}")
                            }
                            if (entry.hasBlood) Text(stringResource(Res.string.blood))
                            if (entry.isUrgent) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UrgencyPictogram(Modifier.testTag("urgent-icon-${entry.id}"))
                                    Text(stringResource(Res.string.urgent), modifier = Modifier.padding(start = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
            if (mealState == null) {
                item {
                    Text(stringResource(Res.string.food_diary_title))
                    Text(stringResource(Res.string.food_diary_description))
                    Spacer(Modifier.height(96.dp))
                }
            } else {
                item { Text(stringResource(Res.string.food_diary_title)) }
                when (mealContent) {
                    null, MealLogContent.Loading -> item { Text(stringResource(Res.string.loading_meals)) }
                    MealLogContent.Error -> item {
                        Text(stringResource(Res.string.meal_load_error))
                        TextButton(onClick = mealState::reload) { Text(stringResource(Res.string.retry)) }
                    }
                    is MealLogContent.Loaded -> {
                        if (mealContent.entries.isEmpty()) item { Text(stringResource(Res.string.empty_meals)) }
                        items(mealContent.entries, key = { "meal-${it.id}" }) { entry ->
                            MealEntryCard(entry, german)
                        }
                    }
                }
                item { Spacer(Modifier.height(96.dp)) }
            }
        }
        Box(Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
            val label = stringResource(Res.string.add_entry)
            FloatingActionButton(
                onClick = { menuOpen = true },
                modifier = Modifier.semantics { contentDescription = label },
            ) { Text("+") }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(onClick = { menuOpen = false; openDraft() }, enabled = content is StoolLogContent.Loaded) {
                    Text(stringResource(Res.string.add_stool))
                }
                DropdownMenuItem(
                    onClick = { menuOpen = false; mealState?.openDraft() },
                    enabled = mealState != null && mealContent is MealLogContent.Loaded,
                ) {
                    Text(
                        if (mealState == null) {
                            "${stringResource(Res.string.add_food)} — ${stringResource(Res.string.coming_soon)}"
                        } else {
                            stringResource(Res.string.add_food)
                        },
                    )
                }
                DropdownMenuItem(onClick = {}, enabled = false) {
                    Text("${stringResource(Res.string.add_pain)} — ${stringResource(Res.string.coming_soon)}")
                }
            }
        }
    }
}

@Composable
private fun MealEntryCard(entry: MealEntry, german: Boolean) {
    Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Text(formatEntryTime(entry.occurredAtEpochMillis, TimeZone.currentSystemDefault(), german))
        entry.foods.forEach { food ->
            val name = BuiltInFoodCatalog.items
                .firstOrNull { it.id == food.catalogFoodId }
                ?.displayName(german)
                ?: food.name
            Text("$name — ${portionLabel(food.effectivePortion(entry.portionSize))}")
        }
        entry.note?.let { Text(it) }
    }
}

@Composable
private fun MealEntryForm(draft: MealDraft, state: MealLogState, german: Boolean) {
    val initial = requireNotNull(draft.localDateTime)
    var date by remember { mutableStateOf(initial.date.toString()) }
    var time by remember { mutableStateOf("${initial.hour.toString().padStart(2, '0')}:${initial.minute.toString().padStart(2, '0')}") }
    var query by remember { mutableStateOf("") }
    var invalid by remember { mutableStateOf(false) }
    val results by state.searchResults.collectAsState()
    val searching by state.searching.collectAsState()
    val searchError by state.searchError.collectAsState()
    val saving by state.saving.collectAsState()
    val saveError by state.saveError.collectAsState()
    val timeError by state.timeError.collectAsState()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(Res.string.meal_entry_title))
        OutlinedTextField(
            date,
            { date = it; invalid = false },
            label = { Text(stringResource(Res.string.date_label)) },
            enabled = !saving,
        )
        OutlinedTextField(
            time,
            { time = it; invalid = false },
            label = { Text(stringResource(Res.string.time_label)) },
            enabled = !saving,
        )
        if (invalid || timeError == EntryTimeError.INVALID_OR_AMBIGUOUS) {
            Text(stringResource(Res.string.invalid_time), color = Color.Red)
        }
        if (timeError == EntryTimeError.FUTURE) Text(stringResource(Res.string.future_time), color = Color.Red)
        OutlinedTextField(
            query,
            {
                query = it
                state.searchFoods(it)
            },
            label = { Text(stringResource(Res.string.food_search_label)) },
            enabled = !saving,
            modifier = Modifier.testTag("food-search"),
        )
        if (searching) Text(stringResource(Res.string.loading))
        if (searchError) Text(stringResource(Res.string.food_search_error), color = Color.Red)
        results.forEach { food ->
            TextButton(
                onClick = { state.addFood(food, german) },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth().testTag("catalog-food-${food.id}"),
            ) {
                Text(food.displayName(german))
            }
        }
        if (query.isNotBlank() && results.none { it.matchesExactly(query) }) {
            TextButton(
                onClick = { state.addCustomFood(query) },
                enabled = !saving,
                modifier = Modifier.testTag("add-custom-food"),
            ) {
                Text("${stringResource(Res.string.add_custom_food)}: ${query.trim()}")
            }
        }
        Text(stringResource(Res.string.selected_foods))
        draft.foods.forEach { food ->
            Column(Modifier.fillMaxWidth().testTag("meal-food-${food.id}")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(food.name, modifier = Modifier.weight(1f))
                    TextButton(onClick = { state.removeFood(food.id) }, enabled = !saving) {
                        Text(stringResource(Res.string.remove_food))
                    }
                }
                Text(stringResource(Res.string.food_portion, food.name))
                PortionSelector(
                    selected = food.effectivePortion(draft.portionSize),
                    enabled = !saving,
                    onSelect = { state.setFoodPortion(food.id, it) },
                    tagPrefix = "food-portion-${food.id}",
                )
                TextButton(
                    onClick = { state.setFoodPortion(food.id, null) },
                    enabled = !saving && food.portionOverride != null,
                ) {
                    Text(stringResource(Res.string.use_meal_portion))
                }
            }
        }
        Text(stringResource(Res.string.meal_portion))
        PortionSelector(
            selected = draft.portionSize,
            enabled = !saving,
            onSelect = state::setPortionSize,
            tagPrefix = "meal-portion",
        )
        if (saveError) Text(stringResource(Res.string.save_error), color = Color.Red)
        Row {
            TextButton(
                onClick = state::cancelDraft,
                enabled = !saving,
                modifier = Modifier.testTag("cancel-meal"),
            ) {
                Text(stringResource(Res.string.cancel))
            }
            Button(
                enabled = !saving && draft.foods.isNotEmpty(),
                onClick = {
                    val parsed = try {
                        if (!Regex("""\d{4}-\d{2}-\d{2}""").matches(date) || !Regex("""\d{2}:\d{2}""").matches(time)) null
                        else LocalDateTime.parse("${date}T$time:00")
                    } catch (error: IllegalArgumentException) {
                        null
                    }
                    if (parsed == null) invalid = true
                    else {
                        invalid = false
                        state.setDateTime(parsed)
                        state.saveDraft()
                    }
                },
            ) {
                Text(stringResource(Res.string.save))
            }
        }
    }
}

@Composable
private fun PortionSelector(
    selected: PortionSize?,
    enabled: Boolean,
    onSelect: (PortionSize) -> Unit,
    tagPrefix: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        PortionSize.entries.forEach { size ->
            val label = portionLabel(size)
            Row(
                modifier = Modifier
                    .selectable(selected == size, enabled = enabled, role = Role.RadioButton) { onSelect(size) }
                    .testTag("$tagPrefix-${size.name}"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected == size, onClick = null, enabled = enabled)
                Text(label)
            }
        }
    }
}

@Composable
private fun portionLabel(size: PortionSize): String = stringResource(
    when (size) {
        PortionSize.SMALL -> Res.string.portion_small
        PortionSize.MEDIUM -> Res.string.portion_medium
        PortionSize.LARGE -> Res.string.portion_large
    },
)

@Composable
private fun StoolEntryForm(
    draft: StoolDraft,
    saving: Boolean,
    saveError: Boolean,
    timeError: EntryTimeError?,
    state: StoolLogState,
) {
    val initial = requireNotNull(draft.localDateTime)
    var date by remember { mutableStateOf(initial.date.toString()) }
    var time by remember { mutableStateOf("${initial.hour.toString().padStart(2, '0')}:${initial.minute.toString().padStart(2, '0')}") }
    var invalid by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(Res.string.add_stool))
        OutlinedTextField(date, { date = it; invalid = false }, label = { Text(stringResource(Res.string.date_label)) }, enabled = !saving)
        OutlinedTextField(time, { time = it; invalid = false }, label = { Text(stringResource(Res.string.time_label)) }, enabled = !saving)
        if (invalid || timeError == EntryTimeError.INVALID_OR_AMBIGUOUS) Text(stringResource(Res.string.invalid_time), color = Color.Red)
        if (timeError == EntryTimeError.FUTURE) Text(stringResource(Res.string.future_time), color = Color.Red)
        Text(stringResource(Res.string.bristol_title))
        BristolSelector(draft.bristolType, !saving, state::selectType)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.toggleable(draft.hasBlood, enabled = !saving, role = Role.Checkbox, onValueChange = state::setBlood)) {
            Checkbox(draft.hasBlood, onCheckedChange = null, enabled = !saving)
            Text(stringResource(Res.string.blood))
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.toggleable(draft.isUrgent, enabled = !saving, role = Role.Checkbox, onValueChange = state::setUrgent)) {
            Checkbox(draft.isUrgent, onCheckedChange = null, enabled = !saving)
            Text(stringResource(Res.string.urgent))
        }
        if (saveError) Text(stringResource(Res.string.save_error), color = Color.Red)
        Row {
            TextButton(onClick = state::cancelDraft, enabled = !saving) { Text(stringResource(Res.string.cancel)) }
            Button(
                enabled = !saving && draft.bristolType != null,
                onClick = {
                    val parsed = try {
                        if (!Regex("""\d{4}-\d{2}-\d{2}""").matches(date) || !Regex("""\d{2}:\d{2}""").matches(time)) null
                        else LocalDateTime.parse("${date}T$time:00")
                    } catch (error: IllegalArgumentException) {
                        null
                    }
                    if (parsed == null) invalid = true
                    else {
                        invalid = false
                        state.setDateTime(parsed)
                        state.saveDraft()
                    }
                },
            ) { Text(stringResource(Res.string.save)) }
        }
    }
}

@Composable
private fun BristolSelector(selected: Int?, enabled: Boolean, onSelect: (Int) -> Unit) {
    val descriptions = listOf(
        stringResource(Res.string.bristol_1), stringResource(Res.string.bristol_2),
        stringResource(Res.string.bristol_3), stringResource(Res.string.bristol_4),
        stringResource(Res.string.bristol_5), stringResource(Res.string.bristol_6),
        stringResource(Res.string.bristol_7),
    )
    descriptions.forEachIndexed { index, description ->
        val type = index + 1
        Row(
            modifier = Modifier.fillMaxWidth().selectable(selected == type, enabled = enabled, role = Role.RadioButton) { onSelect(type) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected == type, onClick = null, enabled = enabled)
            BristolDrawing(type)
            Text("$type: $description", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun BristolDrawing(type: Int, hasBlood: Boolean = false, modifier: Modifier = Modifier) {
    val shapeColor = if (hasBlood) Color(0xFFC62828) else Color(0xFF79513B)
    Canvas(modifier.size(width = 70.dp, height = 42.dp)) {
        val w = size.width
        val h = size.height
        when (type) {
            1 -> listOf(.15f to .35f, .35f to .65f, .5f to .3f, .7f to .6f, .85f to .3f)
                .forEach { (x, y) -> drawCircle(shapeColor, h * .11f, Offset(w * x, h * y)) }
            2 -> {
                drawRoundRect(shapeColor, Offset(w * .12f, h * .3f), Size(w * .76f, h * .4f), androidx.compose.ui.geometry.CornerRadius(h * .2f))
                (0..5).forEach { drawCircle(shapeColor, h * .23f, Offset(w * (.18f + it * .13f), h * .5f)) }
            }
            3, 4 -> {
                drawRoundRect(shapeColor, Offset(w * .08f, h * .3f), Size(w * .84f, h * .4f), androidx.compose.ui.geometry.CornerRadius(h * .2f))
                if (type == 3) (1..3).forEach {
                    val x = w * (.2f * it + .1f)
                    drawLine(Color(0xFFEEDCCD), Offset(x, h * .3f), Offset(x - w * .05f, h * .48f), strokeWidth = h * .035f)
                }
            }
            5 -> listOf(.2f to .35f, .42f to .6f, .67f to .32f, .82f to .65f)
                .forEach { (x, y) -> drawOval(shapeColor, Offset(w * x - h * .15f, h * y - h * .12f), Size(h * .3f, h * .24f)) }
            6 -> {
                val path = Path().apply {
                    moveTo(w * .08f, h * .65f)
                    lineTo(w * .18f, h * .3f); lineTo(w * .32f, h * .45f); lineTo(w * .45f, h * .2f)
                    lineTo(w * .58f, h * .42f); lineTo(w * .75f, h * .25f); lineTo(w * .92f, h * .62f)
                    lineTo(w * .75f, h * .78f); lineTo(w * .5f, h * .67f); lineTo(w * .28f, h * .8f); close()
                }
                drawPath(path, shapeColor)
            }
            7 -> drawOval(shapeColor, Offset(w * .08f, h * .4f), Size(w * .84f, h * .3f))
        }
    }
}

@Composable
private fun UrgencyPictogram(modifier: Modifier = Modifier) {
    Canvas(modifier.size(20.dp)) {
        drawCircle(Color(0xFFE65100))
        drawLine(
            color = Color.White,
            start = Offset(size.width / 2, size.height * .25f),
            end = Offset(size.width / 2, size.height * .58f),
            strokeWidth = size.width * .12f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
        drawCircle(Color.White, radius = size.width * .07f, center = Offset(size.width / 2, size.height * .76f))
    }
}
