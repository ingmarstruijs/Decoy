package nl.decoy.plugins.api

/**
 * How a context was discovered (injected in tests; hardware later).
 */
sealed class DiscoveryInput {
    data class Qr(val payload: String) : DiscoveryInput()
    data class Url(val url: String) : DiscoveryInput()
    data class Nfc(val payload: String) : DiscoveryInput()
    data class ZoneCode(val code: String) : DiscoveryInput()
    data class Ble(val advertisement: String) : DiscoveryInput()
}

/**
 * Resolved context handed to a capability plugin.
 */
data class PluginContext(
    val contextId: String,
    val input: DiscoveryInput,
    val attributes: Map<String, String> = emptyMap(),
)

/**
 * Vendor form field as declared by a plugin / fixture schema.
 */
enum class FieldKind {
    /** Keep — contextual (pitch, table, notes). */
    Needed,

    /** Replace with single-use alias. */
    ContactAlias,

    /** Never collect in form; pay out-of-band. */
    PayElsewhere,

    /** Drop entirely (home address, marketing, forced account). */
    Omit,
}

data class FormField(
    val id: String,
    val label: String,
    val kind: FieldKind,
    val vendorRequestedValue: String? = null,
)

data class MenuItem(
    val id: String,
    val name: String,
    val priceCents: Int,
)

/**
 * UI state produced by a plugin for the Compose shell to render.
 * No Android types here — keep plugins-api JVM-pure.
 */
sealed class PluginUiState {
    data class Screen(
        val title: String,
        val body: String,
        val primaryActionLabel: String? = null,
    ) : PluginUiState()

    data class OrderForm(
        val title: String,
        val subtitle: String,
        val menu: List<MenuItem>,
        val fields: List<FormField>,
        val primaryActionLabel: String = "Pay & order",
    ) : PluginUiState()

    data class Error(val message: String) : PluginUiState()
}

enum class CompletionResult {
    Success,
    Cancelled,
    Failed,
}
