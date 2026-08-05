package com.myluggagepartner.app.model

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/* ————— Catégories ————— */
enum class Category(val label: String, val emoji: String) {
    VET("Vêtements", "👕"),
    TOI("Toilette", "🧴"),
    TECH("Tech", "🔌"),
    DOC("Documents", "📄"),
    SANTE("Santé", "💊"),
    DIV("Divers", "🎒"),
}

/* ————— Types de voyage ————— */
enum class TripType(val label: String, val emoji: String) {
    PLAGE("Plage", "🏖️"),
    VILLE("Ville", "🏙️"),
    RANDO("Randonnée", "🥾"),
    SKI("Ski", "⛷️"),
    AFFAIRES("Affaires", "💼"),
    CAMPING("Camping", "⛺");

    /** Gradient "photo" simulé (remplaçable par une vraie image plus tard). */
    fun gradient(): List<Color> = when (this) {
        VILLE -> listOf(0xFFC8956A, 0xFFA0563B, 0xFF6B3422, 0xFF3A1C10)
        PLAGE -> listOf(0xFF5BA4C9, 0xFF2E8EB5, 0xFFD4A96A, 0xFFE8C49A)
        RANDO -> listOf(0xFF6B8F5E, 0xFF4A7A3C, 0xFF2C5A22, 0xFF1A3A12)
        SKI -> listOf(0xFFB8CFDF, 0xFF8AADCC, 0xFF5A7FA0, 0xFF3A5570)
        AFFAIRES -> listOf(0xFF7A8A9A, 0xFF4A5A6A, 0xFF2A3A4A, 0xFF1A2A3A)
        CAMPING -> listOf(0xFF8FAA6A, 0xFF5A7A3A, 0xFF3A5A2A, 0xFF2A3A1A)
    }.map { Color(it) }
}

enum class Intensity(val label: String) { LEGER("Léger"), NORMAL("Normal"), COMPLET("Complet") }

/* ————— Entités ————— */
data class PackItem(
    val id: Long,
    val category: Category,
    val name: String,
    val qty: Int = 1,
    val checked: Boolean = false,
)

data class Trip(
    val id: Long,
    val name: String,
    val destination: String,
    val dates: String,
    val type: TripType,
    val hasPhoto: Boolean,
    val items: List<PackItem>,
    val departureDateEpoch: Long? = null,
) {
    val total: Int get() = items.size
    val done: Int get() = items.count { it.checked }
    val progress: Float get() = if (total == 0) 0f else done.toFloat() / total
}

/* ————— Génération de liste (quantités réalistes) ————— */
private val idBase = System.currentTimeMillis() * 1000
private var idCounter = 0L
private fun nextId(): Long = idBase + ++idCounter
private fun clampI(v: Double, a: Int, b: Int): Int = max(a, min(b, v.roundToInt()))

data class GenParams(
    val type: TripType,
    val intensity: Intensity = Intensity.NORMAL,
    val days: Int = 5,
    val laundry: Boolean = false,
    val kids: Boolean = false,
    val travelers: Int = 1,
)

fun generateItems(p: GenParams): List<PackItem> {
    val f = when (p.intensity) { Intensity.LEGER -> 0.75; Intensity.COMPLET -> 1.25; else -> 1.0 }
    val w = if (p.laundry) 0.55 else 1.0
    val d = p.days.coerceAtLeast(1)
    val out = mutableListOf<PackItem>()
    fun it(c: Category, n: String, q: Int = 1) = out.add(PackItem(nextId(), c, n, q))

    it(Category.VET, "T-shirts", clampI(d * 0.8 * f * w, 2, 7))
    it(Category.VET, "Sous-vêtements", clampI((d + 1) * f * w, 3, 8))
    it(Category.VET, "Paires de chaussettes", clampI(d * 0.9 * f * w, 2, 7))
    it(Category.VET, "Pantalons / bas", clampI(d / 3.0 * f, 1, 4))
    if (p.intensity != Intensity.LEGER) it(Category.VET, "Pull ou sweat")
    it(Category.VET, "Veste légère")
    it(Category.VET, "Tenue pour dormir")

    it(Category.TOI, "Brosse à dents + dentifrice")
    it(Category.TOI, "Déodorant")
    it(Category.TOI, "Shampoing solide")
    if (p.intensity == Intensity.COMPLET) it(Category.TOI, "Rasoir")

    it(Category.TECH, "Chargeur de téléphone")
    it(Category.TECH, "Batterie externe")
    it(Category.TECH, "Écouteurs")

    it(Category.DOC, "Carte d'identité / passeport")
    it(Category.DOC, "Carte bancaire")
    it(Category.DOC, "Billets (avion / train)")
    it(Category.DOC, "Carte européenne d'assurance maladie")

    it(Category.SANTE, "Doliprane")
    it(Category.SANTE, "Pansements")
    it(Category.SANTE, "Médicaments personnels")

    it(Category.DIV, "Lunettes de soleil")
    it(Category.DIV, "Gourde")
    it(Category.DIV, "Tote bag")

    when (p.type) {
        TripType.PLAGE -> {
            it(Category.VET, "Maillots de bain", clampI(2 * f, 1, 3))
            it(Category.DIV, "Serviette de plage")
            it(Category.VET, "Tongs")
            it(Category.TOI, "Crème solaire")
            it(Category.DIV, "Chapeau ou casquette")
        }
        TripType.VILLE -> {
            it(Category.VET, "Chaussures confortables")
            it(Category.TOI, "Crème solaire")
            if (p.intensity != Intensity.LEGER) it(Category.VET, "Tenue plus habillée")
        }
        TripType.RANDO -> {
            it(Category.VET, "Chaussures de randonnée")
            it(Category.VET, "Veste imperméable")
            it(Category.SANTE, "Trousse premiers secours")
            it(Category.DIV, "Sac à dos de journée")
            it(Category.TOI, "Crème solaire")
        }
        TripType.SKI -> {
            it(Category.VET, "Combinaison / pantalon de ski")
            it(Category.VET, "Sous-couches thermiques", clampI(2 * f, 1, 3))
            it(Category.VET, "Gants de ski")
            it(Category.VET, "Bonnet")
            it(Category.DIV, "Masque de ski")
            it(Category.TOI, "Crème solaire + stick à lèvres")
        }
        TripType.AFFAIRES -> {
            it(Category.VET, "Chemises", clampI(d * 0.8 * f, 2, 5))
            it(Category.VET, "Veste de costume")
            it(Category.TECH, "Ordinateur + chargeur")
            it(Category.DOC, "Cartes de visite")
        }
        TripType.CAMPING -> {
            it(Category.DIV, "Sac de couchage")
            it(Category.DIV, "Lampe frontale")
            it(Category.DIV, "Couteau multifonction")
            it(Category.VET, "Veste imperméable")
            it(Category.SANTE, "Anti-moustiques")
        }
    }

    if (p.kids) {
        it(Category.DIV, "Jeux / livres pour les enfants")
        it(Category.DIV, "Goûters")
        it(Category.TOI, "Lingettes")
    }
    if (p.travelers > 1) it(Category.TECH, "Multiprise de voyage")

    return out
}

fun newId(): Long = nextId()

data class TripTemplate(
    val id: Long,
    val name: String,
    val type: TripType,
    val items: List<Pair<Category, String>>,
)

/* ————— Données de démo ————— */
fun demoTrips(): List<Trip> {
    val lisbonne = generateItems(GenParams(TripType.VILLE, Intensity.LEGER, 5))
        .mapIndexed { i, item -> if (i < 8) item.copy(checked = true) else item }
    val alpes = generateItems(GenParams(TripType.SKI, Intensity.NORMAL, 7, travelers = 2))
    return listOf(
        Trip(1, "Lisbonne", "Lisbonne", "12 – 17 mai", TripType.VILLE, hasPhoto = true, items = lisbonne),
        Trip(2, "Les Alpes", "Les Alpes", "2 – 9 fév", TripType.SKI, hasPhoto = false, items = alpes),
    )
}
