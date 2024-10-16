package com.example.betteraurora.ui;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.betteraurora.R;

public class LightPollutionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lightpollution);

        WebView webView = findViewById(R.id.webviewLightPollution);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.loadUrl("https://www.darkmap.cn/");
    }
}
