package com.example.betteraurora.ui;

import static com.example.betteraurora.network.NOAAService.service;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.betteraurora.R;
import com.example.betteraurora.model.AnimationEntry;
import com.example.betteraurora.model.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuroraActivity extends AppCompatActivity implements ImageLoader.ImagesLoadListener {
    private Handler handler = new Handler();
    private ImageView imageView1, imageView2;
    private ImageLoader imageLoader1, imageLoader2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aurora);

        imageView1 = findViewById(R.id.auroraLastFrame);
        imageView2 = findViewById(R.id.auroraDynamic);

        imageLoader1 = new ImageLoader("LastFrame", this);
        imageLoader2 = new ImageLoader("Dynamic", this);

        loadInitialImages();
    }

    private void loadInitialImages() {
        // 假设 NOAAService 已经定义好并能够获取动画入口数据
        service.getDynamicAuroraEntries().enqueue(new Callback<List<AnimationEntry>>() {
            @Override
            public void onResponse(Call<List<AnimationEntry>> call, Response<List<AnimationEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> urlsDynamic = new ArrayList<>();
                    for (int i = 0; i < response.body().size(); i = i +1){
                        urlsDynamic.add(response.body().get(i).getUrl());
                    }
                    imageLoader2.loadImages(urlsDynamic);
                }
            }

            @Override
            public void onFailure(Call<List<AnimationEntry>> call, Throwable t) {
                Log.e("Aurora", "Failed to fetch Dynamic Aurora pics", t);
                runOnUiThread(()->imageView2.setImageResource(R.drawable.load_failed));
            }
        });

        service.getLastFrameAuroraEntries().enqueue(new Callback<List<AnimationEntry>>() {
            @Override
            public void onResponse(Call<List<AnimationEntry>> call, Response<List<AnimationEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> urlsLastFrame = new ArrayList<>();
                    urlsLastFrame.add(response.body().get(response.body().size() - 1).getUrl());
                    imageLoader1.loadImages(urlsLastFrame);
                }
            }

            @Override
            public void onFailure(Call<List<AnimationEntry>> call, Throwable t) {
                Log.e("RealtimeCoronagraph", "Failed to fetch Static Aurora pic", t);
                runOnUiThread(()->imageView1.setImageResource(R.drawable.load_failed));
            }
        });
    }

    @Override
    public void onImagesLoaded(String identifier, ConcurrentHashMap<Integer, Bitmap> images) {
        if (identifier.equals("LastFrame")) {
            startImagePlayback(imageView1, imageLoader1);
        } else if (identifier.equals("Dynamic")) {
            startImagePlayback(imageView2, imageLoader2);
        }
    }

    private void startImagePlayback(ImageView imageView, ImageLoader imageLoader) {
        Runnable imageUpdater = new Runnable() {
            private int currentIndex = 0;

            @Override
            public void run() {
                if (imageLoader.hasImage(currentIndex)) {
                    imageView.setImageBitmap(imageLoader.getImage(currentIndex));
                    currentIndex = (currentIndex + 1) % imageLoader.size();
                }
                handler.postDelayed(this, 60);
            }
        };
        handler.post(imageUpdater);
    }
}
