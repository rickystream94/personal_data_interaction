package com.bobbytables.phrasebook;


import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.data;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProgressFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        databaseHelper = DatabaseHelper.getInstance(getContext());
        int layout;
        // Inflate the layout for this fragment
        if (databaseHelper.isDatabaseEmpty()) {
            layout = R.layout.empty_database;
            return inflater.inflate(layout, container, false);
        }
        layout = R.layout.fragment_progress;
        rootView = inflater.inflate(layout, container, false);

        //Initialize charts
        initChallengesPieChart();
        initPhrasesPieChart();
        initActivityBarChart();
        initChallengesRatioChart();
        return rootView;
    }

    private void initChallengesRatioChart() {
        //Get chart
        LineChart lineChart = (LineChart) rootView.findViewById(R.id.ratioChart);

        //Retrieve and set entries
        Cursor cursor = databaseHelper.getChallengesRatio();
        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                entries.add(new Entry(i, cursor.getFloat(cursor.getColumnIndex("RATIO"))));
                dates.add(cursor.getString(cursor.getColumnIndex("DATE")));
                i++;
            } while (cursor.moveToNext());
        }

        //Setting the data to the line chart
        LineDataSet dataSet = new LineDataSet(entries, "Correctness Ratio");

        //Styling dataset
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.ratioLine));
        dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.ratioCircle));
        dataSet.setDrawValues(false);

        //Adding data to the chart
        LineData data = new LineData(dataSet);
        lineChart.setData(data);

        //Styling chart
        lineChart.setScaleYEnabled(false);
        lineChart.getXAxis().setValueFormatter(new DateXAxisValueFormatter(dates));
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        //lineChart.getXAxis().setGranularity(1f); //TODO: it doesn't work for this chart! Why?
        lineChart.getAxisRight().setEnabled(false);
        Description description = new Description();
        description.setText("");
        lineChart.setDescription(description);
        Legend legend = lineChart.getLegend();
        legend.setTextSize(12f);
        ;
        lineChart.invalidate();
    }

    private void initActivityBarChart() {
        //Get bar chart from layout
        BarChart barChart = (BarChart) rootView.findViewById(R.id.activityBarChart);

        //Add entries
        Cursor cursor = databaseHelper.getActivityStats();
        List<BarEntry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                entries.add(new BarEntry(i, cursor.getInt(1), cursor.getString(0)));
                dates.add(cursor.getString(0));
                i++;
            } while (cursor.moveToNext());
        }
        BarDataSet dataSet = new BarDataSet(entries, "Frequency");

        //Styling dataset
        dataSet.setValueTextSize(18f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.frequencyChart));
        dataSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return String.valueOf((int) value);
            }
        });

        //Adding the data to the chart
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        //Styling bar chart
        barChart.setScaleYEnabled(false);
        barChart.getXAxis().setValueFormatter(new DateXAxisValueFormatter(dates));
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setGranularity(1f);
        barChart.getXAxis().setGranularity(1f); //TODO: eventually remove if causes problems
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);
        Legend legend = barChart.getLegend();
        legend.setTextSize(12f);
        barChart.invalidate();
    }

    /**
     * XAxis formatter to display correctly dates on XAxis
     */
    private static class DateXAxisValueFormatter implements IAxisValueFormatter {
        private List<String> dates;

        public DateXAxisValueFormatter(List<String> dates) {
            this.dates = dates;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            int index = (int) value;
            return dates.get(index);
        }
    }

    private void initPhrasesPieChart() {
        ContentValues values = databaseHelper.getPhrasesStats();
        int total = values.getAsInteger("total");
        int archived = values.getAsInteger("archived");
        int notArchived = total - archived;

        //Get pie chart from layout
        PieChart pieChart = (PieChart) rootView.findViewById(R.id.phrasesPieChart);

        //Add entries
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(archived, "Learnt"));
        entries.add(new PieEntry(notArchived, "To Study"));
        PieDataSet dataSet = new PieDataSet(entries, "Phrases");

        //Styling dataset
        dataSet.setValueTextSize(18f);
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(0, ContextCompat.getColor(getContext(), R.color.pieChartArchived));
        colors.add(1, ContextCompat.getColor(getContext(), R.color.pieChartToStudy));
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setSliceSpace(3f);
        dataSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return ((int) value) + "";
            }
        });

        //Adding the data to the chart
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        setStandardPieChartStyle(pieChart, "Total Phrases:\n" + total);
    }

    private void initChallengesPieChart() {
        ContentValues values = databaseHelper.getChallengesStats();
        int total = values.getAsInteger("total");
        int won = values.getAsInteger("won");
        int lost = total - won;

        //Get pie chart from layout
        PieChart pieChart = (PieChart) rootView.findViewById(R.id.challengePieChart);

        //Add entries
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(won, "Won"));
        entries.add(new PieEntry(lost, "Lost"));
        PieDataSet dataSet = new PieDataSet(entries, "Challenges");

        //Styling dataset
        dataSet.setValueTextSize(18f);
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(0, ContextCompat.getColor(getContext(), R.color.pieChartWon));
        colors.add(1, ContextCompat.getColor(getContext(), R.color.pieChartLost));
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setSliceSpace(3f);
        dataSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return ((int) value) + "";
            }
        });

        //Adding the data to the chart
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        setStandardPieChartStyle(pieChart, "Total Challenges:\n" + total);
    }

    private void setStandardPieChartStyle(PieChart pieChart, String centerText) {
        //Styling pie chart
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText(centerText);
        pieChart.setCenterTextSize(18f);
        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);
        pieChart.setEntryLabelTextSize(18f);
        pieChart.setHoleRadius(50);
        Legend legend = pieChart.getLegend();
        legend.setTextSize(12f);
        pieChart.invalidate(); //refresh
    }

}
