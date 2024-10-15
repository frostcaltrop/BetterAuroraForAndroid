package com.example.betteraurora.util;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChartGenerator {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // 单线程确保线程安全
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper()); // 主线程Handler
    private final Context context;

    public ChartGenerator(Context context){
        this.context = context;
    }


    public void generateScatterData(@NonNull List<Entry> aEntries,  List<Entry> bEntries, @NonNull String aNname, String bName,
                                    @NonNull ChartCallback callback) {
        // 将绘制任务提交到线程池
        executorService.submit(() -> {
            try {
                ScatterData scatterData = makeScatterData(aEntries, bEntries, aNname, bName);
                mainThreadHandler.post(() -> callback.onScatterDataGenerated(scatterData, aNname, bName));

            } catch (Exception e) {
                e.printStackTrace();
                mainThreadHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void generateBarData(@NonNull List<BarEntry> entries, @NonNull String name,
                                @NonNull ChartCallback callback){
        executorService.submit(()->{
          try {
              BarData barData = makeBarData(entries, name);
              mainThreadHandler.post(()->callback.onBarDataGenerated(barData, name));
          }
          catch (Exception e){
              e.printStackTrace();
              mainThreadHandler.post(()->callback.onError(e));
          }
        });
    }

    private BarData makeBarData(List<BarEntry> entries, String name) {
        BarDataSet dataSet = new BarDataSet(entries, name);

        List<Integer> colors = new ArrayList<>();

        for (BarEntry entry : entries) {
            float yValue = entry.getY();

            // 根据 y 值设置颜色
            if (yValue < 5) {
                colors.add(Color.GREEN);
            } else if (yValue >= 5 && yValue < 6) {
                colors.add(Color.CYAN);
            } else if (yValue >= 6 && yValue < 7) {
                colors.add(Color.YELLOW);
            } else if (yValue >= 7 && yValue < 8) {
                colors.add(Color.parseColor("#FFA500"));
            } else if (yValue >= 8 && yValue < 9) {
                colors.add(Color.parseColor("#FF4500"));
            } else if (yValue >= 9) {
                colors.add(Color.RED);
            }
        }

        dataSet.setColors(colors);

        return new BarData(dataSet);
    }

//    int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
//        scatterChart.setLayoutParams(new ScatterChart.LayoutParams(screenWidth, 3*screenWidth/4));  // 设置图表大小
//            scatterChart.setData(scatterData);

    private ScatterData makeScatterData(List<Entry> aEntries, List<Entry> bEntries, String aNname, String bName) {

        ScatterDataSet aDataSet = new ScatterDataSet(aEntries, aNname);
        aDataSet.setColor(Color.BLUE);
        aDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        aDataSet.setScatterShapeSize(2f);
        ScatterDataSet bDataSet = null;
        ScatterData scatterData;
        if (bEntries != null){
            bDataSet = new ScatterDataSet(bEntries, bName);
            bDataSet.setColor(Color.RED);
            bDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
            bDataSet.setScatterShapeSize(2f);
            scatterData = new ScatterData(aDataSet, bDataSet);
        }
        else
            scatterData = new ScatterData(aDataSet);

        return scatterData;

    }

    // 定义一个回调接口，用于返回Bitmap
    public interface ChartCallback {
        void onScatterDataGenerated(ScatterData scatterData,String aname, String bname);
        //void on
        void onError(Exception e);

        void onBarDataGenerated(BarData barData, String name);
    }
}
