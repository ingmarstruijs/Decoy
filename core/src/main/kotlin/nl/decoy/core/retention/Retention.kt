package nl.decoy.core.retention

import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.Instant

enum class RetentionClass {
    OneShot,
    MultiDay,
}

/**
 * Ephemeral session for One-shot (or Multi-day stub). Auto-destroy by default.
 */
data class SessionRecord(
    val sessionId: String,
    val retention: RetentionClass,
    val createdAt: Instant,
    val endsAt: Instant?,
    val safetyTtl: Duration,
    val pinned: Boolean = false,
    val attributes: Map<String, String> = emptyMap(),
) {
    fun isExpired(now: Instant): Boolean {
        if (pinned) return false
        if (endsAt != null && !now.isBefore(endsAt)) return true
        return !now.isBefore(createdAt.plus(safetyTtl))
    }
}

interface SessionStore {
    fun put(session: SessionRecord)
    fun get(sessionId: String): SessionRecord?
    fun remove(sessionId: String)
    fun wipeExpired(now: Instant): Int
    fun clearAll()
}

class InMemorySessionStore : SessionStore {
    private val sessions = linkedMapOf<String, SessionRecord>()

    override fun put(session: SessionRecord) {
        sessions[session.sessionId] = session
    }

    override fun get(sessionId: String): SessionRecord? = sessions[sessionId]

    override fun remove(sessionId: String) {
        sessions.remove(sessionId)
    }

    override fun wipeExpired(now: Instant): Int {
        val expired = sessions.filterValues { it.isExpired(now) }.keys.toList()
        expired.forEach { sessions.remove(it) }
        return expired.size
    }

    override fun clearAll() {
        sessions.clear()
    }

    fun size(): Int = sessions.size
}

object SessionIds {
    private val random = SecureRandom()

    fun newNonce(): String {
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

class RetentionController(
    private val store: SessionStore,
    private val clock: Clock = Clock.systemUTC(),
    private val defaultOneShotSafetyTtl: Duration = Duration.ofHours(12),
    private val defaultMultiDayTtl: Duration = Duration.ofDays(7),
) {
    fun startOneShot(attributes: Map<String, String> = emptyMap()): SessionRecord {
        val now = clock.instant()
        val session = SessionRecord(
            sessionId = SessionIds.newNonce(),
            retention = RetentionClass.OneShot,
            createdAt = now,
            endsAt = null,
            safetyTtl = defaultOneShotSafetyTtl,
            attributes = attributes,
        )
        store.put(session)
        return session
    }

    fun endSession(sessionId: String) {
        store.remove(sessionId)
    }

    fun pin(sessionId: String) {
        val current = store.get(sessionId) ?: return
        store.put(current.copy(pinned = true))
    }

    fun tickAutoDestroy(): Int = store.wipeExpired(clock.instant())

    fun defaultMultiDayTtl(): Duration = defaultMultiDayTtl
}
