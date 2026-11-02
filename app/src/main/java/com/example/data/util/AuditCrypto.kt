package com.example.data.util

import java.security.MessageDigest

object AuditCrypto {
    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun generateEventHash(
        previousHash: String,
        timestamp: String,
        userId: String,
        module: String,
        actionType: String,
        entityId: String,
        description: String
    ): String {
        val payload = "$previousHash|$timestamp|$userId|$module|$actionType|$entityId|$description"
        return sha256(payload)
    }

    const val GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000"
}
