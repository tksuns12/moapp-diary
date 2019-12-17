package com.moapp.emotion_diary;

import android.content.Context;
import android.graphics.Typeface;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class NewDiaryAdapter extends BaseAdapter {
    private RealmResults<DiaryData> mlist;
    private Realm realm;
    private DiaryData[] con_list;
    private int year;
    private int month;
    private int day;
    private int maxdaysofMonth;
    private int[] temp;
    private String temp_content;
    private int deleted_position;

    public NewDiaryAdapter(RealmResults<DiaryData> list, int iyear, int imonth, int iday) {
        mlist = list;
        realm = Realm.getDefaultInstance();
        year = iyear;
        month = imonth;
        day = iday;
        Calendar mycal = new GregorianCalendar(year, month-1, day);
        maxdaysofMonth = mycal.getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar cal = Calendar.getInstance();
        if (year==cal.get(Calendar.YEAR) && month == cal.get(Calendar.MONTH)+1) {
            maxdaysofMonth = cal.get(Calendar.DATE);
        }
        con_list = new DiaryData[maxdaysofMonth];
        initData(list);}

    private void initData(RealmResults<DiaryData> list) {
        DiaryData dummy = new DiaryData();
        for(int i=0; i<maxdaysofMonth;i++) {
            con_list[i] = dummy;
        }
        for(DiaryData m:list) {
            con_list[m.getDate()-1] = m;
        }
    }

    public void updateData(RealmResults<DiaryData> update_data) {
        initData(update_data);
    }

    @Override
    public int getCount() {
        return con_list.length;
    }

    @Override
    public Object getItem(int i) {
        return con_list[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public static class ViewHolder {
        public ImageView imageView;
        public TextView date;
        public TextView content;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final Context context = viewGroup.getContext();

        final ViewHolder viewHolder;

            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item, viewGroup, false);
            viewHolder.imageView = view.findViewById(R.id.add_diary);
            viewHolder.date = view.findViewById(R.id.date);
            viewHolder.date.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Crasns.ttf"));
            viewHolder.content = view.findViewById(R.id.content);
            viewHolder.content.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/SDMiSaeng.ttf"));
            view.setTag(viewHolder);


            viewHolder.date.setText(Integer.toString(i+1));

            if (con_list[i].getContent() == null) {
                viewHolder.imageView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.imageView.setVisibility(View.GONE);
                viewHolder.content.setText(con_list[i].getContent());
            }


        return view;
    }

    void removeItem(int position) {
        temp = new int[4];
        String rYear = Integer.toString(con_list[position].getYear());
        String rMonth = Integer.toString(con_list[position].getMonth());
        String rDate = Integer.toString(con_list[position].getDate());
        int rEmotion = con_list[position].getEmotion();
        temp_content = con_list[position].getContent();
        temp[0] = Integer.parseInt(rYear);
        temp[1] = Integer.parseInt(rMonth);
        temp[2] = Integer.parseInt(rDate);
        temp[3] = rEmotion;
        realm.beginTransaction();
        DiaryData data = realm.where(DiaryData.class).equalTo("uniqueKey",
                Integer.parseInt(rYear+rMonth+rDate)).findFirst();

        deleted_position = position;
        data.deleteFromRealm();
        realm.commitTransaction();
        con_list[position] = null;
    }

    void restoreItem() {
        String rYear = Integer.toString(temp[0]);
        String rMonth = Integer.toString(temp[1]);
        String rDate = Integer.toString(temp[2]);
        realm.beginTransaction();
        DiaryData diaryData = realm.createObject(DiaryData.class, Integer.parseInt(rYear+rMonth+rDate));
        diaryData.setContent(temp_content);
        diaryData.setYear(temp[0]);
        diaryData.setMonth(temp[1]);
        diaryData.setDate(temp[2]);
        diaryData.setEmotion(temp[3]);
        realm.commitTransaction();
        con_list[deleted_position] = diaryData;
        temp = null;
        temp_content = null;
        deleted_position = 0;
    }
}

