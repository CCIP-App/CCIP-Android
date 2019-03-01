package app.opass.ccip.network

import app.opass.ccip.model.Session
import app.opass.ccip.util.JsonUtil
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

class ConfClient {
    companion object {
        private const val API_BASE_URL = "https://opass.app"

        private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(JsonUtil.GSON))
                .build()
        }
        private val sConfService: ConfService by lazy {
            retrofit.create(ConfService::class.java)
        }

        fun get() = sConfService

        interface ConfService {
            @GET
            fun session(@Url url: String): Call<List<Session>>
        }
    }
}
