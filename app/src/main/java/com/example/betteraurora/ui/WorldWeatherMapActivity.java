package com.example.betteraurora.ui;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.betteraurora.R;

public class WorldWeatherMapActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worldweathermap);

        WebView webView = findViewById(R.id.webviewWorldWeatherMap);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.loadUrl("https://map.worldweatheronline.com/");
    }
}
