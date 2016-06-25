
package org.coscup.ccip.network;

import org.coscup.ccip.model.Attendee;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class CCIPClient{

    public static final String API_BASE_URL = "https://coscup.cprteam.org";

    private static CCIPService sCCIPService;

    public static CCIPService get() {
        if (sCCIPService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            sCCIPService = retrofit.create(CCIPService.class);
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
    }
}
