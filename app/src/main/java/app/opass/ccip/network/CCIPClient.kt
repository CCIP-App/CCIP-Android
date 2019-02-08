package app.opass.ccip.network

import app.opass.ccip.model.Announcement
import app.opass.ccip.model.Attendee
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class CCIPClient {
    companion object {
        private const val API_BASE_URL = "https://ccip.opass.app"

        val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        private val sCCIPService: CCIPService by lazy {
            retrofit.create(CCIPService::class.java)
        }

        fun get(): CCIPService = sCCIPService

        interface CCIPService {
            @GET("/status")
            fun status(
                @Query("token") token: String?
            ): Call<Attendee>

            @GET("/use/{scenario}")
            fun use(
                @Path("scenario") scenario: String,
                @Query("token") token: String?
            ): Call<Attendee>

            @GET("/announcement")
            fun announcement(): Call<List<Announcement>>
        }
    }
}
