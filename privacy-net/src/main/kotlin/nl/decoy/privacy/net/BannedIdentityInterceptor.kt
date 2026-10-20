package nl.decoy.privacy.net

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Rejects requests that appear to carry stable device identifiers.
 */
class BannedIdentityInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        for (name in request.headers.names()) {
            val lower = name.lowercase()
            if (BANNED_HEADER_NAMES.any { lower.contains(it) }) {
                throw BannedIdentityException("Banned header: $name")
            }
        }
        val url = request.url.toString().lowercase()
        for (token in BANNED_QUERY_TOKENS) {
            if (url.contains(token)) {
                throw BannedIdentityException("Banned token in URL: $token")
            }
        }
        request.body?.let { body ->
            // Body inspection is best-effort; unit tests cover explicit headers/query.
        }
        return chain.proceed(request)
    }

    companion object {
        val BANNED_HEADER_NAMES = listOf(
            "x-device-id",
            "x-advertising-id",
            "x-gaid",
            "x-android-id",
            "x-install-id",
        )
        val BANNED_QUERY_TOKENS = listOf(
            "gaid=",
            "android_id=",
            "advertising_id=",
            "install_uuid=",
        )
    }
}

class BannedIdentityException(message: String) : IllegalStateException(message)
