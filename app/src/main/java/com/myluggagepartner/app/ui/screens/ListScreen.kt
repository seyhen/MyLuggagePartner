package com.myluggagepartner.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import com.myluggagepartner.app.model.Category
import com.myluggagepartner.app.model.PackItem
import com.myluggagepartner.app.model.Trip
import com.myluggagepartner.app.ui.AppCheckbox
import com.myluggagepartner.app.ui.QtyStepper
import com.myluggagepartner.app.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun ListScreen(
    trip: Trip,
    onBack: () -> Unit,
    onToggle: (Long) -> Unit,
    onQty: (Long, Int) -> Unit,
    onAdd: (Category, String) -> Unit,
    onRemove: (PackItem) -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onReset: () -> Unit,
    onSaveTemplate: () -> Unit,
) {
    val c = AppTheme.colors
    var menuOpen by remember { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    val ready = trip.done == trip.total && trip.total > 0

    Column(Modifier.fillMaxSize().background(c.surface).navigationBarsPadding()) {
        // ——— En-tête : face avant de l'enveloppe ———
        Box(Modifier.fillMaxWidth().height(196.dp).background(Brush.linearGradient(trip.type.gradient()))) {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color(0x590A1420), 0.4f to Color.Transparent,
                        0.48f to Color.Transparent, 1f to Color(0xA60A1420),
                    ),
                ),
            )
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    GlassButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = Color.White, modifier = Modifier.size(19.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    GlassButton(onClick = { searchVisible = !searchVisible; if (!searchVisible) searchQuery = "" }) {
                        Icon(Icons.Default.Search, "Rechercher un objet", tint = Color.White, modifier = Modifier.size(19.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Box {
                        GlassButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.MoreVert, "Plus d'options", tint = Color.White, modifier = Modifier.size(19.dp))
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Renommer") },
                                leadingIcon = { Icon(Icons.Default.Edit, null) },
                                onClick = { menuOpen = false; onRename() },
                            )
                            DropdownMenuItem(
                                text = { Text("Dupliquer") },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                                onClick = { menuOpen = false; onDuplicate() },
                            )
                            DropdownMenuItem(
                                text = { Text("Sauvegarder comme modèle") },
                                leadingIcon = { Icon(Icons.Default.Bookmark, null) },
                                onClick = { menuOpen = false; onSaveTemplate() },
                            )
                            DropdownMenuItem(
                                text = { Text("Tout décocher") },
                                leadingIcon = { Icon(Icons.Default.RestartAlt, null) },
                                onClick = { menuOpen = false; onReset() },
                            )
                            DropdownMenuItem(
                                text = { Text("Partager") },
                                leadingIcon = { Icon(Icons.Default.Share, null) },
                                onClick = { menuOpen = false; onShare() },
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Supprimer", color = c.errorText) },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = c.errorText) },
                                onClick = { menuOpen = false; onDelete() },
                            )
                        }
                    }
                }
                Row(Modifier.padding(bottom = 16.dp), verticalAlignment = Alignment.Bottom) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(Radius.xs))
                            .background(Color(0x26FFFFFF))
                            .border(1.5.dp, Color(0x8CFFFFFF), RoundedCornerShape(Radius.xs)),
                        contentAlignment = Alignment.Center,
                    ) { Text(trip.type.code, style = DataMono, color = Color.White, fontSize = 12.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(trip.name, style = DisplayMedium, color = Color.White, fontSize = 27.sp)
                        Spacer(Modifier.height(3.dp))
                        val subtitle = buildString {
                            if (trip.dates.isNotBlank()) append("${trip.dates}  ·  ")
                            append(trip.type.label.uppercase())
                            if (trip.departureDateEpoch != null) append("  ·  RAPPEL ACTIF")
                        }
                        Text(subtitle, style = LabelStamp, color = Color(0xCCFFFFFF))
                    }
                }
            }
        }

        // ——— Bordereau : avancement ———
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (ready) {
                // Tampon d'oblitération — le geste signature de la valise bouclée.
                Box(
                    Modifier
                        .rotate(-6f)
                        .clip(RoundedCornerShape(Radius.xs))
                        .border(2.dp, c.reminder, RoundedCornerShape(Radius.xs))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                ) {
                    Text("PRÊTE AU DÉPART", style = LabelStamp, color = c.reminder, fontSize = 12.sp)
                }
                Spacer(Modifier.weight(1f))
                Text("${trip.total}/${trip.total}", style = DataMono, color = c.reminder, fontSize = 17.sp)
            } else {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("CONTENU VÉRIFIÉ", style = LabelStamp, color = c.onSurfaceVariant)
                        Spacer(Modifier.weight(1f))
                        Text("${trip.done}/${trip.total}", style = DataMono, color = c.onSurface, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    ProgressBar(trip.progress, Modifier.fillMaxWidth(), 12.dp, track = c.surfaceContainerLow, fill = c.primary)
                }
            }
        }

        // ——— Barre de recherche ———
        if (searchVisible) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(Radius.xs)).background(c.surfaceContainer)
                    .border(1.dp, c.outline, RoundedCornerShape(Radius.xs))
                    .padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Search, null, tint = c.onSurfaceVariant, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) Text("Rechercher un objet…", color = c.onSurfaceVariant, fontSize = 14.sp)
                    BasicTextField(
                        value = searchQuery, onValueChange = { searchQuery = it },
                        textStyle = LocalTextStyle.current.copy(color = c.onSurface, fontSize = 14.sp),
                        cursorBrush = SolidColor(c.primary), singleLine = true,
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    Box(
                        Modifier.size(48.dp).clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false, radius = 20.dp),
                            onClick = { searchQuery = "" },
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Close, "Effacer la recherche", tint = c.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // ——— Catégories ———
        val filteredItems = if (searchQuery.isBlank()) trip.items
            else trip.items.filter { it.name.contains(searchQuery, ignoreCase = true) }
        val noSearchResult = searchQuery.isNotBlank() && filteredItems.isEmpty()

        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp)) {
            if (noSearchResult) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(top = 48.dp, start = 24.dp, end = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(Icons.Default.Search, null, tint = c.onSurfaceVariant, modifier = Modifier.size(30.dp))
                        Spacer(Modifier.height(14.dp))
                        Text("AUCUN RÉSULTAT", style = LabelStamp, color = c.onSurface)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Rien ne correspond à « $searchQuery ».",
                            color = c.onSurfaceVariant, fontSize = 13.sp, textAlign = TextAlign.Center,
                        )
                    }
                }
                return@LazyColumn
            }

            Category.entries.forEach { cat ->
                val catItems = filteredItems.filter { it.category == cat }
                if (catItems.isEmpty()) return@forEach
                val allDone = catItems.all { it.checked }

                item(key = "hdr-${cat.name}") {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            cat.label.uppercase(),
                            style = CategoryTitle,
                            color = if (allDone) c.onSurfaceVariant else c.onSurface,
                        )
                        Spacer(Modifier.width(10.dp))
                        Box(Modifier.weight(1f).height(1.dp).background(c.outline))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "${catItems.count { it.checked }}/${catItems.size}",
                            style = DataMono,
                            color = if (allDone) c.primary else c.onSurfaceVariant,
                            fontSize = 12.sp,
                        )
                    }
                }

                if (!allDone) {
                    item(key = "body-${cat.name}") {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.md))
                                .background(c.surfaceContainer)
                                .border(1.dp, c.outline, RoundedCornerShape(Radius.md))
                                .padding(vertical = 4.dp),
                        ) {
                            val sorted = catItems.filterNot { it.checked } + catItems.filter { it.checked }
                            sorted.forEachIndexed { i, item ->
                                key(item.id) {
                                    if (i > 0) FormRule(Modifier.padding(horizontal = 12.dp))
                                    SwipeItem(
                                        item,
                                        onToggle = { onToggle(item.id) },
                                        onQty = { onQty(item.id, it) },
                                        onDismiss = { onRemove(item) },
                                    )
                                }
                            }
                            FormRule(Modifier.padding(horizontal = 12.dp))
                            AddItemRow { name -> onAdd(cat, name) }
                        }
                    }
                }
            }

            item {
                Text(
                    "← GLISSER VERS LA GAUCHE POUR RETIRER",
                    style = LabelStamp, color = c.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SwipeItem(item: PackItem, onToggle: () -> Unit, onQty: (Int) -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    val haptic = LocalHapticFeedback.current
    var offset by remember { mutableStateOf(0f) }
    val animated by animateFloatAsState(offset, label = "swipe")

    val toggleWithHaptic = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onToggle()
    }

    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier.matchParentSize().background(c.error).padding(end = 20.dp),
            contentAlignment = Alignment.CenterEnd,
        ) { Icon(Icons.Default.Delete, "Supprimer", tint = c.errorText, modifier = Modifier.size(19.dp)) }

        Row(
            Modifier
                .offset { IntOffset(animated.roundToInt(), 0) }
                .fillMaxWidth()
                .background(c.surfaceContainer)
                .pointerInput(item.id) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, delta -> offset = (offset + delta).coerceIn(-300f, 0f) },
                        onDragEnd = {
                            if (offset < -120f) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDismiss()
                            } else offset = 0f
                        },
                    )
                }
                .padding(start = 4.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppCheckbox(item.checked, toggleWithHaptic)
            Text(
                item.name,
                color = if (item.checked) c.onSurfaceVariant else c.onSurface,
                fontSize = 15.sp, fontWeight = FontWeight.Medium,
                textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Radius.xs))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = toggleWithHaptic,
                    )
                    .padding(vertical = 12.dp, horizontal = 4.dp)
                    .graphicsLayer { alpha = if (item.checked) 0.5f else 1f },
            )
            Spacer(Modifier.width(4.dp))
            QtyStepper(item.qty, onDelta = onQty)
        }
    }
}

@Composable
private fun AddItemRow(onAdd: (String) -> Unit) {
    val c = AppTheme.colors
    var text by rememberSaveable { mutableStateOf("") }
    val submit = {
        if (text.isNotBlank()) { onAdd(text); text = "" }
    }
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Add, null, tint = c.primary, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f).padding(vertical = 14.dp)) {
            if (text.isEmpty()) Text("Ajouter un objet", color = c.onSurfaceVariant, fontSize = 14.sp)
            BasicTextField(
                value = text, onValueChange = { text = it },
                textStyle = LocalTextStyle.current.copy(color = c.onSurface, fontSize = 14.sp),
                cursorBrush = SolidColor(c.primary), singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )
        }
        if (text.isNotBlank()) {
            Box(
                Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .clip(RoundedCornerShape(Radius.xs))
                    .clickable { submit() },
                contentAlignment = Alignment.Center,
            ) {
                Text("OK", style = LabelStamp, color = c.primary, fontSize = 12.sp)
            }
        }
    }
}

/** Bouton translucide sur l'en-tête photo — carré, comme une case tamponnée. */
@Composable
private fun GlassButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(Radius.xs))
            .background(Color(0x26FFFFFF))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) { content() }
}
