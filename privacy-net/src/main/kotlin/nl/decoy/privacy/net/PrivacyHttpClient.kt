package nl.decoy.privacy.net

import okhttp3.CookieJar
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Shared HTTP client with privacy defaults: empty cookie jar, no stable IDs, generic UA.
 */
object PrivacyHttpClient {
    const val GENERIC_USER_AGENT = "Decoy/0.1 (Android; FOSS)"

    fun create(
        extraInterceptors: List<okhttp3.Interceptor> = emptyList(),
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .cookieJar(CookieJar.NO_COOKIES)
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", GENERIC_USER_AGENT)
                    .removeHeader("Referer")
                    .build()
                chain.proceed(req)
            }
            .addInterceptor(BannedIdentityInterceptor())
        extraInterceptors.forEach { builder.addInterceptor(it) }
        return builder.build()
    }
}
