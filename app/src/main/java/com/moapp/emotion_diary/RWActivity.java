package com.moapp.emotion_diary;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class RWActivity extends AppCompatActivity {

    Realm realm;
    private TextView textView;
    private String mYear;
    private String mMonth;
    private String mDate;
    private EditText editText;
    private MenuItem click;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rw);
        Toolbar tb = findViewById(R.id.toolbar2);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        textView = findViewById(R.id.date_picker);
        Calendar calendar = Calendar.getInstance();
        // 오늘 연,월,일 불러옴
        int today_year = calendar.get(Calendar.YEAR);
        int today_month = calendar.get(Calendar.MONTH) + 1;
        int today_date = calendar.get(Calendar.DATE);
        realm = Realm.getDefaultInstance();
        //인텐트 불러옴
        Intent intent = getIntent();
        //연월일을 인텐트에서 불러온대로 설정, 없으면 오늘 연월일을 기본값으로 설정
        mYear = Integer.toString(intent.getIntExtra("year", today_year));
        mMonth = Integer.toString(intent.getIntExtra("month", today_month));
        mDate = Integer.toString(intent.getIntExtra("date", today_date));
        editText = findViewById(R.id.content_view);
        editText.setText(intent.getStringExtra("content"));
        textView.setText(mYear + "년 " + mMonth + "월 " + mDate + "일");


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (editText.getText().toString().length() == 0) {
                    click.setEnabled(false);
                    click.setIcon(ContextCompat.getDrawable(getApplicationContext()
                            , R.drawable.ic_action_savebutton_disabled));
                } else {
                    click.setEnabled(true);
                    click.setIcon(ContextCompat.getDrawable(getApplicationContext(),
                            R.drawable.ic_action_savebutton));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        click = menu.findItem(R.id.saveButton);
        click.setEnabled(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void clickSave(MenuItem item) {
        editText = findViewById(R.id.content_view);
        //저장 버튼 누를 시 새로운 DiaryData 타입의 데이터를 만든 후 연월일, 일기 내용 설정 후 데이터베이스에 저장
        if(realm.where(DiaryData.class)
                .equalTo("uniqueKey", Integer.parseInt(mYear+mMonth+mDate)).findAll().size() == 0){
            realm.beginTransaction();
            DiaryData diaryData = realm.createObject(DiaryData.class, Integer.parseInt(mYear + mMonth + mDate));
            diaryData.setContent(editText.getText().toString());
            diaryData.setDate(Integer.parseInt(mDate));
            diaryData.setYear(Integer.parseInt(mYear));
            diaryData.setMonth(Integer.parseInt(mMonth));
            diaryData.setEmotion(scoreEmotion(editText.getText().toString()));
            realm.commitTransaction();
        } else{
            realm.beginTransaction();
            DiaryData result = realm.where(DiaryData.class)
                    .equalTo("uniqueKey", Integer.parseInt(mYear+mMonth+mDate)).findFirst();
            result.setContent(editText.getText().toString());
            result.setEmotion(scoreEmotion(editText.getText().toString()));
            realm.commitTransaction();
        }
        finish();
    }

    public void clickDatePicker(MenuItem item) {
        //날짜를 클릭할 시 캘린더 대화창 띄움
        DatePickerDialog dialog = new DatePickerDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT, listener
                , Integer.parseInt(mYear)
                , Integer.parseInt(mMonth)-1
                , Integer.parseInt(mDate));
        dialog.show();
    }
    //날짜가 바뀔 시 연월일을 바뀐대로 설정, 그러나 잘 안 되는 것 같음.
    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mYear = Integer.toString(year);
            mMonth = Integer.toString(month + 1);
            mDate = Integer.toString(dayOfMonth);
            textView.setText(year + "년 " + (month + 1) + "월 " + dayOfMonth + "일");
        }
    };


    //감정 점수를 매기는 함수
    private int scoreEmotion(String data) {
        int emotion_count = 0;
        int total_word = 0;
        String[] split = data.split(" ");
        List<String> onegram = new ArrayList<>();
        List<String> twogram = new ArrayList<>();
        List<String> threegram = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            onegram.add(split[i].replace(".", ""));
            total_word++;
            if (i+1 == split.length) {
                continue;
            }
            twogram.add(split[i].replace(".", "") + " "
                    + split[i+1].replace(".", ""));
            total_word++;
            if (i+2 == split.length) {
                continue;
            }
            threegram.add(split[i].replace(".", "") + " "
                    + split[i+1].replace(".", "") + " "
            + split[i+2].replace(".", ""));
            total_word++;
        }
        for (String one : onegram) {
            SentiDict result = realm.where(SentiDict.class).equalTo("word", one).findFirst();
            if (result != null) {
                emotion_count += result.getScore();
            }
        }

        for (String two : twogram) {
            SentiDict result = realm.where(SentiDict.class).equalTo("word", two).findFirst();
            if (result != null) {
                emotion_count += result.getScore();
            }
        }

        for (String three : threegram) {
            SentiDict result = realm.where(SentiDict.class).equalTo("word", three).findFirst();
            if (result != null) {
                emotion_count += result.getScore();
            }
        }

        return emotion_count * 10 /total_word;
    }


}
