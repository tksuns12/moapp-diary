package com.moapp.emotion_diary;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import io.realm.Realm;
import io.realm.RealmResults;

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
        int today_year = calendar.get(Calendar.YEAR);
        int today_month = calendar.get((Calendar.MONTH)+1);
        int today_date = calendar.get(Calendar.DATE);
        realm = Realm.getDefaultInstance();
        Intent intent = getIntent();
        mYear = Integer.toString(intent.getIntExtra("year", today_year));
        mMonth = Integer.toString(intent.getIntExtra("month", today_month));
        mDate = Integer.toString(intent.getIntExtra("date", today_date));
        textView.setText(mYear + "년 " + mMonth + "월 " + mDate + "일");

    }

    @Override
    protected void onResume() {
        super.onResume();
        textView.setText(mYear + "년 " + mMonth + "월 " + mDate + "일");

    }

    public void clickSave(View view) {
        editText = findViewById(R.id.content_view);
        realm.beginTransaction();
        DiaryData diaryData = realm.createObject(DiaryData.class, Integer.parseInt(mYear + mMonth + mDate));
        diaryData.setContent(editText.getText().toString());
        diaryData.setDate(Integer.parseInt(mDate));
        diaryData.setYear(Integer.parseInt(mYear));
        diaryData.setMonth(Integer.parseInt(mMonth));
        realm.copyToRealm(diaryData);
        realm.commitTransaction();
    }

    public void clickDatePicker(View view) {
        DatePickerDialog dialog = new DatePickerDialog(this, listener
                , Integer.parseInt(mYear)
                , Integer.parseInt(mMonth)-1
                , Integer.parseInt(mDate));
        dialog.show();
    }

    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mYear = Integer.toString(year);
            mMonth = Integer.toString(month);
            mDate = Integer.toString(dayOfMonth);
        }
    };

    public void clickDelete(View view) {
        DiaryData data = realm.where(DiaryData.class).equalTo("uniqueKey",
                Integer.parseInt(mYear+mMonth+mDate)).findFirst();
        realm.beginTransaction();
        data.deleteFromRealm();
        realm.commitTransaction();
    }
}
