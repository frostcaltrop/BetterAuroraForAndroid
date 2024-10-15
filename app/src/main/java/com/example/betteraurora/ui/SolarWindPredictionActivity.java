package com.example.betteraurora.ui;

import static com.example.betteraurora.network.NOAAService.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.betteraurora.R;
import com.example.betteraurora.model.ImageLoader;
import com.example.betteraurora.model.AnimationEntry;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SolarWindPredictionActivity extends AppCompatActivity implements ImageLoader.ImagesLoadListener {
    private Handler handler = new Handler();
    private ImageView imageView1, imageView2;
    private ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solarwind_prediction);

        imageView1 = findViewById(R.id.wsa);
        imageView2 = findViewById(R.id.huxt);

        imageLoader = new ImageLoader("WSA", this);

        loadInitialImages();
    }

    private void loadInitialImages() {
        // 假设 NOAAService 已经定义好并能够获取动画入口数据
        service.getWSAEntries().enqueue(new Callback<List<AnimationEntry>>() {
            @Override
            public void onResponse(Call<List<AnimationEntry>> call, Response<List<AnimationEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> urlsWSA = new ArrayList<>();
                    for (int i = 0; i < response.body().size(); i = i +1){
                        urlsWSA.add(response.body().get(i).getUrl());
                    }
                    imageLoader.loadImages(urlsWSA);
                }
            }

            @Override
            public void onFailure(Call<List<AnimationEntry>> call, Throwable t) {
                Log.e("SolarWind Prediction", "Failed to fetch WSA data", t);
                imageView1.setImageResource(R.drawable.load_failed);
            }
        });

        service.fetchImage("https://huxt-bucket.s3.eu-west-2.amazonaws.com/wsa_huxt_forecast_latest.png").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try (InputStream inputStream = response.body().byteStream()){
                        Bitmap huxt = BitmapFactory.decodeStream(inputStream);
                        imageView2.setImageBitmap(huxt);
                    }
                    catch (Exception e) {
                        Log.e("HUXt", "Error processing image stream", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("HUXt", "Error loading image", t);
                imageView2.setImageResource(R.drawable.load_failed);
            }
        });
    }

    @Override
    public void onImagesLoaded(String identifier, ConcurrentHashMap<Integer, Bitmap> images) {
        if (identifier.equals("WSA")) {
            startImagePlayback(imageView1, imageLoader);
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
