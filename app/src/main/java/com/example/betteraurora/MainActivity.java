package com.example.betteraurora;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;

import com.example.betteraurora.model.SolarWindMagField;
import com.example.betteraurora.model.SolarWindSpeed;
import com.example.betteraurora.network.NOAAService;
import com.example.betteraurora.network.RetrofitClientInstance;
import com.example.betteraurora.ui.AuroraActivity;
import com.example.betteraurora.ui.IndexActivity;
import com.example.betteraurora.ui.RealtimeCoronagraphActivity;
import com.example.betteraurora.ui.SolarWindDataActivity;
import com.example.betteraurora.ui.SolarWindPredictionActivity;
import com.example.betteraurora.R;

import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private TextView overviewText;
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
        overviewText = findViewById(R.id.overview);
        runnable = new Runnable() {
            @Override
            public void run() {
                fetchData();
                handler.postDelayed(this, 60000); // 更新频率为每分钟
            }
        };
        handler.post(runnable);
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



