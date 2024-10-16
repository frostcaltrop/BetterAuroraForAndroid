package com.example.betteraurora;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;

import com.example.betteraurora.model.SolarWindMagField;
import com.example.betteraurora.model.SolarWindSpeed;
import com.example.betteraurora.network.NOAAService;
import com.example.betteraurora.network.RetrofitClientInstance;
import com.example.betteraurora.ui.AuroraActivity;
import com.example.betteraurora.ui.ForecastActivity;
import com.example.betteraurora.ui.IndexActivity;
import com.example.betteraurora.ui.LightPollutionActivity;
import com.example.betteraurora.ui.QWeatherMapActivity;
import com.example.betteraurora.ui.RealtimeCoronagraphActivity;
import com.example.betteraurora.ui.SolarWindDataActivity;
import com.example.betteraurora.ui.SolarWindPredictionActivity;
import com.example.betteraurora.ui.WorldWeatherMapActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private TextView overviewText;
    private TextView scaleText;
    private Runnable runnable;
    private String windSpeedData = "";
    private String magneticFieldData = "";
    private final Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);




//        // Applying window insets for system bars
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).toPlatformInsets();
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        // Setting up button listeners
        findViewById(R.id.buttonRealtimeCoronagraph).setOnClickListener(v -> openRealtimeCoronagraph());
        findViewById(R.id.buttonSolarWindPrediction).setOnClickListener(v -> openSolarWindPrediction());
        findViewById(R.id.buttonIndex).setOnClickListener(v -> openIndex());
        findViewById(R.id.buttonSolarWindData).setOnClickListener(v -> openSolarWindData());
        findViewById(R.id.buttonAurora).setOnClickListener(v -> openAurora());
        findViewById(R.id.buttonForecast).setOnClickListener(v-> openForecast());
        findViewById(R.id.buttonLightPollution).setOnClickListener(v -> openLightPollution());
        findViewById(R.id.buttonQWeatherMap).setOnClickListener(v -> openQWeatherMap());
        findViewById(R.id.buttonWorldWeatherMap).setOnClickListener(v -> openWorldWeatherMap());
        overviewText = findViewById(R.id.SolarWind);
        scaleText = findViewById(R.id.scale);
        runnable = new Runnable() {
            @Override
            public void run() {
                fetchData();
                handler.postDelayed(this, 60000); // 更新频率为每分钟
            }
        };
        handler.post(runnable);
    }

    private void openWorldWeatherMap() {
        Intent intent = new Intent(this, WorldWeatherMapActivity.class);
        startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 || requestCode == 2){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(this, QWeatherMapActivity.class);
                startActivity(intent);
            }
            else {
                runOnUiThread(()->Toast.makeText(this, "Location permission is required to open QWeatherMap", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void openQWeatherMap() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        else {
            Intent intent = new Intent(this, QWeatherMapActivity.class);
            startActivity(intent);
        }
    }

    private void openLightPollution() {
        Intent intent = new Intent(this, LightPollutionActivity.class);
        startActivity(intent);
    }

    private void openForecast() {
        Intent intent = new Intent(this, ForecastActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    private void openRealtimeCoronagraph() {
        Intent intent = new Intent(this, RealtimeCoronagraphActivity.class);
        startActivity(intent);
    }

    private void openSolarWindPrediction() {
        Intent intent = new Intent(this, SolarWindPredictionActivity.class);
        startActivity(intent);
    }

    private void openIndex() {
        Intent intent = new Intent(this, IndexActivity.class);
        startActivity(intent);
    }

    private void openSolarWindData() {
        Intent intent = new Intent(this, SolarWindDataActivity.class);
        startActivity(intent);
    }

    private void openAurora() {
        Intent intent = new Intent(this, AuroraActivity.class);
        startActivity(intent);
    }

    private void fetchData() {
        NOAAService service = RetrofitClientInstance.getRetrofitInstance().create(NOAAService.class);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://services.swpc.noaa.gov/products/noaa-scales.json").build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(()->scaleText.setText("Load Scale Failed"));
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful()){
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONObject item0 = jsonObject.getJSONObject("0");
                        String timestamp0 = item0.getString("DateStamp") + "(today):\n";
                        String R0 = "R:" + item0.getJSONObject("R").getString("Scale") + "\n";
                        String S0 = "R:" + item0.getJSONObject("S").getString("Scale") + "\n";
                        String G0 = "G:" + item0.getJSONObject("G").getString("Scale") + "\n";

                        JSONObject item_1 = jsonObject.getJSONObject("-1");
                        String timestamp_1 = item_1.getString("DateStamp") + "(last 24 hours):\n";
                        String R_1 = "R:" + item_1.getJSONObject("R").getString("Scale") + "\n";
                        String S_1 = "R:" + item_1.getJSONObject("S").getString("Scale") + "\n";
                        String G_1 = "G:" + item_1.getJSONObject("G").getString("Scale") + "\n";

                        JSONObject item1 = jsonObject.getJSONObject("1");
                        String timestamp1 = item1.getString("DateStamp") + "(next 24 hours):\n";
                        String R1 = "R:" + item1.getJSONObject("R").getString("Scale") + "\n";
                        String S1 = "R:" + item1.getJSONObject("S").getString("Scale") + "\n";
                        String G1 = "G:" + item1.getJSONObject("G").getString("Scale") + "\n";

                        JSONObject item2 = jsonObject.getJSONObject("2");
                        String timestamp2 = item2.getString("DateStamp") + ":\n";
                        String R2 = "R:" + item2.getJSONObject("R").getString("Scale") + "\n";
                        String S2 = "R:" + item2.getJSONObject("S").getString("Scale") + "\n";
                        String G2 = "G:" + item2.getJSONObject("G").getString("Scale") + "\n";

                        JSONObject item3 = jsonObject.getJSONObject("3");
                        String timestamp3 = item3.getString("DateStamp") + ":\n";
                        String R3 = "R:" + item3.getJSONObject("R").getString("Scale") + "\n";
                        String S3 = "R:" + item3.getJSONObject("S").getString("Scale") + "\n";
                        String G3 = "G:" + item3.getJSONObject("G").getString("Scale");

                        String scale = timestamp0 + R0 + S0 + G0 + timestamp_1 + R_1 + S_1 + G_1 + timestamp1 + R1 + S1 + G1 +
                                timestamp2 + R2 + S2 + G2 + timestamp3 + R3 + S3 + G3;

                        runOnUiThread(()->scaleText.setText(scale));
//                        for (int i = 0; i < jsonObject.length();i++){
//                                JSONArray dataRow = jsonObject.getJSONArray(i);
//
//                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                }
                else {
                    runOnUiThread(()->scaleText.setText("Load Scale Failed:" + response.code()));
                }
            }
        });

        // 获取太阳风速度
        service.getSolarWindSpeed().enqueue(new Callback<SolarWindSpeed>() {
            @Override
            public void onResponse(Call<SolarWindSpeed> call, Response<SolarWindSpeed> response) {
                if (response.isSuccessful()) {
                    windSpeedData = "Wind Speed: " + response.body().getWindSpeed();
                    updateOverviewText();
                }
            }
            @Override
            public void onFailure(Call<SolarWindSpeed> call, Throwable t) {
                windSpeedData = "Error fetching wind speed.";
                updateOverviewText();
            }
        });

        // 获取磁场数据
        service.getSolarWindMagField().enqueue(new Callback<SolarWindMagField>() {
            @Override
            public void onResponse(Call<SolarWindMagField> call, Response<SolarWindMagField> response) {
                if (response.isSuccessful()) {
                    magneticFieldData = "\nBt: " + response.body().getBt() + ", Bz: " + response.body().getBz();
                    updateOverviewText();
                }
            }

            @Override
            public void onFailure(Call<SolarWindMagField> call, Throwable t) {
                magneticFieldData = "\nError fetching magnetic field data.";
                updateOverviewText();
            }
        });
    }

    private void updateOverviewText() {
        overviewText.setText(windSpeedData + magneticFieldData);
    }
}



