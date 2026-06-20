package ni.edu.uam.reuam.data.remote

import ni.edu.uam.reuam.BuildConfig
import ni.edu.uam.reuam.data.repository.AuthTokenProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Punto único de construcción del cliente HTTP de la app.
 *
 * BuildConfig.BASE_URL viene de mobile-app/build.gradle.kts y vale
 * "http://10.0.2.2:8080/api/v1/" — el alias que el Android Emulator
 * usa para llegar al localhost:8080 de la máquina donde corre el
 * backend Ktor. Si alguna vez corres en un dispositivo físico en la
 * misma red Wi-Fi que tu PC, cambia esa única línea por la IP local
 * de tu máquina (ej. "http://192.168.1.50:8080/api/v1/").
 */
object RetrofitInstance {

    // Una sola instancia para toda la app — evita crear un OkHttpClient
    // nuevo (con su propio pool de conexiones) por cada llamada.
    private val authTokenProvider = AuthTokenProvider()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authTokenProvider))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    val api: ReuamApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReuamApiService::class.java)
    }
}
