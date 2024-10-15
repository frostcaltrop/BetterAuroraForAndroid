package com.example.betteraurora.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Url;

public class ImageLoader {
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private ConcurrentHashMap<Integer, Bitmap> orderedImages = new ConcurrentHashMap<>();
    private AtomicInteger loadedImages = new AtomicInteger(0);
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private OkHttpClient okHttpClient;
    public int size() {
        return orderedImages.size();
    }

    public interface ImagesLoadListener {
        void onImagesLoaded(String identifier, ConcurrentHashMap<Integer, Bitmap> images);
    }

    private ImagesLoadListener listener;
    private String identifier;

    public ImageLoader(String identifier, ImagesLoadListener listener) {
        this.identifier = identifier;
        this.listener = listener;
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void loadImages(List<String> urls) {
        for (int i = 0; i < urls.size(); i++) {
            final int index = i;
            executorService.submit(() -> {


                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://services.swpc.noaa.gov")
                        .client(okHttpClient)
                        .build();
                ImageService service = retrofit.create(ImageService.class);

                service.fetchImage(urls.get(index)).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try (InputStream inputStream = response.body().byteStream()) {
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                orderedImages.put(index, bitmap);
                                if (loadedImages.incrementAndGet() == urls.size()) {
                                    mainHandler.post(() -> listener.onImagesLoaded(identifier, orderedImages));
                                }
                            } catch (Exception e) {
                                try {
                                    throw e;
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("ImageLoader", "Error loading image", t);
                    }
                });
            });
        }
    }

    public Bitmap getImage(int index) {
        return orderedImages.get(index);
    }

    public boolean hasImage(int index) {
        return orderedImages.containsKey(index);
    }

    interface ImageService {
        @GET
        Call<ResponseBody> fetchImage(@Url String url);
    }
}
