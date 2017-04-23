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
import com.bobbytables.phrasebook.utils.DateUtil;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProgressFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private View rootView;
    private PieChart challengesPieChart;
    private PieChart phrasesPieChart;
    private BarChart activityBarChart;
    private LineChart ratioLineChart;

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
        ratioLineChart = (LineChart) rootView.findViewById(R.id.ratioChart);

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
        ratioLineChart.setData(data);

        //Setting XAxis
        IAxisValueFormatter xAxisFormatter = new DateXAxisValueFormatter(dates);
        XAxis xAxis = ratioLineChart.getXAxis();
        xAxis.setValueFormatter(xAxisFormatter);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setGranularity(1f);

        //Styling chart
        ratioLineChart.setScaleYEnabled(false);
        ratioLineChart.getAxisRight().setEnabled(false);
        ratioLineChart.getDescription().setEnabled(false);
        ratioLineChart.setDoubleTapToZoomEnabled(false);
        ratioLineChart.getLegend().setTextSize(12f);
        ratioLineChart.invalidate();
    }

    private void initActivityBarChart() {
        //Get bar chart from layout
        activityBarChart = (BarChart) rootView.findViewById(R.id.activityBarChart);

        //Add entries
        Cursor cursor = databaseHelper.getActivityStats();
        List<BarEntry> entries = new ArrayList<>();
        List<String> userDates = new ArrayList<>();
        List<String> allDates = new ArrayList<>();
        if (cursor.moveToFirst()) {
            //Get all dates of the user and all dates between these days
            int i = 0;
            do {
                userDates.add(cursor.getString(0));
                i++;
            } while (cursor.moveToNext());
            allDates = DateUtil.getDaysBetweenDates(userDates.get(0), userDates.get
                    (userDates.size() - 1));
            cursor.moveToFirst();
            i = 0;
            //Populate entries
            for (String date : allDates) {
                int frequency;
                if (userDates.contains(date)) {
                    frequency = cursor.getInt(1);
                    cursor.moveToNext();
                } else
                    frequency = 0;
                entries.add(new BarEntry(i, frequency, date));
                i++;
            }
        }
        BarDataSet dataSet = new BarDataSet(entries, "Frequency");

        //Styling dataset
        dataSet.setValueTextSize(18f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.frequencyChart));
        dataSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return value == 0 ? "" : String.valueOf((int) value);
            }
        });

        IAxisValueFormatter xAxisFormatter = new DateXAxisValueFormatter(allDates);
        XAxis xAxis = activityBarChart.getXAxis();
        xAxis.setValueFormatter(xAxisFormatter);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setGranularity(1f);

        //Adding the data to the chart
        BarData barData = new BarData(dataSet);
        activityBarChart.setData(barData);

        //Styling bar chart
        activityBarChart.setScaleYEnabled(false);
        activityBarChart.setDrawBarShadow(false);
        activityBarChart.setDrawValueAboveBar(true);
        activityBarChart.getDescription().setEnabled(false);
        activityBarChart.getAxisRight().setEnabled(false);
        activityBarChart.getAxisLeft().setGranularity(1f);
        activityBarChart.setDoubleTapToZoomEnabled(false);
        activityBarChart.getLegend().setTextSize(12f);
        activityBarChart.invalidate();
    }

    /**
     * XAxis formatter to display correctly dates on XAxis
     */
    private static class DateXAxisValueFormatter implements IAxisValueFormatter {
        private List<String> dates;
        private String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
                "Sep", "Oct", "Nov", "Dec"};

        public DateXAxisValueFormatter(List<String> dates) {
            this.dates = dates;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            int index = (int) value;
            if (dates.size() > index) {
                String date = dates.get(index);
                return formatDate(date);
            } else return null;
        }

        String formatDate(String date) {
            String[] dateParts = date.split("-");
            String year = "'" + dateParts[0].substring(2); //Get only the last two digits of the
            // year
            String intMonth = dateParts[1];
            String day = dateParts[2];
            String month;
            switch (intMonth) {
                case "01":
                    month = months[0];
                    break;
                case "02":
                    month = months[1];
                    break;
                case "03":
                    month = months[2];
                    break;
                case "04":
                    month = months[3];
                    break;
                case "05":
                    month = months[4];
                    break;
                case "06":
                    month = months[5];
                    break;
                case "07":
                    month = months[6];
                    break;
                case "08":
                    month = months[7];
                    break;
                case "09":
                    month = months[8];
                    break;
                case "10":
                    month = months[9];
                    break;
                case "11":
                    month = months[10];
                    break;
                case "12":
                    month = months[11];
                    break;
                default:
                    month = "";
            }
            return day + " " + month + " " + year;
        }
    }

    private void initPhrasesPieChart() {
        ContentValues values = databaseHelper.getPhrasesStats();
        int total = values.getAsInteger("total");
        int archived = values.getAsInteger("archived");
        int notArchived = total - archived;

        //Get pie chart from layout
        phrasesPieChart = (PieChart) rootView.findViewById(R.id.phrasesPieChart);

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
        phrasesPieChart.setData(pieData);

        setStandardPieChartStyle(phrasesPieChart, "Total Phrases:\n" + total);
    }

    private void initChallengesPieChart() {
        ContentValues values = databaseHelper.getChallengesStats();
        int total = values.getAsInteger("total");
        int won = values.getAsInteger("won");
        int lost = total - won;

        //Get pie chart from layout
        challengesPieChart = (PieChart) rootView.findViewById(R.id.challengePieChart);

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
        challengesPieChart.setData(pieData);

        setStandardPieChartStyle(challengesPieChart, "Total Challenges:\n" + total);
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

    @Override
    public void onResume() {
        super.onResume();
        if (rootView != null) {
            initChallengesPieChart();
            initPhrasesPieChart();
            initActivityBarChart();
            initChallengesRatioChart();
        }
    }
}
