package com.moapp.emotion_diary;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

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

    public DiaryAdapter(RealmResults<DiaryData> list, Activity context) {
        mlist = list;
        realm = Realm.getDefaultInstance();


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        con_list.addAll(realm.copyFromRealm(mlist));
        return new DiaryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiaryData temp = con_list.get(position);
        holder.date.setText(temp.getDate());
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
            TextView year_show = itemView.findViewById(R.id.year);
            TextView month_show = itemView.findViewById(R.id.month);
            year = Integer.parseInt(year_show.getText().toString());
            month = Integer.parseInt(month_show.getText().toString());

            date = itemView.findViewById(R.id.date);
            content = itemView.findViewById(R.id.content);

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
}
