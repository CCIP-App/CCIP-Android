package app.opass.ccip.network

import app.opass.ccip.model.Submission
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class ConfClient {
    companion object {
        private const val API_BASE_URL = "https://summit.g0v.tw"

        val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        val sConfService: ConfService by lazy {
            retrofit.create(ConfService::class.java)
        }

        fun get() = sConfService

        interface ConfService {
            @GET("/2018/static/ccip.json")
            fun submission(): Call<List<Submission>>
        }
    }
}
