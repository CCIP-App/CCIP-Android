package app.opass.ccip.network

import app.opass.ccip.model.Submission
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class ConfClient {
    companion object {
        private const val API_BASE_URL = "https://summit.g0v.tw"

        private lateinit var retrofit: Retrofit
        private lateinit var sConfService: ConfService

        fun getRetrofit(): Retrofit {
            if (!this::retrofit.isInitialized) {
                retrofit = Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }

            return retrofit
        }

        fun get(): ConfService {
            if (!this::sConfService.isInitialized) {
                sConfService = getRetrofit().create(ConfService::class.java)
            }

            return sConfService
        }

        interface ConfService {
            @GET("/2018/static/ccip.json")
            fun submission(): Call<List<Submission>>
        }
    }
}
