package com.myluggagepartner.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myluggagepartner.app.model.Category
import com.myluggagepartner.app.model.PackItem
import com.myluggagepartner.app.model.Trip
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
    data object ResetChecks : Dialog
    data class DeleteTemplate(val id: Long) : Dialog
}

/* Savers : l'état de navigation doit survivre à la rotation / mort du process. */
private val ScreenSaver = listSaver<Screen, Any>(
    save = { screen ->
        when (screen) {
            Screen.Home -> listOf("home")
            Screen.Create -> listOf("create")
            Screen.Settings -> listOf("settings")
            is Screen.ListView -> listOf("list", screen.tripId)
        }
    },
    restore = { list ->
        when (list.firstOrNull()) {
            "create" -> Screen.Create
            "settings" -> Screen.Settings
            "list" -> (list.getOrNull(1) as? Long)?.let { Screen.ListView(it) } ?: Screen.Home
            else -> Screen.Home
        }
    },
)

// Une liste vide n'est pas sauvegardée par listSaver : on retombe donc sur null (aucun dialog).
private val DialogSaver = listSaver<Dialog?, Any>(
    save = { dialog ->
        when (dialog) {
            null -> emptyList()
            Dialog.Rename -> listOf("rename")
            Dialog.Delete -> listOf("delete")
            Dialog.Premium -> listOf("premium")
            Dialog.ResetChecks -> listOf("reset")
            is Dialog.DeleteTemplate -> listOf("deleteTemplate", dialog.id)
        }
    },
    restore = { list ->
        when (list.firstOrNull()) {
            "rename" -> Dialog.Rename
            "delete" -> Dialog.Delete
            "premium" -> Dialog.Premium
            "reset" -> Dialog.ResetChecks
            "deleteTemplate" -> (list.getOrNull(1) as? Long)?.let { Dialog.DeleteTemplate(it) }
            else -> null
        }
    },
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        setContent { AppRoot() }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }
}

/** Bordereau de contenu — même langage que l'app : capitales, cases, quantités alignées. */
private fun formatTripText(trip: Trip): String = buildString {
    appendLine("${trip.name.uppercase()}  ·  ${trip.type.code}")
    if (trip.dates.isNotBlank()) appendLine(trip.dates)
    appendLine("${trip.done}/${trip.total} vérifiés")
    appendLine("—".repeat(28))
    appendLine()
    Category.entries.forEach { cat ->
        val items = trip.items.filter { it.category == cat }
        if (items.isNotEmpty()) {
            appendLine(cat.label.uppercase())
            items.forEach { item ->
                val check = if (item.checked) "[x]" else "[ ]"
                val qty = if (item.qty > 1) "  ×${item.qty}" else ""
                appendLine("  $check ${item.name}$qty")
            }
            appendLine()
        }
    }
    append("MyLuggagePartner")
}

@Composable
private fun AppRoot(vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    MyLuggageTheme(themeMode = state.themeMode) {
        if (!state.loaded) {
            Box(Modifier.fillMaxSize().background(AppTheme.colors.surface))
            return@MyLuggageTheme
        }
        if (!state.onboarded) {
            OnboardingScreen(onDone = { vm.completeOnboarding() })
            return@MyLuggageTheme
        }
        var screen by rememberSaveable(stateSaver = ScreenSaver) { mutableStateOf<Screen>(Screen.Home) }
        var dialog by rememberSaveable(stateSaver = DialogSaver) { mutableStateOf<Dialog?>(null) }
        // Le snackbar porte un lambda "annuler" non sérialisable : volontairement éphémère.
        var snack by remember { mutableStateOf<Pair<String, (() -> Unit)?>?>(null) }
        var renameText by rememberSaveable { mutableStateOf("") }

        // Auto-dismiss du snackbar
        LaunchedEffect(snack) {
            if (snack != null) { delay(4000); snack = null }
        }

        fun flash(msg: String, undo: (() -> Unit)? = null) { snack = msg to undo }

        BackHandler(enabled = screen != Screen.Home) {
            when {
                screen == Screen.Create && state.step > 1 -> vm.setStep(state.step - 1)
                else -> screen = Screen.Home
            }
        }

        Surface(color = AppTheme.colors.surface, modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) {

                AnimatedContent(
                    targetState = screen,
                    transitionSpec = {
                        val targetOrder = when (targetState) {
                            Screen.Home -> 0; Screen.Create -> 1; is Screen.ListView -> 2; Screen.Settings -> 3
                        }
                        val initialOrder = when (initialState) {
                            Screen.Home -> 0; Screen.Create -> 1; is Screen.ListView -> 2; Screen.Settings -> 3
                        }
                        if (targetOrder > initialOrder) {
                            (slideInHorizontally { it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut())
                        } else {
                            (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { it / 3 } + fadeOut())
                        }
                    },
                    label = "screen",
                ) { currentScreen ->

                when (val s = currentScreen) {
                    Screen.Home -> HomeScreen(
                        trips = state.trips,
                        templates = state.templates,
                        canCreate = vm.canCreate(),
                        onOpenTrip = { screen = Screen.ListView(it) },
                        onCreate = { vm.resetDraft(); screen = Screen.Create },
                        onUseTemplate = { tplId ->
                            val id = vm.createFromTemplate(tplId)
                            if (id >= 0) { screen = Screen.ListView(id); flash("Créée depuis le modèle") }
                        },
                        onDeleteTemplate = { dialog = Dialog.DeleteTemplate(it) },
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
                            val hasDates = state.draft.from != null
                            val id = vm.finishCreate()
                            screen = Screen.ListView(id)
                            flash(if (hasDates) "Liste générée 🔔 Rappel prévu la veille du départ" else "Liste générée — tout est modifiable ✨")
                        },
                    )

                    is Screen.ListView -> {
                        val trip = vm.tripById(s.tripId)
                        if (trip == null) {
                            LaunchedEffect(Unit) { screen = Screen.Home }
                        } else ListScreen(
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
                            onShare = {
                                val text = formatTripText(trip)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Valise : ${trip.name}")
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "Partager la liste"))
                            },
                            onDelete = { dialog = Dialog.Delete },
                            onReset = { dialog = Dialog.ResetChecks },
                            onSaveTemplate = {
                                val name = vm.saveAsTemplate(trip.id)
                                flash("Modèle « $name » sauvegardé")
                            },
                        )
                    }

                    Screen.Settings -> SettingsScreen(
                        themeMode = state.themeMode,
                        premium = state.premium,
                        onTheme = vm::setTheme,
                        onUnlock = { vm.unlockPremium(); flash("Merci ! Valises illimitées débloquées ✨") },
                        onShare = {
                            if (state.trips.isEmpty()) { flash("Aucune valise à partager") }
                            else {
                                val text = state.trips.joinToString("\n\n${"—".repeat(30)}\n\n") { formatTripText(it) }
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Mes valises — MyLuggagePartner")
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "Partager"))
                            }
                        },
                        onBack = { screen = Screen.Home },
                    )
                }
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
                        DialogTitle("Cinq valises gratuites")
                        DialogBody("Vous avez atteint la limite gratuite. Passez en illimité pour 3,99 € — un seul paiement, à vie.")
                        DialogActions(
                            confirm = "Débloquer", cancel = "Plus tard",
                            onCancel = { dialog = null },
                            onConfirm = { vm.unlockPremium(); dialog = null; vm.resetDraft(); screen = Screen.Create; flash("Valises illimitées débloquées ✨") },
                        )
                    }
                    Dialog.ResetChecks -> AppDialog(onDismiss = { dialog = null }) {
                        DialogTitle("Tout décocher ?")
                        DialogBody("La progression de cette valise sera réinitialisée.")
                        DialogActions(
                            confirm = "Décocher", destructive = true,
                            onCancel = { dialog = null },
                            onConfirm = { activeTripId?.let { vm.resetChecks(it) }; dialog = null; flash("Tout décoché") },
                        )
                    }
                    is Dialog.DeleteTemplate -> AppDialog(onDismiss = { dialog = null }) {
                        DialogTitle("Supprimer ce modèle ?")
                        DialogBody("Ce modèle sera supprimé définitivement.")
                        DialogActions(
                            confirm = "Supprimer", destructive = true,
                            onCancel = { dialog = null },
                            onConfirm = { vm.deleteTemplate((dialog as Dialog.DeleteTemplate).id); dialog = null; flash("Modèle supprimé") },
                        )
                    }
                    null -> {}
                }
            }
        }
    }
}

/* ————— Snackbar : bandeau d'accusé de réception ————— */
@Composable
private fun SnackBar(msg: String, undo: (() -> Unit)?, onUndo: () -> Unit, modifier: Modifier) {
    val c = AppTheme.colors
    Row(
        modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(Radius.sm))
            .background(if (c.isDark) Color(0xFFEDEDE8) else Color(0xFF14213D))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            msg,
            color = if (c.isDark) Color(0xFF14213D) else Color(0xFFFBFAF7),
            fontSize = 14.sp, modifier = Modifier.weight(1f),
        )
        if (undo != null) {
            Box(
                Modifier
                    .defaultMinSize(minHeight = 44.dp)
                    .clip(RoundedCornerShape(Radius.xs))
                    .clickable { onUndo() }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "ANNULER", style = LabelStamp,
                    color = if (c.isDark) Color(0xFFD8232A) else Color(0xFFF0796E),
                )
            }
        }
    }
}

/* ————— Dialog primitives : cases de formulaire ————— */
@Composable
private fun AppDialog(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val c = AppTheme.colors
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .width(330.dp)
                .clip(RoundedCornerShape(Radius.md))
                .background(c.surfaceContainer)
                .border(1.dp, c.outline, RoundedCornerShape(Radius.md)),
        ) {
            AirmailBand(height = 7.dp)
            Column(Modifier.padding(22.dp), content = content)
        }
    }
}

@Composable private fun DialogTitle(t: String) =
    Text(t.uppercase(), style = TitleCard, color = AppTheme.colors.onSurface, fontSize = 19.sp)

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
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.xs)).background(c.surface)
            .border(1.5.dp, c.outline, RoundedCornerShape(Radius.xs)).padding(14.dp),
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
    Spacer(Modifier.height(20.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.defaultMinSize(minHeight = 48.dp).clip(RoundedCornerShape(Radius.xs))
                .clickable { onCancel() }.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(cancel.uppercase(), style = LabelStamp, color = c.onSurfaceVariant)
        }
        Spacer(Modifier.width(6.dp))
        Box(
            Modifier.defaultMinSize(minHeight = 48.dp).clip(RoundedCornerShape(Radius.xs))
                .background(if (destructive) c.errorText else c.primary)
                .clickable { onConfirm() }.padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(confirm.uppercase(), style = LabelStamp, color = c.onPrimary)
        }
    }
}
