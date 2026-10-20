package nl.decoy.core.retention

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Minimal local breadcrumb after a context ends.
 * May keep a short [HistoryEntry.reference] (order / pay / ticket id) so One-shot
 * wipe does not erase the only proof the user needs — never full receipts or PII.
 */
enum class HistoryRetentionClass {
    OneShot,
    MultiDay,
}

enum class HistoryEndReason {
    Completed,
    Dismissed,
    Ended,
}

data class HistoryEntry(
    val id: String,
    val kind: String,
    val label: String,
    val retention: HistoryRetentionClass,
    val endedAt: Instant,
    val endReason: HistoryEndReason,
    val expiresAt: Instant,
    /** Order number, payment ref, or ticket id — short local proof only. */
    val reference: String? = null,
)

interface SessionHistoryStore {
    fun append(entry: HistoryEntry)
    fun list(): List<HistoryEntry>
    fun delete(id: String)
    fun clear()
    fun wipeExpired(now: Instant): Int
}

/** Newest-first in-memory log (Phase 6c). Disk/Keystore can replace later. */
class InMemorySessionHistoryStore : SessionHistoryStore {
    private val entries = ArrayDeque<HistoryEntry>()

    override fun append(entry: HistoryEntry) {
        entries.addFirst(entry)
    }

    override fun list(): List<HistoryEntry> = entries.toList()

    override fun delete(id: String) {
        entries.removeAll { it.id == id }
    }

    override fun clear() {
        entries.clear()
    }

    override fun wipeExpired(now: Instant): Int {
        val before = entries.size
        entries.removeAll { !now.isBefore(it.expiresAt) }
        return before - entries.size
    }
}

class SessionHistoryService(
    private val store: SessionHistoryStore,
    private val clock: Clock = Clock.systemUTC(),
    private val defaultTtl: Duration = Duration.ofDays(7),
    private val maxEntries: Int = 40,
) {
    fun record(
        kind: String,
        label: String,
        retention: HistoryRetentionClass,
        endReason: HistoryEndReason,
        reference: String? = null,
        ttl: Duration = defaultTtl,
    ): HistoryEntry {
        val now = clock.instant()
        val entry = HistoryEntry(
            id = UUID.randomUUID().toString(),
            kind = kind,
            label = label,
            retention = retention,
            endedAt = now,
            endReason = endReason,
            expiresAt = now.plus(ttl),
            reference = reference?.trim()?.takeIf { it.isNotEmpty() }?.take(64),
        )
        store.append(entry)
        trim()
        return entry
    }

    fun listRecent(): List<HistoryEntry> {
        wipeExpired()
        return store.list()
    }

    fun clear() = store.clear()

    fun wipeExpired(): Int = store.wipeExpired(clock.instant())

    private fun trim() {
        val all = store.list()
        if (all.size <= maxEntries) return
        all.drop(maxEntries).forEach { store.delete(it.id) }
    }
}
