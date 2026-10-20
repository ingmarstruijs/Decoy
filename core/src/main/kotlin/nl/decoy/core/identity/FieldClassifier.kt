package nl.decoy.core.identity

import nl.decoy.plugins.api.FieldKind
import nl.decoy.plugins.api.FormField

data class ClassifiedSubmission(
    /** Values Decoy would send to a venue capability API (never IBAN/home). */
    val outbound: Map<String, String>,
    val omittedFieldIds: List<String>,
    val payElsewhereFieldIds: List<String>,
    val aliasedFieldIds: List<String>,
)

/**
 * Applies Decoy field policy to a vendor form schema + user contextual values.
 */
class FieldClassifier(
    private val aliases: AliasGenerator = AliasGenerator(),
) {
    fun classify(
        fields: List<FormField>,
        contextualValues: Map<String, String>,
    ): ClassifiedSubmission {
        val outbound = linkedMapOf<String, String>()
        val omitted = mutableListOf<String>()
        val payElsewhere = mutableListOf<String>()
        val aliased = mutableListOf<String>()

        for (field in fields) {
            when (field.kind) {
                FieldKind.Needed -> {
                    val value = contextualValues[field.id]
                        ?: field.vendorRequestedValue
                        ?: ""
                    if (value.isNotBlank()) {
                        outbound[field.id] = value
                    }
                }
                FieldKind.ContactAlias -> {
                    outbound[field.id] = aliases.nextEmail()
                    aliased += field.id
                }
                FieldKind.PayElsewhere -> {
                    payElsewhere += field.id
                }
                FieldKind.Omit -> {
                    omitted += field.id
                }
                FieldKind.ConsentRequired -> {
                    val consented = contextualValues["consent_${field.id}"] == "true"
                    val value = contextualValues[field.id]
                    if (consented && !value.isNullOrBlank()) {
                        outbound[field.id] = value
                    } else {
                        omitted += field.id
                    }
                }
            }
        }
        return ClassifiedSubmission(
            outbound = outbound,
            omittedFieldIds = omitted,
            payElsewhereFieldIds = payElsewhere,
            aliasedFieldIds = aliased,
        )
    }

    fun assertNoSecrets(outbound: Map<String, String>) {
        val bannedKeys = listOf("iban", "card", "bank", "account_number", "home_address", "address")
        for ((key, value) in outbound) {
            val lowerKey = key.lowercase()
            require(bannedKeys.none { lowerKey.contains(it) }) {
                "Outbound must not include payment/home field: $key"
            }
            val lowerVal = value.lowercase()
            require(!lowerVal.contains("nl00") && !lowerVal.matches(Regex(".*\\biban\\b.*"))) {
                "Outbound value looks like IBAN for $key"
            }
        }
    }
}
