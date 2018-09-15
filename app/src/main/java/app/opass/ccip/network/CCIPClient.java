package app.opass.ccip.network;

import java.util.List;

import app.opass.ccip.model.Announcement;
import app.opass.ccip.model.Attendee;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class CCIPClient {

    public static final String API_BASE_URL = "https://ccip.opass.app";

    private static Retrofit retrofit;
    private static CCIPService sCCIPService;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    public static CCIPService get() {
        if (sCCIPService == null) {
            sCCIPService = getRetrofit().create(CCIPService.class);
        }

        return sCCIPService;
    }

    public interface CCIPService {
        @GET("/status")
        Call<Attendee> status(
                @Query("token") String token
        );

        @GET("/use/{scenario}")
        Call<Attendee> use(
                @Path("scenario") String scenario,
                @Query("token") String token
        );

        @GET("/announcement")
        Call<List<Announcement>> announcement();
    }
}
