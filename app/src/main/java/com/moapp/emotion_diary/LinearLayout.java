package com.moapp.emotion_diary;


import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import org.w3c.dom.Text;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class LinearLayout extends Fragment  {

    public LinearLayout() {
        // Required empty public constructor
    }
    private DiaryData diaryData;
    private LineChart lineChart ;
    final String [] mDays = {"","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24",
            "25","26","27","28","29","30","31"};

    //x축 날짜 표시
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_linear_layout,container,false);
        lineChart = (LineChart) view.findViewById(R.id.chart);

        TextView textView = (TextView) getActivity().findViewById(R.id.month); //getActivity 읽어오기


        ArrayList<Entry> entries = new ArrayList<>();//x축 데이터
        entries.add(new Entry(1, 2));  //좌표값 x축 날짜 y축 기분점수
        entries.add(new Entry(2, 3));
        entries.add(new Entry(3, 1));
        entries.add(new Entry(4, 2));
        entries.add(new Entry(5, 6));
        entries.add(new Entry(6, 8));


//       for(int i=1; i<6;i++) {
//           entries.add(new Entry(i, 2*i));  //좌표값 x축 날짜 y축 기분점수
//       };

        LineDataSet lineDataSet = new LineDataSet(entries, "기분점수"); //속성
        lineDataSet.setLineWidth(2);
        lineDataSet.setCircleRadius(4);
        lineDataSet.setCircleColor(Color.BLUE);  //데이터 원 색상
        lineDataSet.setColor(Color.parseColor("#A42196F3")); //데이터 선 색상
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawHorizontalHighlightIndicator(true);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); //선 둥글게 표시
        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setDrawValues(true);
        lineDataSet.setDrawFilled(true); //그래프 밑 부분 채우기 유무

        LineData lineData = new LineData();
        lineData.addDataSet(lineDataSet);
        lineChart.setData(lineData);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setValueFormatter(new GraphAxisValueFormatter(mDays));// x축 설명
        xAxis.setTextColor(Color.BLACK);
        xAxis.enableGridDashedLine(10, 24, 0);
        xAxis.setDrawLabels(true);
        xAxis.setGranularity(1.0f);


        YAxis yLAxis = lineChart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);

        YAxis yRAxis = lineChart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);

        Description description = new Description();
        description.setText("");

        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDescription(description);
        lineChart.animateY(2000);
        lineChart.invalidate();

        return view;
    }

}
