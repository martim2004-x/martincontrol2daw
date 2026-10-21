package com.louaraya.macrame.domain

import com.louaraya.macrame.data.CampaignEntity
import java.time.LocalDate
import java.time.ZoneOffset

/** roles de la app. */
object Role {
    const val ADMIN = "ADMIN"
    const val CLIENT = "CLIENT"
}

/** cat de catalogo. */
object Category {
    const val MATRIMONIOS = "Matrimonios"
    const val FESTIVALES = "Festivales"
    const val INTERIORES = "Interiores"
    val all = listOf(MATRIMONIOS, FESTIVALES, INTERIORES)
}

/** estados posibles de una cotizacion. */
object RequestStatus {
    const val EXPLORACION = "Exploración"
    const val PENDIENTE = "Pendiente"
    const val CONFIRMADO = "Confirmado"
    const val ENTREGADO = "Entregado"
    const val RECHAZADA = "Rechazada"
    val all = listOf(EXPLORACION, PENDIENTE, CONFIRMADO, ENTREGADO, RECHAZADA)
}

object NotificationType {
    const val CAMPAIGN = "CAMPAIGN"
    const val REQUEST = "REQUEST"
    const val FAIR = "FAIR"
    const val CATALOG = "CATALOG"
}

object FairType {
    const val FERIA = "Feria"
    const val SHOWROOM = "Showroom"
    const val EVENTO = "Evento"
    val all = listOf(FERIA, SHOWROOM, EVENTO)
}

object FormOptions {
    val eventTypes = listOf("Matrimonio", "Festival", "Evento corporativo", "Encuentro cultural", "Hogar / Decoración")
    val campaignThemes = listOf("Temporada primavera", "Bodas y matrimonios", "Festivales de verano", "Hogar y decoración", "Edición limitada")
    val materials = listOf("Algodón natural", "Algodón reciclado", "Algodón y lino", "Lino", "Yute", "Cuerda teñida")
}

/** paleta de colores disponibles cuando solicitas un producto. */
data class YarnColor(val name: String, val argb: Long)

val YarnColors = listOf(
    YarnColor("Crudo", 0xFFF1EBDD),
    YarnColor("Arena", 0xFFD9C9A8),
    YarnColor("Salvia", 0xFF8FA595),
    YarnColor("Bosque", 0xFF2D4030),
    YarnColor("Terracota", 0xFFB5755A),
    YarnColor("Grafito", 0xFF3A3F3C)
)

enum class CampaignPhase(val label: String) {
    BORRADOR("Borrador"),
    PROGRAMADA("Programada"),
    ACTIVA("Activa"),
    FINALIZADA("Finalizada")
}

/** Calcula la fase de una campaña según su publicación y sus fechas (guardadas como medianoche UTC). */
fun CampaignEntity.phase(today: LocalDate = LocalDate.now()): CampaignPhase {
    if (!published) return CampaignPhase.BORRADOR
    val start = java.time.Instant.ofEpochMilli(startDate).atZone(ZoneOffset.UTC).toLocalDate()
    val end = java.time.Instant.ofEpochMilli(endDate).atZone(ZoneOffset.UTC).toLocalDate()
    return when {
        today.isBefore(start) -> CampaignPhase.PROGRAMADA
        today.isAfter(end) -> CampaignPhase.FINALIZADA
        else -> CampaignPhase.ACTIVA
    }
}
