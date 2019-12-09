package com.moapp.emotion_diary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {
    private int today_year;
    private int today_month;
    private int today_date;
    private FloatingActionButton writeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Calendar calendar = Calendar.getInstance(); // 오늘 날짜 가져오기 위해 캘린더 인스턴스 생성
        today_year = calendar.get(Calendar.YEAR); // 오늘 연도 가져오기
        today_month = calendar.get((Calendar.MONTH)+1); //오늘 월 가져오기
        today_date = calendar.get(Calendar.DATE); // 오늘 일 가져오기
        writeButton = findViewById(R.id.floatingActionButton2); //쓰기 버튼 할당
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
        RecyclerView recyclerView = findViewById(R.id.diaryList);
        //데이터베이스 초기화
        Realm.init(this);
        //데이터베이스 인스턴스 가져오기
        Realm realm = Realm.getDefaultInstance();
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
        DiaryAdapter adapter = new DiaryAdapter(results);
        //리사이클러뷰에 어댑터 선정
        recyclerView.setAdapter(adapter);
    }


}
