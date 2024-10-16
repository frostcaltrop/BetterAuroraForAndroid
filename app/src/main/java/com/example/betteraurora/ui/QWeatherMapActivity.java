package com.example.betteraurora.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.betteraurora.R;

public class QWeatherMapActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qwearthermap);

        WebView webView = findViewById(R.id.webviewQWeatherMap);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);  // 如果网页使用DOM存储

        // 设置 WebViewClient
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i("WebView", "Page loaded: " + url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e("WebView", "Error loading page: " + error.getDescription() + ", code: " + error.getErrorCode());
            }
        });

        checkLocationAndLoad(webView);
    }

    private void checkLocationAndLoad(WebView webView) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String formattedLatitude = String.format("%.2f", latitude);
                String formattedLongitude = String.format("%.2f", longitude);
                String url = "https://map.qweather.com/index.html?lat=" + formattedLatitude + "&lon=" + formattedLongitude + "&level=4&layer=cloud";
                webView.loadUrl(url);
            } else {
                webView.loadUrl("https://map.qweather.com/");
            }
        } else {
            webView.loadUrl("https://map.qweather.com/");
        }
    }
}

