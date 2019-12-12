package com.moapp.emotion_diary;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;


public class GraphAxisValueFormatter implements IAxisValueFormatter {
    private String[] mValues;
    GraphAxisValueFormatter(String[] values){
        this.mValues = values;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis){
        return mValues[(int) value];
    }
}