package nl.decoy.core.identity

import nl.decoy.fixtures.CampsiteOrderPlugin
import nl.decoy.plugins.api.FieldKind
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldClassifierTest {
    private val classifier = FieldClassifier(AliasGenerator(domain = "test.local"))

    @Test
    fun campsiteHeroKeepsPitchAliasesContactOmitsHomeBlocksIban() {
        val result = classifier.classify(
            fields = CampsiteOrderPlugin.VENDOR_FORM_SCHEMA,
            contextualValues = mapOf("pitch" to "42", "notes" to "no onions"),
        )

        assertTrue(result.outbound["pitch"] == "42")
        assertTrue(result.outbound["notes"] == "no onions")
        assertTrue(result.outbound["email"]!!.endsWith("@test.local"))
        assertTrue(result.aliasedFieldIds.containsAll(listOf("email", "phone")))
        assertTrue(result.omittedFieldIds.contains("home_address"))
        assertTrue(result.omittedFieldIds.contains("marketing_opt_in"))
        assertTrue(result.payElsewhereFieldIds.contains("iban"))
        assertFalse(result.outbound.containsKey("iban"))
        assertFalse(result.outbound.containsKey("home_address"))

        classifier.assertNoSecrets(result.outbound)
    }

    @Test(expected = IllegalArgumentException::class)
    fun assertNoSecretsRejectsIbanKey() {
        classifier.assertNoSecrets(mapOf("iban" to "NL00"))
    }

    @Test
    fun neededEmptyValueSkipped() {
        val result = classifier.classify(
            fields = listOf(
                nl.decoy.plugins.api.FormField("notes", "Notes", FieldKind.Needed),
            ),
            contextualValues = emptyMap(),
        )
        assertFalse(result.outbound.containsKey("notes"))
    }
}
