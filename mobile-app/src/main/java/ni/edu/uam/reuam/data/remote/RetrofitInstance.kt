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
 * BuildConfig.BASE_URL viene de mobile-app/build.gradle.kts. Por defecto
 * usa "http://10.0.2.2:8080/api/v1/" para el emulador. Para una APK en
 * telefono fisico, se puede compilar pasando REUAM_BASE_URL con la IP
 * local de la computadora donde corre el backend.
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
