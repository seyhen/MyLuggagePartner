package com.myluggagepartner.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myluggagepartner.app.model.Category
import com.myluggagepartner.app.model.PackItem
import com.myluggagepartner.app.model.Trip
import com.myluggagepartner.app.ui.AppCheckbox
import com.myluggagepartner.app.ui.QtyStepper
import com.myluggagepartner.app.ui.blobShape
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
) {
    val c = AppTheme.colors
    var menuOpen by remember { mutableStateOf(false) }
    val ready = trip.done == trip.total && trip.total > 0

    Column(Modifier.fillMaxSize().background(c.surface)) {
        // ——— En-tête photo ———
        Box(Modifier.fillMaxWidth().height(210.dp).background(Brush.linearGradient(trip.type.gradient()))) {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color(0x66140C08), 0.4f to Color.Transparent,
                        0.48f to Color.Transparent, 1f to Color(0x99140C08),
                    ),
                ),
            )
            Column(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Row(Modifier.fillMaxWidth().padding(top = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                    GlassButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    Box {
                        GlassButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.MoreVert, "Menu", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(text = { Text("✏️  Renommer") }, onClick = { menuOpen = false; onRename() })
                            DropdownMenuItem(text = { Text("📑  Dupliquer") }, onClick = { menuOpen = false; onDuplicate() })
                            DropdownMenuItem(text = { Text("📤  Partager") }, onClick = { menuOpen = false; onShare() })
                            DropdownMenuItem(text = { Text("🗑️  Supprimer", color = c.errorText) }, onClick = { menuOpen = false; onDelete() })
                        }
                    }
                }
                Column(Modifier.padding(start = 8.dp, bottom = 16.dp)) {
                    Text(trip.name, style = DisplayMedium, color = Color.White, fontSize = 30.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("${trip.dates} · ${trip.type.label}", color = Color(0xD1FFFFFF), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // ——— Blob progression ———
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(64.dp).clip(blobShape()).background(c.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) { Text("${trip.done}/${trip.total}", style = TitleCard, color = c.onSecondaryContainer, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(if (ready) "Valise prête ! 🎉" else "Articles prêts", color = c.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                ProgressBar(trip.progress, Modifier.fillMaxWidth(), 16.dp, c.surfaceContainerLow, c.primary)
            }
            Spacer(Modifier.width(12.dp))
            Text("${(trip.progress * 100).roundToInt()}%", style = TitleCard, color = c.primary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }

        // ——— Catégories ———
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)) {
            Category.entries.forEach { cat ->
                val catItems = trip.items.filter { it.category == cat }
                if (catItems.isEmpty()) return@forEach
                val allDone = catItems.all { it.checked }

                if (allDone) {
                    item(key = "compact-${cat.name}") {
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(CircleShape)
                                .background(c.surfaceContainer).padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("${cat.emoji} ${cat.name}", color = c.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            Row(
                                Modifier.clip(CircleShape).background(c.secondaryContainer).padding(horizontal = 12.dp, vertical = 4.dp),
                            ) { Text("${catItems.size}/${catItems.size} ✓", color = c.onSecondaryContainer, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                } else {
                    item(key = "hdr-${cat.name}") {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${cat.emoji} ${cat.name}", style = CategoryTitle, color = c.onSurface)
                            Spacer(Modifier.weight(1f))
                            Row(Modifier.clip(CircleShape).background(c.surface).padding(horizontal = 10.dp, vertical = 4.dp)) {
                                Text("${catItems.count { it.checked }}/${catItems.size}", color = c.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    item(key = "body-${cat.name}") {
                        Column(
                            Modifier.fillMaxWidth().padding(bottom = 16.dp).clip(RoundedCornerShape(28.dp))
                                .background(c.surfaceContainer).padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 10.dp),
                        ) {
                            val sorted = catItems.filterNot { it.checked } + catItems.filter { it.checked }
                            sorted.forEach { item ->
                                key(item.id) {
                                    SwipeItem(item, onToggle = { onToggle(item.id) }, onQty = { onQty(item.id, it) }, onDismiss = { onRemove(item) })
                                }
                            }
                            AddItemRow { name -> onAdd(cat, name) }
                        }
                    }
                }
            }
            item {
                Text("← Glisser pour retirer un objet →", color = c.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

@Composable
private fun SwipeItem(item: PackItem, onToggle: () -> Unit, onQty: (Int) -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    var offset by remember { mutableStateOf(0f) }
    val animated by animateFloatAsState(offset, label = "swipe")

    Box(Modifier.fillMaxWidth().padding(vertical = 1.dp).clip(RoundedCornerShape(18.dp))) {
        // Fond delete
        Box(
            Modifier.matchParentSize().background(c.error).padding(end = 20.dp),
            contentAlignment = Alignment.CenterEnd,
        ) { Icon(Icons.Default.Delete, "Supprimer", tint = c.errorText, modifier = Modifier.size(20.dp)) }

        Row(
            Modifier
                .offset { IntOffset(animated.roundToInt(), 0) }
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(c.surfaceContainer)
                .pointerInput(item.id) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, delta -> offset = (offset + delta).coerceIn(-160f, 0f) },
                        onDragEnd = { if (offset < -180f) onDismiss() else offset = 0f },
                    )
                }
                .padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppCheckbox(item.checked, onToggle)
            Spacer(Modifier.width(12.dp))
            Text(
                item.name,
                color = if (item.checked) c.onSurfaceVariant else c.onSurface,
                fontSize = 15.sp, fontWeight = FontWeight.Medium,
                textDecoration = if (item.checked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                modifier = Modifier.weight(1f).graphicsLayer { alpha = if (item.checked) 0.55f else 1f },
            )
            Spacer(Modifier.width(8.dp))
            QtyStepper(item.qty, onDelta = onQty)
        }
    }
}

@Composable
private fun AddItemRow(onAdd: (String) -> Unit) {
    val c = AppTheme.colors
    var text by remember { mutableStateOf("") }
    Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f).clip(CircleShape).background(c.surface).padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("＋", color = c.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Box(Modifier.weight(1f)) {
                if (text.isEmpty()) Text("Ajouter", color = c.onSurfaceVariant, fontSize = 14.sp)
                BasicTextField(
                    value = text, onValueChange = { text = it },
                    textStyle = LocalTextStyle.current.copy(color = c.onSurface, fontSize = 14.sp),
                    cursorBrush = SolidColor(c.primary), singleLine = true,
                )
            }
        }
        if (text.isNotBlank()) {
            Spacer(Modifier.width(8.dp))
            Text(
                "OK", color = c.primary, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp,
                modifier = Modifier.clip(CircleShape).clickable { onAdd(text); text = "" }.padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun GlassButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).background(Color(0x26FFFFFF))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = ripple(bounded = false, radius = 24.dp), onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}
