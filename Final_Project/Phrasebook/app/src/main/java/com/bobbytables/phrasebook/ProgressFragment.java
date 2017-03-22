package com.bobbytables.phrasebook;


import android.content.ContentValues;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;


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
        return rootView;
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

        //Adding the data to the chart
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        //Styling pie chart
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Total Phrases:\n" + total);
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

        //Adding the data to the chart
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        //Styling pie chart
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Total Challenges:\n" + total);
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
