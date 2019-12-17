package com.moapp.emotion_diary;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {
    private int today_year;
    private int today_month;
    private int today_date;
    private TextView year_show;
    private TextView month_show;
    private Realm realm;
    private ListView listView;
    NewDiaryAdapter adapter;
    private long mBackPressed;
    private Toast closeToast;
    private ImageButton add_diary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.init(this);
        //데이터 베이스 초기화, Realm은 앱 시작할 때 초기화를 반드시 해주어야 함.
        //MainActivity 레이아웃 뷰 설정 전에 로딩 화면을 띄움
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);
        setContentView(R.layout.activity_main);
        Calendar calendar = Calendar.getInstance(); // 오늘 날짜 가져오기 위해 캘린더 인스턴스 생성
        today_year = calendar.get(Calendar.YEAR); // 오늘 연도 가져오기
        today_month = calendar.get(Calendar.MONTH) + 1; //오늘 월 가져오기
        today_date = calendar.get(Calendar.DATE); // 오늘 일 가져오기
        year_show = findViewById(R.id.year);
        year_show.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Binggrae.ttf"));
        month_show = findViewById(R.id.month);
        month_show.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Binggrae.ttf"));
        year_show.setText(Integer.toString(today_year));
        month_show.setText(Integer.toString(today_month));
        //쓰기 버튼에 클릭리스너 할당
        //쓰기 버튼을 누르면 오늘 날짜를 인텐트에 담아 RWActivity로 넘겨주고 RWActivity를 불러옴

        //리사이클러뷰 할당
        listView = findViewById(R.id.diaryList);
        //데이터베이스 인스턴스 가져오기
        realm = Realm.getDefaultInstance();
        //탐색 결과 초기화
        RealmResults<DiaryData> results;
        //오늘 연,월에 해당하는 모든 데이터를 찾아
        //날짜 기준, 오름차순으로 정렬
        results = realm.where(DiaryData.class)
                .equalTo("year", today_year)
                .equalTo("month", today_month)
                .findAll()
                .sort("date", Sort.ASCENDING);

        adapter = new NewDiaryAdapter(results, today_year, today_month, today_date);

        listView.setAdapter(adapter);
        listView.setSelection(adapter.getCount() - 1);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DiaryData item = (DiaryData) adapterView.getItemAtPosition(i);
                if (i != ListView.NO_ID) {
                    Intent intent = new Intent(getApplicationContext(), RWActivity.class);
                    intent.putExtra("year", today_year)
                            .putExtra("month", today_month)
                            .putExtra("date", i+1)
                            .putExtra("content", item.getContent());
                    startActivity(intent);
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int position = i;
                DiaryData temp = (DiaryData) adapterView.getItemAtPosition(position);
                if (temp.getContent() != null) {
                    new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle)
                            .setTitle("삭제")
                            .setMessage("삭제하시려면 예를 누르세요.")
                            .setPositiveButton("삭제",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            adapter.removeItem(position);
                                            RealmResults<DiaryData> results = realm.where(DiaryData.class)
                                                    .equalTo("year", today_year)
                                                    .equalTo("month", today_month)
                                                    .findAll()
                                                    .sort("date", Sort.ASCENDING);

                                            adapter.updateData(results);
                                            listView.setAdapter(adapter);
                                            listView.setSelection(adapter.getCount() - 1);
                                            setChart(results);

                                            Snackbar snackbar = Snackbar.make(findViewById(R.id.diaryList), "일기가 삭제되었습니다.", Snackbar.LENGTH_LONG);
                                            snackbar.setAction("취소", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    RealmResults<DiaryData> results = realm.where(DiaryData.class)
                                                            .equalTo("year", today_year)
                                                            .equalTo("month", today_month)
                                                            .findAll()
                                                            .sort("date", Sort.ASCENDING);
                                                    adapter.restoreItem();
                                                    listView.setAdapter(adapter);
                                                    listView.setSelection(position);

                                                    setChart(results);
                                                }
                                            });

                                            snackbar.setActionTextColor(Color.CYAN);
                                            snackbar.show();
                                        }
                                    })
                            .setNegativeButton("취소", null)
                            .show();
                }
                return true;
            }
        });
        setChart(results);


    }

    @Override
    public void onBackPressed() {
        closeToast = Toast.makeText(this, "종료하려면 한 번 더 누르세요.", Toast.LENGTH_SHORT);
        if (mBackPressed + 2000 > System.currentTimeMillis()) {
            closeToast.cancel();
            super.onBackPressed();
        } else {
            closeToast.show();
        }
        mBackPressed = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    //저장 버튼이나 삭제 버튼을 누르고 다시 MainActivity로 돌아왔을 때 할 동작 설정
    @Override
    protected void onResume() {
        super.onResume();
        RealmResults<DiaryData> results;
        //오늘 연,월에 해당하는 모든 데이터를 찾아
//        //날짜 기준, 오름차순으로 정렬
        results = realm.where(DiaryData.class)
                .equalTo("year", today_year)
                .equalTo("month", today_month)
                .findAll()
                .sort("date", Sort.ASCENDING);


        adapter.updateData(results);
        //리사이클러뷰에 어댑터 선정
        listView.setAdapter(adapter);
        listView.setSelection(adapter.getCount() - 1);
        setChart(results);
    }

    public void clickDatePickerMain(View view) {
        //날짜를 클릭할 시 캘린더 대화창 띄움
        DatePickerDialog dialog = new DatePickerDialog(this, AlertDialog.THEME_HOLO_DARK, listener
                , today_year
                , today_month-1
                , 1);
        //연, 월만 선택하면 되기 때문에 일은 안 보이도록 설정함.
        dialog.getDatePicker().findViewById(Resources.getSystem()
                .getIdentifier("day", "id", "android")).setVisibility(View.GONE);
        dialog.show();
    }

    //MainActivity 상단에 연,월을 클릭하는 동작을 감지하는 리스너 설정
    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            year_show.setText(Integer.toString(year));
            month_show.setText(Integer.toString(month+1));
            today_year = year;
            today_month = month+1;
            //오늘 연,월에 해당하는 모든 데이터를 찾아
            //날짜 기준, 오름차순으로 정렬
            RealmResults<DiaryData> results;
            results = realm.where(DiaryData.class)
                    .equalTo("year", today_year)
                    .equalTo("month", today_month)
                    .findAll()
                    .sort("date", Sort.ASCENDING);
            NewDiaryAdapter adapter = new NewDiaryAdapter(results, today_year, today_month, today_date);
            //리사이클러뷰에 어댑터 선정
            listView.setAdapter(adapter);
            listView.setSelection(adapter.getCount() - 1);
            setChart(results);
        }
    };

    private void setChart(List<DiaryData> data) {
        LineChart lineChart = findViewById(R.id.chart);
        lineChart.invalidate();
        lineChart.clear();

        List<DiaryData> contentlist = new ArrayList<>();
        contentlist.addAll(realm.copyFromRealm(data));
        ArrayList<Entry> values = new ArrayList<>();
        if (contentlist.size() != 0) {
            for (int day = 1, i = 0; day < 32; day++) {
                if (i == contentlist.size()) {
                    values.add(new Entry(day, 0));
                } else {
                    if (contentlist.get(i).getDate() == day) {
                        values.add(new Entry(day, contentlist.get(i).getEmotion()));
                        i++;
                } else {
                        values.add(new Entry(day, 0));
                    }

                }
            }
    }

        LineDataSet lineDataSet = new LineDataSet(values, "기분 점수");
        lineDataSet.setLineWidth(2);
        lineDataSet.setCircleRadius(4);
        lineDataSet.setCircleColor(Color.BLUE);  //데이터 원 색상
        lineDataSet.setColor(Color.parseColor("#F7CA5C25")); //데이터 선 색상
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawHorizontalHighlightIndicator(true);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); //선 둥글게 표시
        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawFilled(false); //그래프 밑 부분 채우기 유무

        LineData lineData = new LineData();
        lineData.addDataSet(lineDataSet);

        lineChart.setData(lineData);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

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

}
}
