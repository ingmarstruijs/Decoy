package nl.decoy.core.retention

import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Durable Multi-day record (e.g. campsite stay pitch). Payload is opaque UTF-8 JSON/text.
 */
data class MultiDayRecord(
    val id: String,
    val kind: String,
    val createdAt: Instant,
    val expiresAt: Instant,
    val plaintextPayload: String,
    val pinned: Boolean = false,
) {
    fun isExpired(now: Instant): Boolean {
        if (pinned) return false
        return !now.isBefore(expiresAt)
    }
}

interface MultiDayVault {
    fun put(record: MultiDayRecord)
    fun get(id: String): MultiDayRecord?
    fun delete(id: String)
    fun wipeExpired(now: Instant): Int
    fun clearAll()
    fun size(): Int
}

/**
 * AES-GCM encrypted vault. Ciphertext stored in memory for Phase 2 unit/emulator tests;
 * Android Keystore-backed keys can replace [key] later.
 */
class AesGcmMultiDayVault(
    private val key: SecretKey,
    private val clock: Clock = Clock.systemUTC(),
) : MultiDayVault {
    private data class Sealed(val iv: ByteArray, val ciphertext: ByteArray, val meta: Meta)
    private data class Meta(
        val id: String,
        val kind: String,
        val createdAt: Instant,
        val expiresAt: Instant,
        val pinned: Boolean,
    )

    private val sealed = linkedMapOf<String, Sealed>()

    override fun put(record: MultiDayRecord) {
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(record.plaintextPayload.toByteArray(Charsets.UTF_8))
        sealed[record.id] = Sealed(
            iv = iv,
            ciphertext = ciphertext,
            meta = Meta(record.id, record.kind, record.createdAt, record.expiresAt, record.pinned),
        )
    }

    override fun get(id: String): MultiDayRecord? {
        val entry = sealed[id] ?: return null
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, entry.iv))
        val plain = String(cipher.doFinal(entry.ciphertext), Charsets.UTF_8)
        return MultiDayRecord(
            id = entry.meta.id,
            kind = entry.meta.kind,
            createdAt = entry.meta.createdAt,
            expiresAt = entry.meta.expiresAt,
            plaintextPayload = plain,
            pinned = entry.meta.pinned,
        )
    }

    override fun delete(id: String) {
        sealed.remove(id)
    }

    override fun wipeExpired(now: Instant): Int {
        val expired = sealed.filter { (_, v) ->
            !v.meta.pinned && !now.isBefore(v.meta.expiresAt)
        }.keys.toList()
        expired.forEach { sealed.remove(it) }
        return expired.size
    }

    override fun clearAll() {
        sealed.clear()
    }

    override fun size(): Int = sealed.size

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"

        fun generateKey(): SecretKey {
            val gen = KeyGenerator.getInstance("AES")
            gen.init(256)
            return gen.generateKey()
        }

        fun keyFromBase64(encoded: String): SecretKey =
            SecretKeySpec(Base64.getDecoder().decode(encoded), "AES")

        fun keyToBase64(key: SecretKey): String =
            Base64.getEncoder().encodeToString(key.encoded)
    }
}

class MultiDayService(
    private val vault: MultiDayVault,
    private val clock: Clock = Clock.systemUTC(),
    private val defaultTtl: Duration = Duration.ofDays(7),
) {
    fun startStay(
        id: String,
        kind: String,
        payload: String,
        ttl: Duration = defaultTtl,
        pinned: Boolean = false,
    ): MultiDayRecord {
        val now = clock.instant()
        val record = MultiDayRecord(
            id = id,
            kind = kind,
            createdAt = now,
            expiresAt = now.plus(ttl),
            plaintextPayload = payload,
            pinned = pinned,
        )
        vault.put(record)
        return record
    }

    fun get(id: String): MultiDayRecord? = vault.get(id)?.takeUnless { it.isExpired(clock.instant()) }

    fun end(id: String) {
        vault.delete(id)
    }

    fun tickAutoDestroy(): Int = vault.wipeExpired(clock.instant())
}
