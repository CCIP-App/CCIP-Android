package app.opass.ccip.network

import app.opass.ccip.model.Announcement
import app.opass.ccip.model.Attendee
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

object CCIPClient {
    private const val API_BASE_URL = "https://ccip.opass.app"

    private lateinit var retrofit: Retrofit
    private lateinit var sCCIPService: CCIPService

    fun getRetrofit(): Retrofit {
        if (!this::retrofit.isInitialized) {
            retrofit = Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        return retrofit
    }

    fun get(): CCIPService {
        if (!this::sCCIPService.isInitialized) {
            sCCIPService = getRetrofit().create(CCIPService::class.java)
        }

        return sCCIPService
    }

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
