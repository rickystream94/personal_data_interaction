package com.bobbytables.phrasebook;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.github.mikephil.charting.charts.PieChart;
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
        View rootView = inflater.inflate(layout, container, false);

        PieChart pieChart = (PieChart) rootView.findViewById(R.id.challengePieChart);
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(75, "Won"));
        entries.add(new PieEntry(25, "Lost"));
        PieDataSet dataSet = new PieDataSet(entries, "Challenges");
        dataSet.setValueTextSize(18f);
        //dataSet.setColor(...);
        //dataSet.setValueTextColor(...); // styling, ...
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(0, Color.GREEN);
        colors.add(1, Color.RED);
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setCenterText("Your Challenges:\n20 Won\n10 Lost");
        pieChart.setCenterTextSize(24f);
        pieChart.setEntryLabelTextSize(18f);
        pieChart.setHoleRadius(70);
        pieChart.invalidate(); //refresh
        return rootView;
    }

}
