package com.shrubsink.everylifeismatter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
import java.util.ArrayList;
import java.util.List;


public class ActivityFragment extends Fragment {

    public ActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        AnyChartView anyChartView = view.findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(view.findViewById(R.id.progress_bar));

        Pie pie = AnyChart.pie();

        pie.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"x", "value"}) {
            @Override
            public void onClick(Event event) {
                Toast.makeText(requireContext(),
                        event.getData().get("x") + ":" + event.getData().get("value"),
                        Toast.LENGTH_SHORT).show();
            }
        });

        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("Posted Questions", 1));
        data.add(new ValueDataEntry("Answers", 1));
        data.add(new ValueDataEntry("Acceptations", 0));
        pie.data(data);

        pie.title("ShrubsInk Community Contribution Statistics");

        pie.labels().position("outside").padding(0d, 0d, 30d, 0d);

        pie.legend().title().enabled(true).fontFamily(String.valueOf(R.font.sf_pro_display));
        pie.legend().title()
                .text("Your activity contributions")
                .padding(0d, 0d, 16d, 0d)
        .fontFamily(String.valueOf(R.font.sf_pro_text));

        pie.legend()
                .position("center-bottom")
                .itemsLayout(LegendLayout.HORIZONTAL)
                .align(Align.CENTER)
                .fontFamily(String.valueOf(R.font.sf_pro_text));

        anyChartView.setChart(pie);

        return view;
    }

}