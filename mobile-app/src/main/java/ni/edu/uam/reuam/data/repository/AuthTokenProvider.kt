package ni.edu.uam.reuam.data.repository

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Responsable único de obtener el Firebase ID token del usuario actual.
 *
 * Por qué existe esta clase separada en vez de llamar a FirebaseAuth
 * directamente desde el interceptor: así el interceptor de red no
 * depende de Firebase directamente, solo de esta interfaz pequeña.
 * Si algún día cambias el proveedor de auth, solo tocas esta clase.
 *
 * getIdToken(true) fuerza la renovación si el token está vencido o
 * próximo a vencer; Firebase decide internamente si de verdad necesita
 * llamar a la red o puede devolver el token cacheado.
 */
class AuthTokenProvider {

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    /** true si hay un usuario de Firebase con sesión activa. */
    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    /**
     * Devuelve el ID token actual, renovándolo si es necesario.
     * Devuelve null si no hay usuario logueado o si falla la obtención
     * (p. ej. sin conexión a internet).
     */
    suspend fun getValidIdToken(): String? {
        val user = auth.currentUser ?: return null
        return withContext(Dispatchers.IO) {
            try {
                val result = Tasks.await(user.getIdToken(true))
                result.token
            } catch (_: Exception) {
                null
            }
        }
    }
}
