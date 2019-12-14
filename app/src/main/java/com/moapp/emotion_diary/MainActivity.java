package com.moapp.emotion_diary;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private FloatingActionButton writeButton;
    private TextView year_show;
    private TextView month_show;
    private Realm realm;
    private RecyclerView recyclerView;
    DiaryAdapter adapter;
    private LineChart lineChart;
    final String [] mDays = {"","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24",
            "25","26","27","28","29","30","31"};

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
        writeButton = findViewById(R.id.floatingActionButton2); //쓰기 버튼 할당
        year_show = findViewById(R.id.year);
        month_show = findViewById(R.id.month);
        year_show.setText(Integer.toString(today_year));
        month_show.setText(Integer.toString(today_month));
        //쓰기 버튼에 클릭리스너 할당
        //쓰기 버튼을 누르면 오늘 날짜를 인텐트에 담아 RWActivity로 넘겨주고 RWActivity를 불러옴
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RWActivity.class);
                intent.putExtra("year", today_year)
                        .putExtra("month", today_month)
                        .putExtra("date", today_date);
                startActivity(intent);
            }
        });
        //리사이클러뷰 할당
        recyclerView = findViewById(R.id.diaryList);
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
        //리사이클러뷰에 레이아웃 매니저 설정(수직 리니어 레이아웃)
        if (results.size() == 0) {
            TextView textView = findViewById(R.id.noDiary);
            textView.setVisibility(View.VISIBLE);
        } else {
            TextView textView = findViewById(R.id.noDiary);
            textView.setVisibility(View.GONE);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //DiaryAdapter.java에서 정의해둔 어댑터 인스턴스 생성
       adapter = new DiaryAdapter(results);
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                adapter.removeItem(position);
                RealmResults<DiaryData> results = realm.where(DiaryData.class)
                        .equalTo("year", today_year)
                        .equalTo("month", today_month)
                        .findAll()
                        .sort("date", Sort.ASCENDING);
                if (results.size() == 0) {
                    TextView textView = findViewById(R.id.noDiary);
                    textView.setVisibility(View.VISIBLE);
                } else {
                    TextView textView = findViewById(R.id.noDiary);
                    textView.setVisibility(View.GONE);
                }
                //리사이클러뷰에 레이아웃 매니저 설정(수직 리니어 레이아웃)
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                //DiaryAdapter.java에서 정의해둔 어댑터 인스턴스 생성
                adapter.updateData(results);
                recyclerView.setAdapter(adapter);

                Snackbar snackbar = Snackbar.make(findViewById(R.id.diaryList), "일기가 삭제되었습니다.", Snackbar.LENGTH_LONG);
                snackbar.setAction("취소", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.restoreItem();
                        recyclerView.setAdapter(adapter);
                        recyclerView.scrollToPosition(position);
                        TextView textView = findViewById(R.id.noDiary);
                        textView.setVisibility(View.GONE);

                    }
                });

                snackbar.setActionTextColor(Color.CYAN);
                snackbar.show();

            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        //리사이클러뷰에 어댑터 설정
        recyclerView.setAdapter(adapter);

        setChart(results);
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
        if (results.size() == 0) {
            TextView textView = findViewById(R.id.noDiary);
            textView.setVisibility(View.VISIBLE);
        } else {
            TextView textView = findViewById(R.id.noDiary);
            textView.setVisibility(View.GONE);
        }
        //리사이클러뷰에 레이아웃 매니저 설정(수직 리니어 레이아웃)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //DiaryAdapter.java에서 정의해둔 어댑터 인스턴스 생성
        adapter.updateData(results);
        //리사이클러뷰에 어댑터 선정
        recyclerView.setAdapter(adapter);
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
            //리사이클러뷰에 레이아웃 매니저 설정(수직 리니어 레이아웃)
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            //DiaryAdapter.java에서 정의해둔 어댑터 인스턴스 생성
            DiaryAdapter adapter = new DiaryAdapter(results);
            //리사이클러뷰에 어댑터 선정
            recyclerView.setAdapter(adapter);
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

        xAxis.setTextColor(Color.BLACK);
        xAxis.enableGridDashedLine(10, 24, 0);
        xAxis.setDrawLabels(true);
//        xAxis.setGranularity(1.0f);

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
