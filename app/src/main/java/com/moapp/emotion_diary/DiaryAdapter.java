package com.moapp.emotion_diary;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.ViewHolder> {
    private RealmResults<DiaryData> mlist;
    private Realm realm;
    private List<DiaryData> con_list = new ArrayList<>();
    private int year;
    private int month;
    private int[] temp;
    private String temp_content;
    private int deleted_position;
    public DiaryAdapter(RealmResults<DiaryData> list) {
        this.mlist = list;
        //렐름 인스턴스 불러오기
        realm = Realm.getDefaultInstance();
        con_list.addAll(realm.copyFromRealm(mlist));
    }

    public void updateData(RealmResults<DiaryData> update_data) {
        this.con_list.clear();
        this.con_list.addAll(realm.copyFromRealm(update_data));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //커스텀 레이아웃을 각 아이템 뷰에 붙여줌
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        //RealmResults 타입의 데이터를 List 타입으로 타입캐스팅
        return new DiaryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //순서대로 항목을 불러와서 각 항목 레이아웃에 설정해줌
        DiaryData temp = con_list.get(position);
        holder.date.setText(Integer.toString(temp.getDate()));
        holder.content.setText(temp.getContent());
    }

    @Override
    public int getItemCount() {
        return con_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView date;
        private final TextView content;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            content = itemView.findViewById(R.id.content);
            year = con_list.get(0).getYear();
            month = con_list.get(0).getMonth();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        Intent intent = new Intent(content.getContext(), RWActivity.class);
                        intent.putExtra("year", year)
                                .putExtra("month", month)
                                .putExtra("date", con_list.get(pos).getDate())
                                .putExtra("content", con_list.get(pos).getContent());
                        content.getContext().startActivity(intent);
                    }
                }
            });
        }
    }

    void removeItem(int position) {
        temp = new int[4];
        String rYear = Integer.toString(con_list.get(position).getYear());
        String rMonth = Integer.toString(con_list.get(position).getMonth());
        String rDate = Integer.toString(con_list.get(position).getDate());
        int rEmotion = con_list.get(position).getEmotion();
        temp_content = con_list.get(position).getContent();
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
        notifyItemRemoved(position);
        con_list.remove(position);
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
        notifyItemInserted(deleted_position);
        con_list.add(deleted_position, diaryData);
        temp = null;
        temp_content = null;
        deleted_position = 0;
    }
}
