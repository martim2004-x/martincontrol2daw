package com.louaraya.macrame.util

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatters {
    private val localeCl = Locale("es", "CL")
    private val numberFormat: NumberFormat = NumberFormat.getInstance(localeCl)
    private val dateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", localeCl)
    private val dateTimeFormat = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", localeCl)

    fun price(clp: Int): String = "\$" + numberFormat.format(clp)

    /** Fechas del DatePicker vienen en medianoche UTC. */
    fun date(utcMillis: Long): String =
        Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate().format(dateFormat)

    fun dateTime(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(dateTimeFormat)

    fun requestCode(id: Long): String = "SOL-%04d".format(id)

    fun productCode(n: Long): String = "LAM-%04d".format(n)

    fun relative(millis: Long, now: Long = System.currentTimeMillis()): String {
        val diff = (now - millis).coerceAtLeast(0)
        val minutes = diff / 60_000
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes < 1 -> "Ahora"
            minutes < 60 -> "Hace $minutes min"
            hours < 24 -> if (hours == 1L) "Hace 1 hora" else "Hace $hours horas"
            days < 7 -> if (days == 1L) "Hace 1 día" else "Hace $days días"
            else -> dateTime(millis).substringBefore(",")
        }
    }

    fun dimensions(widthCm: Int, heightCm: Int): String = "$widthCm × $heightCm cm"
}

object Dates {
    /** Hoy a medianoche UTC; sirve como fecha mínima del DatePicker. */
    fun todayUtcMillis(): Long =
        LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    fun utcMillisFromNow(plusDays: Long): Long =
        LocalDate.now().plusDays(plusDays).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}
