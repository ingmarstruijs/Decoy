package nl.decoy.core.identity

import java.security.SecureRandom

/**
 * Local single-use alias emails. No mailbox relay in Phase 1 — deterministic for tests when seeded.
 */
class AliasGenerator(
    private val domain: String = "alias.decoy.local",
    private val random: SecureRandom = SecureRandom(),
) {
    fun nextEmail(prefix: String = "order"): String {
        val token = ByteArray(6).also { random.nextBytes(it) }
            .joinToString("") { "%02x".format(it) }
        return "$prefix-$token@$domain"
    }
}
