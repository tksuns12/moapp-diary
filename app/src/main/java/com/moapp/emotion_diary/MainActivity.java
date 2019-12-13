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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //데이터 베이스 초기화, Realm은 앱 시작할 때 초기화를 반드시 해주어야 함.
        Realm.init(this);
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
                adapter.notifyItemRemoved(position);

                Snackbar snackbar = Snackbar.make(findViewById(R.id.diaryList), "일기가 삭제되었습니다.", Snackbar.LENGTH_LONG);
                snackbar.setAction("취소", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.restoreItem();
                        adapter.notifyItemInserted(position);
                        recyclerView.setAdapter(adapter);
                        recyclerView.scrollToPosition(position);
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
        //날짜 기준, 오름차순으로 정렬
        results = realm.where(DiaryData.class)
                .equalTo("year", today_year)
                .equalTo("month", today_month)
                .findAll()
                .sort("date", Sort.ASCENDING);
        //리사이클러뷰에 레이아웃 매니저 설정(수직 리니어 레이아웃)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //DiaryAdapter.java에서 정의해둔 어댑터 인스턴스 생성
        DiaryAdapter adapter = new DiaryAdapter(results);
        //리사이클러뷰에 어댑터 선정
        recyclerView.setAdapter(adapter);

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
}
