package com.example.betteraurora.ui;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.betteraurora.R;

import static com.example.betteraurora.network.NOAAService.service;
import com.example.betteraurora.util.ChartGenerator;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class IndexActivity extends AppCompatActivity {
    private BarChart barChart;
    private ImageView imageView1;
    private ChartGenerator chartGenerator;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        barChart = (BarChart) findViewById(R.id.Kp);
        imageView1 = findViewById(R.id.AE);
        chartGenerator = new ChartGenerator(this);
        initBarChart(barChart);
        fetchAndDisplayKp();
        fetchAndDisplayAE();
    }

    private void fetchAndDisplayAE() {
        ZonedDateTime utcTime = ZonedDateTime.now(ZoneOffset.UTC);

        // 分别提取年、月、日，并保存为String
        String year = String.valueOf(utcTime.getYear());
        String month = String.format("%02d", utcTime.getMonthValue());
        String day = String.format("%02d", utcTime.getDayOfMonth());

        String ae_url = "https://wdc.kugi.kyoto-u.ac.jp/ae_realtime/today/rtae_"+year+month+day+".png";

        service.fetchImage(ae_url).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                try (InputStream inputStream = response.body().byteStream()){
                    Bitmap ae = BitmapFactory.decodeStream(inputStream);
                    imageView1.setImageBitmap(ae);
                }
                catch (Exception e) {
                    Log.e("AE", "Error processing image stream", e);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                Log.e("AE", "Error processing image stream", t);
                imageView1.setImageResource(R.drawable.load_failed);
            }
        });
    }

    private void initBarChart(BarChart scatterChart) {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {

            scatterChart.setBackgroundColor(Color.WHITE);
            scatterChart.setNoDataTextColor(Color.BLACK);
        } else {

            scatterChart.setBackgroundColor(Color.BLACK);
            scatterChart.setNoDataTextColor(Color.WHITE);
        }
        scatterChart.setNoDataText("Loading");
    }

    private void fetchAndDisplayKp() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://services.swpc.noaa.gov/products/noaa-planetary-k-index.json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                barChart.setNoDataText("Load Failed");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        runOnUiThread(() -> {
                            try {
                                ArrayList<BarEntry> kpEntries = new ArrayList<>();
                                long baseTime = parseTimeToLong(jsonArray.getJSONArray(0).getString(0)); // 使用第一个元素作为基准时间

                                for (int i = 1; i < jsonArray.length(); i++) {
                                    JSONArray dataRow = jsonArray.getJSONArray(i);
                                    String timeTag = dataRow.getString(0);
                                    long time = parseTimeToLong(timeTag);  // 自定义时间解析函数

                                    float kp = Float.parseFloat(dataRow.getString(1));

                                    // 计算相对时间（小时数）
                                    float hoursFromBase = (time - baseTime) / (3600 * 1000f);  // 将时间转换为相对于基准时间的小时数
                                    kpEntries.add(new BarEntry(hoursFromBase, kp));
                                }

                                chartGenerator.generateBarData(kpEntries, "Kp Index", new ChartGenerator.ChartCallback() {
                                    @Override
                                    public void onScatterDataGenerated(ScatterData scatterData, String aname, String bname) {}

                                    @Override
                                    public void onError(Exception e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onBarDataGenerated(BarData barData, String name) {
                                        if (name.equals("Kp Index")) {
                                            barChart.setData(barData);
                                            barChart.setTouchEnabled(true);
                                            barChart.setDragEnabled(true);
                                            XAxis xAxis = barChart.getXAxis();
                                            xAxis.setGranularity(1f);  // 每小时为单位
                                            xAxis.setValueFormatter(new ValueFormatter() {
                                                @Override
                                                public String getAxisLabel(float value, AxisBase axis) {
                                                    // 将相对小时数转换回具体日期时间
                                                    long actualMillis = baseTime + (long) (value * 3600 * 1000);
                                                    return new SimpleDateFormat("MM-dd HH", Locale.getDefault()).format(new Date(actualMillis));
                                                }
                                            });
                                            barChart.getLegend().setEnabled(false);
                                            barChart.invalidate();
                                            barChart.notifyDataSetChanged();
                                        }
                                    }
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    barChart.setNoDataText("Loaded Failed");
                }
            }

        });
    }


    private long parseTimeToLong(String timeTag) {
        try {
            // 使用 SimpleDateFormat 将时间字符串转换为 UTC 时间戳
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));  // 将 SimpleDateFormat 设置为解析 UTC 时间
            long utcTime = sdf.parse(timeTag).getTime();   // 获取 UTC 时间戳

            // 加上 8 小时的毫秒数，转换为东八区（UTC+8）
            long adjustedTime = utcTime;// + (8 * 60 * 60 * 1000);  // UTC + 8 小时

            return adjustedTime;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    private Map<Integer, String> generateDateLabels(List<Long> timestamps) {
        Map<Integer, String> labels = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH", Locale.getDefault());
        for (int i = 0; i < timestamps.size(); i++) {
            labels.put(i, format.format(new Date(timestamps.get(i))));
        }
        return labels;
    }

}
