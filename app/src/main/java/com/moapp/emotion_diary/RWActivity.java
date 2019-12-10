package com.moapp.emotion_diary;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.realm.Realm;

public class RWActivity extends AppCompatActivity {

    Realm realm;
    private TextView textView;
    private String mYear;
    private String mMonth;
    private String mDate;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rw);
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
        //연월일이 표시되어야 하는데 잘 작동하지 않는 것 같음.
        textView.setText(mYear + "년 " + mMonth + "월 " + mDate + "일");

    }

    @Override
    protected void onResume() {
        super.onResume();
        //연월일이 표시되어야 하는데 잘 작동하지 않는 것 같음.


    }

    public void clickSave(View view) {
        editText = findViewById(R.id.content_view);
        //저장 버튼 누를 시 새로운 DiaryData 타입의 데이터를 만든 후 연월일, 일기 내용 설정 후 데이터베이스에 저장
        realm.executeTransaction(new Realm.Transaction(){
            @Override
            public void execute(Realm realm) {
                if(realm.where(DiaryData.class)
                        .equalTo("uniqueKey", Integer.parseInt(mYear+mMonth+mDate)).findAll() == null){
                DiaryData diaryData = realm.createObject(DiaryData.class, Integer.parseInt(mYear + mMonth + mDate));
                diaryData.setContent(editText.getText().toString());
                diaryData.setDate(Integer.parseInt(mDate));
                diaryData.setYear(Integer.parseInt(mYear));
                diaryData.setMonth(Integer.parseInt(mMonth));
            } else{
                    DiaryData result = realm.where(DiaryData.class)
                            .equalTo("uniqueKey", Integer.parseInt(mYear+mMonth+mDate)).findFirst();
                    result.setContent(editText.getText().toString());
                }
            }
        });
        finish();
    }

    public void clickDatePicker(View view) {
        //날짜를 클릭할 시 캘린더 대화창 띄움
        DatePickerDialog dialog = new DatePickerDialog(this, listener
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

    //삭제 버튼 누를 시 오늘 연월일에 해당하는 일기를 찾은 뒤 데이터베이스에서 삭제
    public void clickDelete(View view) {
        DiaryData data = realm.where(DiaryData.class).equalTo("uniqueKey",
                Integer.parseInt(mYear+mMonth+mDate)).findFirst();
        realm.beginTransaction();
        data.deleteFromRealm();
        realm.commitTransaction();
    }
}
