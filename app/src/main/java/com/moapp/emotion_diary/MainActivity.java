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
        Calendar calendar = Calendar.getInstance();
        today_year = calendar.get(Calendar.YEAR);
        today_month = calendar.get((Calendar.MONTH)+1);
        today_date = calendar.get(Calendar.DATE);
        writeButton = findViewById(R.id.floatingActionButton2);
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

        RecyclerView recyclerView = findViewById(R.id.diaryList);

        Realm.init(this);

        Realm realm = Realm.getDefaultInstance();

        RealmResults<DiaryData> results;

        results = realm.where(DiaryData.class)
                .equalTo("year", today_year)
                .equalTo("month", today_month)
                .equalTo("date", today_date)
                .findAll()
                .sort("date", Sort.ASCENDING);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DiaryAdapter adapter = new DiaryAdapter(results, this);
        recyclerView.setAdapter(adapter);
    }


}
