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
    private DiaryData temp;

    public DiaryAdapter(RealmResults<DiaryData> list) {
        mlist = list;
        //렐름 인스턴스 불러오기
        realm = Realm.getDefaultInstance();
        con_list.addAll(realm.copyFromRealm(mlist));
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

    public void removeItem(int position) {
        String rYear = Integer.toString(con_list.get(position).getYear());
        String rMonth = Integer.toString(con_list.get(position).getMonth());
        String rDate = Integer.toString(con_list.get(position).getDate());
        realm.beginTransaction();
        DiaryData data = realm.where(DiaryData.class).equalTo("uniqueKey",
                Integer.parseInt(rYear+rMonth+rDate)).findFirst();
        temp = data;
        data.deleteFromRealm();
        realm.commitTransaction();
        notifyItemRemoved(position);
    }

    public void restoreItem() {
        realm.beginTransaction();
        realm.copyToRealm(temp);
        realm.commitTransaction();
        notifyItemInserted(temp.getDate());
        temp = null;
    }
}
