package com.example.betteraurora.ui;

import static com.example.betteraurora.network.NOAAService.service;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.betteraurora.R;
import com.example.betteraurora.model.ImageLoader;
import com.example.betteraurora.model.AnimationEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RealtimeCoronagraphActivity extends AppCompatActivity implements ImageLoader.ImagesLoadListener {
    private Handler handler = new Handler();
    private ImageView imageView1, imageView2;
    private ImageLoader imageLoader1, imageLoader2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_coronagraph);

        imageView1 = findViewById(R.id.lascoC3);
        imageView2 = findViewById(R.id.lascoC2);

        imageLoader1 = new ImageLoader("C3", this);
        imageLoader2 = new ImageLoader("C2", this);

        loadInitialImages();
    }

    private void loadInitialImages() {
        // 假设 NOAAService 已经定义好并能够获取动画入口数据
        service.getLascoC3Entries().enqueue(new Callback<List<AnimationEntry>>() {
            @Override
            public void onResponse(Call<List<AnimationEntry>> call, Response<List<AnimationEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> urlsC3 = new ArrayList<>();
                    for (int i = 0; i < response.body().size(); i = i +1){
                        urlsC3.add(response.body().get(i).getUrl());
                    }
                    imageLoader1.loadImages(urlsC3);
                }
            }

            @Override
            public void onFailure(Call<List<AnimationEntry>> call, Throwable t) {
                Log.e("RealtimeCoronagraph", "Failed to fetch LASCO C3 data", t);
                imageView1.setImageResource(R.drawable.load_failed);
            }
        });

        service.getLascoC2Entries().enqueue(new Callback<List<AnimationEntry>>() {
            @Override
            public void onResponse(Call<List<AnimationEntry>> call, Response<List<AnimationEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> urlsC2 = new ArrayList<>();
                    for (int i = 0; i < response.body().size(); i = i +1){
                        urlsC2.add(response.body().get(i).getUrl());
                    }
                    imageLoader2.loadImages(urlsC2);
                }
            }

            @Override
            public void onFailure(Call<List<AnimationEntry>> call, Throwable t) {
                Log.e("RealtimeCoronagraph", "Failed to fetch LASCO C2 data", t);
                imageView1.setImageResource(R.drawable.load_failed);
            }
        });
    }

    @Override
    public void onImagesLoaded(String identifier, ConcurrentHashMap<Integer, Bitmap> images) {
        if (identifier.equals("C3")) {
            startImagePlayback(imageView1, imageLoader1);
        } else if (identifier.equals("C2")) {
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
                    currentIndex = (currentIndex + 1) % imageLoader.size(); // 假设 ImageLoader 有 size() 方法返回图片数量
                }
                handler.postDelayed(this, 60);  // 设置更新频率
            }
        };
        handler.post(imageUpdater);
    }
}
