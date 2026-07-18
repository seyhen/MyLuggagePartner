# MyLuggagePartner

App Android de listes de valise intelligentes — Kotlin + Jetpack Compose + Material 3.
Squelette navigable généré à partir des design specs (M3 Expressive + photo plein cadre,
teinte terracotta/sable). Prêt pour itération dans Claude Code.

## Ouvrir le projet

1. Android Studio (Ladybug ou +), **Open** → sélectionner ce dossier.
2. Laisser Gradle synchroniser (télécharge le wrapper 8.9, AGP 8.5.2, Kotlin 2.0.20).
3. Run sur un émulateur/appareil **API 26+**.

> Le wrapper JAR (`gradle/wrapper/gradle-wrapper.jar`) n'est pas inclus. Android Studio
> le régénère à l'ouverture, ou : `gradle wrapper` si tu as Gradle en local.

## État actuel (ce premier jet)

- ✅ 4 écrans navigables : Accueil, Création (3 étapes), Liste, Paramètres
- ✅ Design system complet : palette clair/sombre, formes, typo (voir note polices)
- ✅ Interactions : cocher, quantités ±, swipe-to-delete + undo, ajout, renommer/dupliquer/supprimer
- ✅ Thème clair/sombre/auto, limite 2 valises gratuites + déblocage premium
- ✅ Catégories qui se compactent quand tout est coché, tri des cochés en bas
- ⬜ **Persistance** : tout est en mémoire (`AppViewModel`). À brancher sur Room/DataStore.
- ⬜ **Vraies photos** : gradients simulés par type. Voir `TripType.gradient()`.
- ⬜ **DateRangePicker** Material : emplacement marqué dans `CreateScreen` (étape 1).
- ⬜ **Polices** : voir ci-dessous.

## Polices (Bricolage Grotesque + Figtree)

Le design demande **Bricolage Grotesque** (display) et **Figtree** (body). Pour éviter
d'embarquer des binaires, `Type.kt` référence pour l'instant les familles système.

Pour installer les vraies :
1. Télécharger les `.ttf` sur Google Fonts.
2. Les poser dans `app/src/main/res/font/` (noms snake_case).
3. Dans `Type.kt`, remplacer `DisplayFamily` / `BodyFamily` par des
   `FontFamily(Font(R.font.bricolage_grotesque_bold, FontWeight.Bold), …)`.

Alternative : le module `androidx.compose.ui:ui-text-google-fonts` pour du téléchargement
à la volée.

## Architecture

```
model/Models.kt        Entités (Trip, PackItem, Category, TripType) + generateItems()
AppViewModel.kt        État applicatif en mémoire + toutes les mutations
MainActivity.kt        Navigation (sealed Screen), dialogs, snackbar
ui/theme/              Color.kt (palette), Theme.kt (AppColors + CompositionLocal), Type.kt
ui/Components.kt       Composants réutilisables : checkbox, switch, segmented, stepper, blob
ui/screens/            HomeScreen, CreateScreen, ListScreen, SettingsScreen, Shared
```

Le design va au-delà du `ColorScheme` M3 standard, donc les couleurs passent par un
`AppColors` custom exposé via `LocalAppColors` — accès partout via `AppTheme.colors`.

## Pistes suivantes (idées pour Claude Code)

- Room : entités `TripEntity` / `ItemEntity`, DAO, `Flow` → remplacer le state en mémoire.
- Google Play Billing pour le premium 3,99 € one-time.
- Coil + images de destination (URL ou upload utilisateur), fallback sur les gradients.
- Predictive back + shared element transition carte → en-tête liste.
- Export PDF / partage texte réels.
- Localisation : extraire les strings FR dans `strings.xml`.
