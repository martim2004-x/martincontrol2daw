package com.louaraya.macrame.domain

import com.louaraya.macrame.util.Dates

/**
 * Reglas de validación centralizadas
 * cada funcion devuelve el mensaje de error o null si el valor es valido.
 * Los ViewModels las usan y la UI solo muestra el resultado.
 */
object Validators {
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val aliasRegex = Regex("^[A-Za-z0-9_.]+$")

    fun required(value: String, message: String): String? =
        if (value.isBlank()) message else null

    fun fullName(value: String): String? {
        val t = value.trim()
        return when {
            t.isEmpty() -> "Ingresa tu nombre completo"
            t.length < 3 -> "Minimo 3 caracteres"
            t.length > 60 -> "Máximo 60 caracteres"
            !t.all { it.isLetter() || it == ' ' || it == '\'' || it == '.' } -> "Usa solo letras y espacios"
            !t.contains(' ') -> "Incluye nombre y apellido"
            else -> null
        }
    }

    fun alias(value: String): String? {
        val t = value.trim()
        return when {
            t.isEmpty() -> "Elige un alias"
            t.length < 3 -> "Mínimo 3 caracteres"
            t.length > 20 -> "Máximo 20 caracteres"
            !aliasRegex.matches(t) -> "Solo letras, números, punto y guion bajo"
            else -> null
        }
    }

    fun email(value: String): String? {
        val t = value.trim()
        return when {
            t.isEmpty() -> "Ingresa tu correo"
            !emailRegex.matches(t) -> "Formato inválido (ej: nombre@correo.cl)"
            else -> null
        }
    }

    /** 9 dígitos que comienzan con 9 con o sin +56. */
    fun phone(value: String): String? {
        if (value.isBlank()) return "Ingresa tu teléfono"
        val digits = value.filter { it.isDigit() }
        val national = if (digits.startsWith("56") && digits.length == 11) digits.drop(2) else digits
        return if (national.length == 9 && national.startsWith("9")) null
        else "Celular chileno, ej: +56 9 1234 5678"
    }

    fun password(value: String): String? = when {
        value.isEmpty() -> "Ingresa una contraseña"
        value.length < 8 -> "Mínimo 8 caracteres"
        value.none { it.isUpperCase() } -> "Incluye una mayúscula"
        value.none { it.isLowerCase() } -> "Incluye una minúscula"
        value.none { it.isDigit() } -> "Incluye un número"
        else -> null
    }

    fun confirmPassword(password: String, confirm: String): String? = when {
        confirm.isEmpty() -> "Repite la contraseña"
        confirm != password -> "Las contraseñas no coinciden"
        else -> null
    }

    /** Direccion opcional */
    fun optionalAddress(value: String): String? {
        val t = value.trim()
        return when {
            t.isEmpty() -> null
            t.length < 5 -> "Escribe una dirección más completa"
            t.length > 100 -> "Máximo 100 caracteres"
            else -> null
        }
    }

    fun choice(value: String, what: String): String? =
        if (value.isBlank()) "Selecciona $what" else null

    fun dimension(value: String, what: String): String? {
        val t = value.trim()
        if (t.isEmpty()) return "Ingresa $what"
        val n = t.toIntOrNull() ?: return "Solo números enteros"
        return if (n in 10..600) null else "Debe estar entre 10 y 600 cm"
    }

    fun price(value: String): String? {
        val t = value.trim()
        if (t.isEmpty()) return "Ingresa el precio"
        val n = t.toIntOrNull() ?: return "Solo números enteros"
        return if (n in 1_000..5_000_000) null else "Entre \$1.000 y \$5.000.000"
    }

    fun stock(value: String): String? {
        val t = value.trim()
        if (t.isEmpty()) return "Ingresa el stock"
        val n = t.toIntOrNull() ?: return "Solo números enteros"
        return if (n in 0..999) null else "Entre 0 y 999 unidades"
    }

    fun productName(value: String): String? {
        val t = value.trim()
        return when {
            t.isEmpty() -> "Ingresa el nombre de la pieza"
            t.length < 3 -> "Mínimo 3 caracteres"
            t.length > 60 -> "Máximo 60 caracteres"
            else -> null
        }
    }

    fun description(value: String): String? {
        val t = value.trim()
        return when {
            t.isEmpty() -> "Describe la pieza"
            t.length < 10 -> "Mínimo 10 caracteres"
            t.length > 400 -> "Máximo 400 caracteres"
            else -> null
        }
    }

    fun notes(value: String): String? =
        if (value.length > 300) "Máximo 300 caracteres" else null

    fun colors(selected: List<String>): String? =
        if (selected.isEmpty()) "Elige al menos un color" else null

    /** Fecha requerida obligatoria y no puede ser anterior al dia actuakl */
    fun requiredDate(millis: Long?): String? = when {
        millis == null -> "Selecciona una fecha"
        millis < Dates.todayUtcMillis() -> "La fecha no puede ser anterior a hoy"
        else -> null
    }

    fun campaignName(value: String): String? {
        val t = value.trim()
        return when {
            t.isEmpty() -> "Ingresa el nombre de la campaña"
            t.length < 3 -> "Mínimo 3 caracteres"
            t.length > 50 -> "Máximo 50 caracteres"
            else -> null
        }
    }

    fun campaignDescription(value: String): String? {
        val t = value.trim()
        return when {
            t.isEmpty() -> "Describe la campaña"
            t.length < 10 -> "Mínimo 10 caracteres"
            t.length > 200 -> "Máximo 200 caracteres"
            else -> null
        }
    }

    fun startDate(millis: Long?): String? = if (millis == null) "Selecciona la fecha de inicio" else null

    fun endDate(start: Long?, end: Long?): String? = when {
        end == null -> "Selecciona la fecha de término"
        start != null && end < start -> "Debe ser posterior al inicio"
        else -> null
    }

    fun campaignProducts(selected: Set<Long>): String? =
        if (selected.isEmpty()) "Asocia al menos un diseño" else null
}
