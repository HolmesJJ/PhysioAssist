package sg.com.nyp.a164936j.physioAssist.fragments.fragmentsphysio;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.Config;
import sg.com.nyp.a164936j.physioAssist.configuration.CustomSharedPreference;
import sg.com.nyp.a164936j.physioAssist.customadapters.CustomPhysioProgressBarGraphAdapter;
import sg.com.nyp.a164936j.physioAssist.models.Exercise;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhysioProgressBarGraph extends Fragment implements PhysioProgress.ItemClickListener, OnChartValueSelectedListener {

    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";

    private Context context;
    private ListView listView;
    private CustomPhysioProgressBarGraphAdapter mAdapter;
    private FragmentManager manager;
    private TextView graphHeader;
    private TextView chartHeader;
    private ImageView btnBack;
    private LineChart mLineChart;

    private List<Exercise> selectedPerformedExercises = new ArrayList<>();
    private List<String> dateList = new ArrayList<>();

    private String startDateTxt;
    private String endDateTxt;

    public PhysioProgressBarGraph() {
        // Required empty public constructor
    }

    public static PhysioProgressBarGraph newInstance(String startDate, String endDate) {
        PhysioProgressBarGraph fragment = new PhysioProgressBarGraph();
        Bundle bundle = new Bundle();
        bundle.putString(START_DATE, startDate);
        bundle.putString(END_DATE, endDate);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            startDateTxt = bundle.getString(START_DATE);
            endDateTxt = bundle.getString(END_DATE);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_physio_progress_bar_graph, container, false);
        initView(rootView);

        graphHeader.setText(String.format(getResources().getString(R.string.physio_schedule_bar_graph_header), startDateTxt, endDateTxt));
        chartHeader.setText("Summary");

        manager = getActivity().getSupportFragmentManager();

        btnBack.setOnClickListener((View view)-> {
            Log.d(Config.TAG_BUTTON, "(PhysioProgressBarGraph) btn back pressed");

            PhysioProgress physioProgress = new PhysioProgress();
            physioProgress.setClickListener(this);
            manager.beginTransaction()
                    .replace(R.id.fragment_container, physioProgress)
                    .addToBackStack("tag")
                    .commit();
        });

        rootView.post(()-> {
            int listViewHeight = listView.getMeasuredHeight();
            int listViewWidth = listView.getMeasuredWidth();

            selectedPerformedExercises = CustomSharedPreference.selectedPerformedExercises;

            for (int i = 0; i < selectedPerformedExercises.size(); i++) {
                Date startDate = selectedPerformedExercises.get(i).getStartTime();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);

                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int date = calendar.get(Calendar.DATE);
                String dateTxt = year + "-" + month + "-" + date;

                boolean isDateRepeat = false;
                for (int j = 0; j < dateList.size(); j++) {
                    if(dateList.get(j).equals(dateTxt)) {
                        isDateRepeat = true;
                        break;
                    }
                }

                if(!isDateRepeat) {
                    dateList.add(year + "-" + month + "-" + date);
                }
            }

            mAdapter = new CustomPhysioProgressBarGraphAdapter(context, dateList, listViewHeight, listViewWidth);
            listView.setAdapter(mAdapter);

            initChart();
        });

        return rootView;
    }

    private void initView(View rootView) {
        context = getContext();
        graphHeader = rootView.findViewById(R.id.physio_progress_bar_graph_header);
        chartHeader = rootView.findViewById(R.id.physio_progress_bar_graph_exercise);
        btnBack = rootView.findViewById(R.id.physio_progress_bar_graph_btn_back);
        listView = rootView.findViewById(R.id.physio_progress_video_listview);
        mLineChart = rootView.findViewById(R.id.line_chart);
    }

    private void initChart() {
        mLineChart.setOnChartValueSelectedListener(this);

        mLineChart.getDescription().setEnabled(false);
        mLineChart.setDrawGridBackground(false);
        mLineChart.setBackgroundColor(Color.TRANSPARENT);

        mLineChart.setDragEnabled(true);
        mLineChart.setScaleEnabled(true);
        mLineChart.setPinchZoom(true);
        mLineChart.setHighlightPerDragEnabled(true);
        mLineChart.animateX(500);

        mLineChart.setTouchEnabled(true);
        mLineChart.setDragDecelerationEnabled(true);
        mLineChart.setDragDecelerationFrictionCoef(0.9f);

        Legend l = mLineChart.getLegend();
        l.setForm(LegendForm.LINE);
        l.setTextColor(Color.BLACK);

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setTextColor(Color.RED);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setDrawLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularityEnabled(true);

        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setTextColor(Color.rgb(0, 180, 0));
        leftAxis.setTextSize(15f);
        leftAxis.setAxisMaximum(200f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);

        YAxis rightAxis = mLineChart.getAxisRight();
        rightAxis.setTextColor(Color.BLUE);
        rightAxis.setTextSize(15f);
        rightAxis.setAxisMaximum(200f);
        rightAxis.setAxisMinimum(0);
        rightAxis.setDrawGridLines(true);
        rightAxis.setGranularityEnabled(true);

        setData(5, 40);
    }

    private void setData(int count, float range) {

        ArrayList<Entry> avgAngle = new ArrayList<>();
        ArrayList<Entry> avgHoldDuration = new ArrayList<>();
        ArrayList<Entry> hideAvgAngle = new ArrayList<>();
        ArrayList<Entry> hideAvgHoldDuration = new ArrayList<>();

        avgAngle.add(new Entry(0, selectedPerformedExercises.get(0).getAvgAngle(), ""));
        avgHoldDuration.add(new Entry(0, selectedPerformedExercises.get(0).getAvgHoldDuration()));
        hideAvgAngle.add(new Entry(0, -1));
        hideAvgHoldDuration.add(new Entry(0, -1));

        String previousDateTxt = "";
        for (int i = 0; i < selectedPerformedExercises.size(); i++) {

            Date startDate = selectedPerformedExercises.get(i).getStartTime();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int date = calendar.get(Calendar.DATE);
            String dateTxt = year + "-" + month + "-" + date;

            for (int j = 0; j < dateList.size(); j++) {

                if(dateList.get(j).equals(dateTxt)) {
                    if(previousDateTxt.equals(dateTxt)) {
                        avgAngle.add(new Entry(i+1, selectedPerformedExercises.get(i).getAvgAngle(), ""));
                    }
                    else {
                        avgAngle.add(new Entry(i+1, selectedPerformedExercises.get(i).getAvgAngle(), dateTxt));
                    }

                    avgHoldDuration.add(new Entry(i+1, selectedPerformedExercises.get(i).getAvgHoldDuration()));

                    hideAvgAngle.add(new Entry(i+1, selectedPerformedExercises.get(i).getAvgAngle()));
                    hideAvgHoldDuration.add(new Entry(i+1, selectedPerformedExercises.get(i).getAvgHoldDuration()));

                    previousDateTxt = dateTxt;
                }
            }
        }

        avgAngle.add(new Entry(selectedPerformedExercises.size() + 1, selectedPerformedExercises.get(selectedPerformedExercises.size()-1).getAvgAngle(), ""));
        avgHoldDuration.add(new Entry(selectedPerformedExercises.size() + 1, selectedPerformedExercises.get(selectedPerformedExercises.size()-1).getAvgHoldDuration()));
        hideAvgAngle.add(new Entry(selectedPerformedExercises.size() + 1, -1));
        hideAvgHoldDuration.add(new Entry(selectedPerformedExercises.size() + 1, -1));

        LineDataSet avgAngleSet, avgHoldDurationSet, hideAvgAngleSet, hideAvgHoldDurationSet;

        if (mLineChart.getData() != null && mLineChart.getData().getDataSetCount() > 0) {
            avgAngleSet = (LineDataSet) mLineChart.getData().getDataSetByIndex(0);
            avgHoldDurationSet = (LineDataSet) mLineChart.getData().getDataSetByIndex(1);
            hideAvgAngleSet = (LineDataSet) mLineChart.getData().getDataSetByIndex(2);
            hideAvgHoldDurationSet = (LineDataSet) mLineChart.getData().getDataSetByIndex(3);
            avgAngleSet.setValues(avgAngle);
            avgHoldDurationSet.setValues(avgHoldDuration);
            hideAvgAngleSet.setValues(hideAvgAngle);
            hideAvgHoldDurationSet.setValues(hideAvgHoldDuration);
            mLineChart.getData().notifyDataChanged();
            mLineChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            avgAngleSet = new LineDataSet(avgAngle, "Average Angle");
            avgAngleSet.setAxisDependency(AxisDependency.LEFT);
            avgAngleSet.setColor(Color.rgb(0, 180, 0));
            avgAngleSet.setLineWidth(3f);
            avgAngleSet.setCircleRadius(4f);
            avgAngleSet.setDrawCircles(false);
            avgAngleSet.setDrawValues(false);

            // create a dataset and give it a type
            avgHoldDurationSet = new LineDataSet(avgHoldDuration, "Average Hold Duration");
            avgHoldDurationSet.setAxisDependency(AxisDependency.RIGHT);
            avgHoldDurationSet.setColor(Color.BLUE);
            avgHoldDurationSet.setLineWidth(3f);
            avgHoldDurationSet.setCircleRadius(4f);
            avgHoldDurationSet.setDrawCircles(false);
            avgHoldDurationSet.setDrawValues(false);

            // create a dataset and give it a type
            hideAvgAngleSet = new LineDataSet(hideAvgAngle, "");
            hideAvgAngleSet.setAxisDependency(AxisDependency.LEFT);
            hideAvgAngleSet.setColor(Color.TRANSPARENT);
            hideAvgAngleSet.setCircleColor(Color.rgb(0, 180, 0));
            hideAvgAngleSet.setCircleHoleColor(Color.WHITE);
            hideAvgAngleSet.setValueTextColor(Color.rgb(0, 180, 0));
            hideAvgAngleSet.setValueTextSize(12f);

            // create a dataset and give it a type
            hideAvgHoldDurationSet = new LineDataSet(hideAvgHoldDuration, "");
            hideAvgHoldDurationSet.setAxisDependency(AxisDependency.LEFT);
            hideAvgHoldDurationSet.setColor(Color.TRANSPARENT);
            hideAvgHoldDurationSet.setCircleColor(Color.BLUE);
            hideAvgHoldDurationSet.setCircleHoleColor(Color.WHITE);
            hideAvgHoldDurationSet.setValueTextColor(Color.BLUE);
            hideAvgHoldDurationSet.setValueTextSize(12f);

            // create a data object with the data sets
            LineData data = new LineData(avgAngleSet, avgHoldDurationSet, hideAvgAngleSet, hideAvgHoldDurationSet);

            // set data
            mLineChart.setData(data);
        }

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setValueFormatter((value, axis) -> String.valueOf(avgAngle.get((int)value).getData()));

        // let the chart know it's data has changed
        mLineChart.notifyDataSetChanged();

        // limit the number of visible entries
        mLineChart.setVisibleXRangeMaximum(6);
    }

    @Override
    public void onItemClick(String startDate, String endDate) {
        manager.beginTransaction()
                .replace(R.id.fragment_container, PhysioProgressBarGraph.newInstance(startDate, endDate))
                .addToBackStack("tag")
                .commit();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
