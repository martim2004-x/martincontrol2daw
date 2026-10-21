package com.louaraya.macrame.util

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Hash de contraseñas con sal aleatoria (SHA-256) para no guardar texto plano en SQLite.
 * En producción esto se delega al backend (bcrypt/Argon2); aquí es suficiente para el MVP académico.
 */
object PasswordHasher {
    fun hash(password: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return "${salt.toHex()}:${digest(salt, password).toHex()}"
    }

    fun verify(password: String, stored: String): Boolean {
        val parts = stored.split(":")
        if (parts.size != 2) return false
        val salt = parts[0].hexToBytes() ?: return false
        val expected = parts[1].hexToBytes() ?: return false
        return MessageDigest.isEqual(expected, digest(salt, password))
    }

    private fun digest(salt: ByteArray, password: String): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        return md.digest(password.toByteArray(Charsets.UTF_8))
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray? {
        if (length % 2 != 0) return null
        return try {
            ByteArray(length / 2) { i -> substring(i * 2, i * 2 + 2).toInt(16).toByte() }
        } catch (e: NumberFormatException) {
            null
        }
    }
}
