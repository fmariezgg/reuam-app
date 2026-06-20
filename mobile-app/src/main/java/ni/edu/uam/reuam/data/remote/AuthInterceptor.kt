package ni.edu.uam.reuam.data.remote

import kotlinx.coroutines.runBlocking
import ni.edu.uam.reuam.data.repository.AuthTokenProvider
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor OkHttp que adjunta el header
 *
 *     Authorization: Bearer <firebase_id_token>
 *
 * a TODAS las peticiones salientes que tengan un usuario logueado.
 *
 * Para los endpoints públicos (GET /categories, GET /items, GET /items/{id})
 * no pasa nada si el header va de todos modos: el backend simplemente no
 * lo exige porque esas rutas usan `security { optional() }`. Por eso este
 * interceptor no necesita saber qué ruta es pública o protegida — siempre
 * intenta adjuntar el token si hay uno disponible, y si no hay usuario
 * logueado, deja la petición tal cual (sin header) y el backend responde
 * 401 solo si esa ruta en particular lo requiere.
 */
class AuthInterceptor(
    private val tokenProvider: AuthTokenProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // runBlocking es intencional y seguro aquí: OkHttp ya ejecuta los
        // interceptors en su propio dispatcher de I/O (nunca en el hilo
        // principal de Android), así que bloquear este hilo mientras se
        // obtiene el token no congela la UI.
        val token = runBlocking { tokenProvider.getValidIdToken() }

        val requestWithAuth = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(requestWithAuth)
    }
}
