package com.myluggagepartner.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myluggagepartner.app.model.PackItem
import com.myluggagepartner.app.ui.screens.*
import com.myluggagepartner.app.ui.theme.*
import kotlinx.coroutines.delay

/** Écrans de navigation (nav simple sans lib externe, suffisant pour ce prototype). */
sealed interface Screen {
    data object Home : Screen
    data object Create : Screen
    data class ListView(val tripId: Long) : Screen
    data object Settings : Screen
}

/** Dialogs modaux. */
private sealed interface Dialog {
    data object Rename : Dialog
    data object Delete : Dialog
    data object Premium : Dialog
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppRoot() }
    }
}

@Composable
private fun AppRoot(vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsState()

    MyLuggageTheme(themeMode = state.themeMode) {
        var screen by remember { mutableStateOf<Screen>(Screen.Home) }
        var dialog by remember { mutableStateOf<Dialog?>(null) }
        var snack by remember { mutableStateOf<Pair<String, (() -> Unit)?>?>(null) }
        var renameText by remember { mutableStateOf("") }

        // Auto-dismiss du snackbar
        LaunchedEffect(snack) {
            if (snack != null) { delay(4000); snack = null }
        }

        fun flash(msg: String, undo: (() -> Unit)? = null) { snack = msg to undo }

        Surface(color = AppTheme.colors.surface, modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) {

                when (val s = screen) {
                    Screen.Home -> HomeScreen(
                        trips = state.trips,
                        canCreate = vm.canCreate(),
                        onOpenTrip = { screen = Screen.ListView(it) },
                        onCreate = { vm.resetDraft(); screen = Screen.Create },
                        onLimitReached = { dialog = Dialog.Premium },
                        onSettings = { screen = Screen.Settings },
                    )

                    Screen.Create -> CreateScreen(
                        step = state.step,
                        draft = state.draft,
                        onStep = vm::setStep,
                        onDraft = vm::updateDraft,
                        onCancel = { screen = Screen.Home },
                        onFinish = {
                            val id = vm.finishCreate()
                            screen = Screen.ListView(id)
                            flash("Liste générée — tout est modifiable ✨")
                        },
                    )

                    is Screen.ListView -> {
                        val trip = vm.tripById(s.tripId)
                        if (trip == null) { screen = Screen.Home }
                        else ListScreen(
                            trip = trip,
                            onBack = { screen = Screen.Home },
                            onToggle = { vm.toggleItem(trip.id, it) },
                            onQty = { itemId, d -> vm.changeQty(trip.id, itemId, d) },
                            onAdd = { cat, name -> vm.addItem(trip.id, cat, name) },
                            onRemove = { item: PackItem ->
                                vm.removeItem(trip.id, item.id)
                                flash("« ${item.name} » retiré") { vm.restoreItem(trip.id, item) }
                            },
                            onRename = { renameText = trip.name; dialog = Dialog.Rename },
                            onDuplicate = { vm.duplicateTrip(trip.id); flash("Valise dupliquée") },
                            onShare = { flash("Liste copiée 📋") },
                            onDelete = { dialog = Dialog.Delete },
                        )
                    }

                    Screen.Settings -> SettingsScreen(
                        themeMode = state.themeMode,
                        premium = state.premium,
                        onTheme = vm::setTheme,
                        onUnlock = { vm.unlockPremium(); flash("Merci ! Valises illimitées débloquées ✨") },
                        onExport = { flash("Export PDF lancé 📄") },
                        onShare = { flash("Liste copiée 📤") },
                        onBack = { screen = Screen.Home },
                    )
                }

                // ——— Snackbar ———
                snack?.let { (msg, undo) ->
                    SnackBar(msg, undo, onUndo = { undo?.invoke(); snack = null }, Modifier.align(Alignment.BottomCenter))
                }

                // ——— Dialogs ———
                val activeTripId = (screen as? Screen.ListView)?.tripId
                when (dialog) {
                    Dialog.Rename -> AppDialog(onDismiss = { dialog = null }) {
                        DialogTitle("Renommer la valise")
                        Spacer(Modifier.height(10.dp))
                        DialogTextField(renameText) { renameText = it }
                        DialogActions(
                            confirm = "Renommer",
                            onCancel = { dialog = null },
                            onConfirm = { activeTripId?.let { vm.renameTrip(it, renameText) }; dialog = null },
                        )
                    }
                    Dialog.Delete -> AppDialog(onDismiss = { dialog = null }) {
                        DialogTitle("Supprimer cette valise ?")
                        DialogBody("La liste et sa progression seront supprimées.")
                        DialogActions(
                            confirm = "Supprimer", destructive = true,
                            onCancel = { dialog = null },
                            onConfirm = { activeTripId?.let { vm.deleteTrip(it) }; dialog = null; screen = Screen.Home; flash("Valise supprimée") },
                        )
                    }
                    Dialog.Premium -> AppDialog(onDismiss = { dialog = null }) {
                        DialogTitle("Deux valises gratuites")
                        DialogBody("Vous avez atteint la limite. Passez en illimité pour 3,99 € — une seule fois.")
                        DialogActions(
                            confirm = "Débloquer", cancel = "Plus tard",
                            onCancel = { dialog = null },
                            onConfirm = { vm.unlockPremium(); dialog = null; vm.resetDraft(); screen = Screen.Create; flash("Valises illimitées débloquées ✨") },
                        )
                    }
                    null -> {}
                }
            }
        }
    }
}

/* ————— Snackbar ————— */
@Composable
private fun SnackBar(msg: String, undo: (() -> Unit)?, onUndo: () -> Unit, modifier: Modifier) {
    val c = AppTheme.colors
    Row(
        modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(16.dp))
            .background(if (c.isDark) Color(0xFFF0EBE5) else Color(0xFF352F2C))
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(msg, color = if (c.isDark) Color(0xFF241914) else Color(0xFFF3EFEC), fontSize = 14.sp, modifier = Modifier.weight(1f))
        if (undo != null) {
            Text("Annuler", color = if (c.isDark) Color(0xFF9E4522) else Color(0xFFFFB59B), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp,
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { onUndo() }.padding(horizontal = 8.dp, vertical = 6.dp))
        }
    }
}

/* ————— Dialog primitives ————— */
@Composable
private fun AppDialog(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val c = AppTheme.colors
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.width(324.dp).clip(RoundedCornerShape(30.dp)).background(c.surfaceContainer).padding(26.dp),
            content = content,
        )
    }
}

@Composable private fun DialogTitle(t: String) = Text(t, style = TitleCard, color = AppTheme.colors.onSurface, fontSize = 21.sp)
@Composable private fun DialogBody(t: String) {
    Spacer(Modifier.height(12.dp))
    Text(t, color = AppTheme.colors.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp)
}

@Composable
private fun DialogTextField(value: String, onChange: (String) -> Unit) {
    val c = AppTheme.colors
    androidx.compose.foundation.text.BasicTextField(
        value = value, onValueChange = onChange,
        textStyle = LocalTextStyle.current.copy(color = c.onSurface, fontSize = 16.sp),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(c.primary), singleLine = true,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(c.surface).padding(14.dp),
    )
}

@Composable
private fun DialogActions(
    confirm: String,
    cancel: String = "Annuler",
    destructive: Boolean = false,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    val c = AppTheme.colors
    Spacer(Modifier.height(16.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
        Text(cancel, color = c.primary, fontWeight = FontWeight.Bold, fontSize = 15.sp,
            modifier = Modifier.clip(RoundedCornerShape(999.dp)).clickable { onCancel() }.padding(horizontal = 16.dp, vertical = 10.dp))
        Spacer(Modifier.width(4.dp))
        Text(confirm, color = if (destructive) c.errorText else c.primary, fontWeight = FontWeight.Bold, fontSize = 15.sp,
            modifier = Modifier.clip(RoundedCornerShape(999.dp)).clickable { onConfirm() }.padding(horizontal = 16.dp, vertical = 10.dp))
    }
}
