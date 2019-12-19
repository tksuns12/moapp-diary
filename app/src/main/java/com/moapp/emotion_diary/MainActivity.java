package com.moapp.emotion_diary;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.util.Calendar;
import android.media.Image;
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
    private final String[] month_names = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
    private Calendar calendar;
    private ImageButton rightclick;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 메인 액티비티 레이아웃 붙이기
        Realm.init(this); // DB 초기화
        //메인 화면 세팅하는 스레드
        mHandler = new Handler();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //오늘 연,월에 해당하는 모든 데이터를 찾아
                        //날짜 기준, 오름차순으로 정렬
                        RealmResults<DiaryData> results;
                        results = realm.where(DiaryData.class)
                                .equalTo("year", today_year)
                                .equalTo("month", today_month)
                                .findAll()
                                .sort("date", Sort.ASCENDING);

                        adapter.updateData(results, today_year, today_month);
                        listView.setAdapter(adapter);
                        listView.setSelection(adapter.getCount() - 1);
                        month_show.setText(month_names[today_month-1]);
                        year_show.setText(Integer.toString(today_year));

                        if (today_year == calendar.get(Calendar.YEAR) && today_month == calendar.get(Calendar.MONTH) + 1) {
                            rightclick.setEnabled(false);
                            rightclick.setVisibility(View.INVISIBLE);
                        } else {
                            rightclick.setVisibility(View.VISIBLE);
                            rightclick.setEnabled(true);
                        }

                        setChart(results);
                    }
                });
            }
        });
        calendar = Calendar.getInstance(); // 오늘 날짜 가져오기 위해 캘린더 인스턴스 생성
        today_year = calendar.get(Calendar.YEAR); // 오늘 연도 가져오기
        today_month = calendar.get(Calendar.MONTH) + 1; //오늘 월 가져오기
        today_date = calendar.get(Calendar.DATE); // 오늘 일 가져오기
        year_show = findViewById(R.id.year); // 년도 표시 뷰
        year_show.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Binggrae.ttf")); // 년도 글꼴 설정
        month_show = findViewById(R.id.month); // 월 표시 뷰
        month_show.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Dense-Regular.otf")); // 월 글꼴 설정
        year_show.setText(Integer.toString(today_year)); // 년도 설정
        month_show.setText(month_names[today_month-1]); // 월 설정
        rightclick = findViewById(R.id.rightclick); // 오른쪽 화살표 버튼
        listView = findViewById(R.id.diaryList); // 리스트뷰 불러오기
        realm = Realm.getDefaultInstance(); // 데이터베이스 인스턴스 가져오기
        RealmResults<DiaryData> results; //탐색 결과 초기화
        //오늘 연,월에 해당하는 모든 데이터를 찾아
        //날짜 기준, 오름차순으로 정렬
        results = realm.where(DiaryData.class)
                .equalTo("year", today_year)
                .equalTo("month", today_month)
                .findAll()
                .sort("date", Sort.ASCENDING);
        //데이터 검색 끝
        adapter = new NewDiaryAdapter(results, today_year, today_month, today_date); // 어댑터 생성
        listView.setAdapter(adapter); // 리스트뷰에 어댑터 붙이기
        setChart(results); // 그래프 그리기
        // 지금 설정된 연,월이 현재의 연,월이면 오른쪽 화살표 버튼을 안 보이게 함.
        if (today_year == calendar.get(Calendar.YEAR) && today_month == calendar.get(Calendar.MONTH) + 1) {
            ImageButton rightclick = findViewById(R.id.rightclick);
            rightclick.setVisibility(View.INVISIBLE);
            rightclick.setEnabled(false);
        } else {
            ImageButton rightclick = findViewById(R.id.rightclick);
            rightclick.setVisibility(View.VISIBLE);
            rightclick.setEnabled(true);
        }
        listView.setSelection(adapter.getCount() - 1); // 리스트뷰를 가장 아래로 스크롤
        // 리스트뷰에 onClick 리스너 설정
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
        // 리스트뷰에 onLongClick 리스너 설정
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
                                            setMainThread();

                                            Snackbar snackbar = Snackbar.make(findViewById(R.id.diaryList), "일기가 삭제되었습니다.", Snackbar.LENGTH_LONG);
                                            snackbar.setAction("취소", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    adapter.restoreItem();
                                                    setMainThread();
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

//        //리스트뷰에 스와이프리스너 설정
//        listView.setOnTouchListener(new OnSwipeTouchListener(this) {
//            @Override
//            public void onSwipeLeft() {
//                super.onSwipeLeft();
//                if (today_month == 12) {
//                    if (today_year < calendar.get(Calendar.YEAR)) {
//                        today_month = 1;
//                        today_year += 1;
//                        setMainThread();
//                    }
//                } else {
//                    today_month += 1;
//                    setMainThread();
//                }
//
//            }
//
//            @Override
//            public void onSwipeRight() {
//                super.onSwipeRight();
//                if (today_month == 1){
//                    today_month = 12;
//                    today_year -= 1;
//                } else {
//                today_month -= 1;}
//                setMainThread();
//            }
//        });

        t.start();
    }

    // 뒤로가기 눌렀을 때 바로 종료되지 않고 토스트 메세지 띄움
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

    // 프로그램이 닫힐 때 DB를 종료
    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    //저장 버튼이나 삭제 버튼을 누르고 다시 MainActivity로 돌아왔을 때 할 동작 설정
    @Override
    protected void onResume() {
        super.onResume();
        setMainThread();
    }


    public void clickDatePickerMain(View view) {
        //날짜를 클릭할 시 캘린더 대화창 띄움
        DatePickerDialog dialog = new DatePickerDialog(this, AlertDialog.THEME_HOLO_LIGHT, listener
                , today_year
                , today_month-1
                , 1);
        //연, 월만 선택하면 되기 때문에 일은 안 보이도록 설정함.
        dialog.getDatePicker().findViewById(Resources.getSystem()
                .getIdentifier("day", "id", "android")).setVisibility(View.GONE);
        dialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
        dialog.show();
    }

    //MainActivity 상단에 연,월을 클릭하는 동작을 감지하는 리스너 설정
    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            year_show.setText(Integer.toString(year));
            month_show.setText(month_names[month]);
            today_year = year;
            today_month = month+1;
            setMainThread();
        }
    };

    private void setChart(List<DiaryData> data) {
        LineChart lineChart = findViewById(R.id.chart);
        lineChart.invalidate();
        lineChart.clear();

        List<DiaryData> contentlist = new ArrayList<>(realm.copyFromRealm(data));
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

    // 메인 액티비티의 모든 뷰를 종합적으로 업데이트 하는 함수
    public void setMain() {
        //오늘 연,월에 해당하는 모든 데이터를 찾아
        //날짜 기준, 오름차순으로 정렬
        RealmResults<DiaryData> results;
        results = realm.where(DiaryData.class)
                .equalTo("year", today_year)
                .equalTo("month", today_month)
                .findAll()
                .sort("date", Sort.ASCENDING);

        adapter.updateData(results, today_year, today_month);
        listView.setAdapter(adapter);
        listView.setSelection(adapter.getCount() - 1);
        month_show.setText(month_names[today_month-1]);
        year_show.setText(Integer.toString(today_year));

        if (today_year == calendar.get(Calendar.YEAR) && today_month == calendar.get(Calendar.MONTH) + 1) {
            rightclick.setEnabled(false);
            rightclick.setVisibility(View.INVISIBLE);
        } else {
            rightclick.setVisibility(View.VISIBLE);
            rightclick.setEnabled(true);
        }

        setChart(results);

    }

    public void setMainThread(){


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //오늘 연,월에 해당하는 모든 데이터를 찾아
                        //날짜 기준, 오름차순으로 정렬
                        RealmResults<DiaryData> results;
                        results = realm.where(DiaryData.class)
                                .equalTo("year", today_year)
                                .equalTo("month", today_month)
                                .findAll()
                                .sort("date", Sort.ASCENDING);

                        adapter.updateData(results, today_year, today_month);
                        listView.setAdapter(adapter);
                        listView.setSelection(adapter.getCount() - 1);
                        month_show.setText(month_names[today_month-1]);
                        year_show.setText(Integer.toString(today_year));

                        if (today_year == calendar.get(Calendar.YEAR) && today_month == calendar.get(Calendar.MONTH) + 1) {
                            rightclick.setEnabled(false);
                            rightclick.setVisibility(View.INVISIBLE);
                        } else {
                            rightclick.setVisibility(View.VISIBLE);
                            rightclick.setEnabled(true);
                        }

                        setChart(results);
                    }
                });
            }
        });
        t.start();
    }

    // 연월 표시 좌우 버튼을 누를 시 동작
    public void moveMonth(View view) {
        if (view.getId() == R.id.leftclick) {
            if(today_month == 1) {
                today_month = 12;
                today_year -= 1;
            } else{today_month -= 1;}
            setMainThread();
        } else {
            if (today_month == 12) {
                today_month = 1;
                today_year += 1;
            } else {today_month += 1;}
            setMainThread();
        }
    }
}
