package com.example.betteraurora.network;

import com.example.betteraurora.model.SolarWindMagField;
import com.example.betteraurora.model.SolarWindSpeed;

import java.util.List;
import com.example.betteraurora.model.AnimationEntry;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface NOAAService {
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://services.swpc.noaa.gov/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    NOAAService service = retrofit.create(NOAAService.class);

    @GET("products/summary/solar-wind-speed.json")
    Call<SolarWindSpeed> getSolarWindSpeed();

    @GET("products/summary/solar-wind-mag-field.json")
    Call<SolarWindMagField> getSolarWindMagField();

    @GET("products/animations/lasco-c3.json")
    Call<List<AnimationEntry>> getLascoC3Entries();

    @GET("products/animations/lasco-c2.json")
    Call<List<AnimationEntry>> getLascoC2Entries();
    @GET
    Call<ResponseBody> fetchImage(@Url String url);

    @GET("products/animations/ovation_north_24h.json")
    Call<List<AnimationEntry>> getDynamicAuroraEntries();

    @GET("products/animations/ovation_north_24h.json")
    Call<List<AnimationEntry>> getLastFrameAuroraEntries();

    @GET("products/animations/enlil.json")
    Call<List<AnimationEntry>> getWSAEntries();
}
