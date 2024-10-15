package com.example.betteraurora.ui;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.betteraurora.R;
import com.example.betteraurora.util.ChartGenerator;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SolarWindDataActivity extends AppCompatActivity {
    private ScatterChart scatterChart1;
    private ScatterChart scatterChart2;
    private ScatterChart scatterChart3;
    private ScatterChart scatterChart4;
    private ScatterChart scatterChart5;
    private ScatterChart scatterChart6;
    private ChartGenerator chartGenerator;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solarwind_data);
        scatterChart1 = (ScatterChart) findViewById(R.id.BtBz);
        scatterChart2 = (ScatterChart) findViewById(R.id.Phi);
        scatterChart3 = (ScatterChart) findViewById(R.id.Speed);
        scatterChart4 = (ScatterChart) findViewById(R.id.Density);
        scatterChart5 = (ScatterChart) findViewById(R.id.Temperature);
        scatterChart6 = (ScatterChart) findViewById(R.id.BxBy);
        chartGenerator = new ChartGenerator(this);
        initScatterCharts();
        fetchAndDisplayPlasma();
        fetchAndDisplaySolarWind();
    }

    private void initScatterChart(ScatterChart scatterChart) {
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
    private void initScatterCharts() {
        initScatterChart(scatterChart1);
        initScatterChart(scatterChart2);
        initScatterChart(scatterChart3);
        initScatterChart(scatterChart4);
        initScatterChart(scatterChart5);
        initScatterChart(scatterChart6);
//        int screenWidth = this.getResources().getDisplayMetrics().widthPixels;
//        scatterChart1.setLayoutParams(new ScatterChart.LayoutParams(screenWidth, 3*screenWidth/4));
//        scatterChart2.setLayoutParams(new ScatterChart.LayoutParams(screenWidth, 3*screenWidth/4));
//        scatterChart3.setLayoutParams(new ScatterChart.LayoutParams(screenWidth, 3*screenWidth/4));
//        scatterChart4.setLayoutParams(new ScatterChart.LayoutParams(screenWidth, 3*screenWidth/4));
//        scatterChart5.setLayoutParams(new ScatterChart.LayoutParams(screenWidth, 3*screenWidth/4));
    }

    private void fetchAndDisplayPlasma() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://services.swpc.noaa.gov/text/rtsw/data/plasma-1-day.i.json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                scatterChart3.setNoDataText("Load Failed");
                scatterChart4.setNoDataText("Load Failed");
                scatterChart5.setNoDataText("Load Failed");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()){
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        runOnUiThread(() ->{
                            try {
                                ArrayList<Entry> speedEntries = new ArrayList<>();
                                ArrayList<Entry> densityEntries = new ArrayList<>();
                                ArrayList<Entry> temperatureEntries = new ArrayList<>();

                                for (int i = 1; i < jsonArray.length(); i++) {
                                    JSONArray dataRow = jsonArray.getJSONArray(i);

                                    // 获取 time_tag（假设它是第 0 列），将其转换为图表的 X 轴值
                                    String timeTag = dataRow.getString(0);
                                    long time = parseTimeToLong(timeTag);  // 自定义时间解析函数

                                    // 提取 speed、density、temperature（假设它们分别在第 1、2、3 列）
                                    float speed = Float.parseFloat(dataRow.getString(1));
                                    float density = Float.parseFloat(dataRow.getString(2));
                                    float temperature = Float.parseFloat(dataRow.getString(3));


                                    // 分别添加到各个列表中
                                    speedEntries.add(new Entry(time, speed));
                                    densityEntries.add(new Entry(time, density));
                                    temperatureEntries.add(new Entry(time, temperature));

                              }
                                chartGenerator.generateScatterData(speedEntries, null, "Speed Data", null, new ChartGenerator.ChartCallback() {
                                    @Override
                                        public void onScatterDataGenerated(ScatterData scatterData, String aname, String bname) {
                                        if (aname.equals("Speed Data") && bname == null)
                                        {
                                            scatterChart3.setData(scatterData);
                                            scatterChart3.setTouchEnabled(true);
                                            scatterChart3.setDragEnabled(true);
                                            XAxis xAxis = scatterChart3.getXAxis();
                                            xAxis.setGranularity(1f);  // 每小时为单位
                                            xAxis.setValueFormatter(new ValueFormatter() {
                                                @Override
                                                public String getAxisLabel(float value, AxisBase axis) {
                                                    long millis = (long) value;  // 转换为毫秒数
                                                    return new SimpleDateFormat("MM-dd HH", Locale.getDefault()).format(millis);
                                                }
                                            });
                                            scatterChart3.invalidate();
                                            scatterChart3.notifyDataSetChanged();
                                        }
                                    }
                                    @Override
                                    public void onError(Exception e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onBarDataGenerated(BarData barData, String name){

                                    }
                                });

                                chartGenerator.generateScatterData(densityEntries, null, "Density Data", null, new ChartGenerator.ChartCallback() {
                                    @Override
                                    public void onScatterDataGenerated(ScatterData scatterData, String aname, String bname) {
                                        if (aname.equals("Density Data") && bname == null)
                                        {
                                            scatterChart4.setData(scatterData);
                                            scatterChart4.setTouchEnabled(true);
                                            scatterChart4.setDragEnabled(true);
                                            XAxis xAxis = scatterChart4.getXAxis();
                                            xAxis.setGranularity(1f);  // 每小时为单位
                                            xAxis.setValueFormatter(new ValueFormatter() {
                                                @Override
                                                public String getAxisLabel(float value, AxisBase axis) {
                                                    long millis = (long) value;  // 转换为毫秒数
                                                    return new SimpleDateFormat("MM-dd HH", Locale.getDefault()).format(millis);
                                                }
                                            });
                                            scatterChart4.invalidate();
                                            scatterChart4.notifyDataSetChanged();
                                        }
                                    }
                                    @Override
                                    public void onError(Exception e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onBarDataGenerated(BarData barData, String name){

                                    }
                                });

                                chartGenerator.generateScatterData(temperatureEntries, null, "Temperature Data", null, new ChartGenerator.ChartCallback() {
                                    @Override
                                    public void onScatterDataGenerated(ScatterData scatterData, String aname, String bname) {
                                        if (aname.equals("Temperature Data") && bname == null)
                                        {
                                            scatterChart5.setData(scatterData);
                                            scatterChart5.setTouchEnabled(true);
                                            scatterChart5.setDragEnabled(true);
                                            XAxis xAxis = scatterChart5.getXAxis();
                                            xAxis.setGranularity(1f);  // 每小时为单位
                                            xAxis.setValueFormatter(new ValueFormatter() {
                                                @Override
                                                public String getAxisLabel(float value, AxisBase axis) {
                                                    long millis = (long) value;  // 转换为毫秒数
                                                    return new SimpleDateFormat("MM-dd HH", Locale.getDefault()).format(millis);
                                                }
                                            });
                                            scatterChart5.invalidate();
                                            scatterChart5.notifyDataSetChanged();
                                        }
                                    }
                                    @Override
                                    public void onError(Exception e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onBarDataGenerated(BarData barData, String name){

                                    }
                                });

                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        });

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    scatterChart3.setNoDataText("Load Failed");
                    scatterChart4.setNoDataText("Load Failed");
                    scatterChart5.setNoDataText("Load Failed");
                }
            }
        });
    }

    private void fetchAndDisplaySolarWind() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://services.swpc.noaa.gov/text/rtsw/data/mag-1-day.i.json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                scatterChart1.setNoDataText("Load Failed");
                scatterChart2.setNoDataText("Load Failed");
                scatterChart6.setNoDataText("Load Failed");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()){
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        runOnUiThread(() ->{
                            try {
                                ArrayList<Entry> btEntries = new ArrayList<>();
                                ArrayList<Entry> bzEntries = new ArrayList<>();
                                ArrayList<Entry> phiEntries = new ArrayList<>();
                                ArrayList<Entry> bxEntries = new ArrayList<>();
                                ArrayList<Entry> byEntries = new ArrayList<>();

                                for (int i = 1; i < jsonArray.length(); i++) {
                                    JSONArray dataRow = jsonArray.getJSONArray(i);

                                    String timeTag = dataRow.getString(0);
                                    long time = parseTimeToLong(timeTag);  // 自定义时间解析函数

                                    float bz = Float.parseFloat(dataRow.getString(1));
                                    float bt = Float.parseFloat(dataRow.getString(4));
                                    float bx = Float.parseFloat(dataRow.getString(2));
                                    float by = Float.parseFloat(dataRow.getString(3));
                                    float phi = Float.parseFloat(dataRow.getString(6));


                                    // 分别添加到各个列表中
                                    bzEntries.add(new Entry(time, bz));
                                    btEntries.add(new Entry(time, bt));
                                    phiEntries.add(new Entry(time, phi));
                                    bxEntries.add(new Entry(time, bx));
                                    byEntries.add(new Entry(time, by));
                                }
                                chartGenerator.generateScatterData(btEntries, bzEntries, "Bt Data", "Bz Data", new ChartGenerator.ChartCallback() {
                                    @Override
                                    public void onScatterDataGenerated(ScatterData scatterData, String aname, String bname) {
                                        if (aname.equals("Bt Data") && bname!=null && bname.equals("Bz Data"))
                                        {
                                            scatterChart1.setData(scatterData);
                                            scatterChart1.setTouchEnabled(true);
                                            scatterChart1.setDragEnabled(true);
                                            XAxis xAxis = scatterChart1.getXAxis();
                                            xAxis.setGranularity(1f);  // 每小时为单位
                                            xAxis.setValueFormatter(new ValueFormatter() {
                                                @Override
                                                public String getAxisLabel(float value, AxisBase axis) {
                                                    long millis = (long) value;  // 转换为毫秒数
                                                    return new SimpleDateFormat("MM-dd HH", Locale.getDefault()).format(millis);
                                                }
                                            });
                                            scatterChart1.invalidate();
                                            scatterChart1.notifyDataSetChanged();
                                        }
                                    }
                                    @Override
                                    public void onError(Exception e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onBarDataGenerated(BarData barData, String name){

                                    }
                                });

                                chartGenerator.generateScatterData(phiEntries, null, "Phi Data", null, new ChartGenerator.ChartCallback() {
                                    @Override
                                    public void onScatterDataGenerated(ScatterData scatterData, String aname, String bname) {
                                        if (aname.equals("Phi Data") && bname == null)
                                        {
                                            scatterChart2.setData(scatterData);
                                            scatterChart2.setTouchEnabled(true);
                                            scatterChart2.setDragEnabled(true);
                                            XAxis xAxis = scatterChart2.getXAxis();
                                            xAxis.setGranularity(1f);  // 每小时为单位
                                            xAxis.setValueFormatter(new ValueFormatter() {
                                                @Override
                                                public String getAxisLabel(float value, AxisBase axis) {
                                                    long millis = (long) value;  // 转换为毫秒数
                                                    return new SimpleDateFormat("MM-dd HH", Locale.getDefault()).format(millis);
                                                }
                                            });
                                            scatterChart2.getAxisLeft().setAxisMaximum(390);
                                            scatterChart2.getAxisLeft().setAxisMinimum(0);
                                            scatterChart2.getAxisRight().setEnabled(false);
                                            scatterChart2.invalidate();
                                            scatterChart2.notifyDataSetChanged();
                                        }
                                    }
                                    @Override
                                    public void onError(Exception e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onBarDataGenerated(BarData barData, String name){

                                    }
                                });

                                chartGenerator.generateScatterData(bxEntries, byEntries, "Bx Data", "By Data", new ChartGenerator.ChartCallback() {
                                    @Override
                                    public void onScatterDataGenerated(ScatterData scatterData, String aname, String bname) {
                                        if (aname.equals("Bx Data") && bname != null && bname.equals("By Data"))
                                        {
                                            scatterChart6.setData(scatterData);
                                            scatterChart6.setTouchEnabled(true);
                                            scatterChart6.setDragEnabled(true);
                                            XAxis xAxis = scatterChart6.getXAxis();
                                            xAxis.setGranularity(1f);  // 每小时为单位
                                            xAxis.setValueFormatter(new ValueFormatter() {
                                                @Override
                                                public String getAxisLabel(float value, AxisBase axis) {
                                                    long millis = (long) value;  // 转换为毫秒数
                                                    return new SimpleDateFormat("MM-dd HH", Locale.getDefault()).format(millis);
                                                }
                                            });
                                            scatterChart6.invalidate();
                                            scatterChart6.notifyDataSetChanged();
                                        }
                                    }
                                    @Override
                                    public void onError(Exception e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onBarDataGenerated(BarData barData, String name){

                                    }
                                });

                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        });

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    scatterChart1.setNoDataText("Load Failed");
                    scatterChart2.setNoDataText("Load Failed");
                    scatterChart6.setNoDataText("Load Failed");
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

}
