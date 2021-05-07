package com.example.sns_project.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sns_project.Activities.PopupActivity;
import com.example.sns_project.R;

public class Change_LocationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String prelocation;
    private  String[] arr;
    private PopupActivity activity;
    private Change_LocationHolder pre_selected = null;

    public Change_LocationAdapter(PopupActivity activity,String[] arr,String prelocation){
        this.activity = activity;
        this.arr = arr;
        this.prelocation = prelocation;
    }

    //holder
    static class Change_LocationHolder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함
        TextView location;
        FrameLayout frame;

        public Change_LocationHolder(@NonNull View itemView) {
            super(itemView);
            location = itemView.findViewById(R.id.change_locationT);
            frame = itemView.findViewById(R.id.change_locaiont_FrameLayout);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_change_location,parent,false);
        Change_LocationHolder change_locationHolder = new Change_LocationHolder(frameLayout);
        setClickListenerOnHolder(change_locationHolder, change_locationHolder.getAbsoluteAdapterPosition());
        return change_locationHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((Change_LocationHolder)holder).location.setText(arr[position]);
    }

    @Override
    public int getItemCount() {
        return arr.length;
    }

    private void setClickListenerOnHolder(Change_LocationHolder holder, int position) {

        holder.frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pre_selected == null){ //첫 선택이라면,
                    holder.frame.setBackgroundResource(R.drawable.corner_red);
                    pre_selected = holder;
                    activity.setTextView(prelocation+" -> "+holder.location.getText().toString());
                    activity.setSelected_Location(holder.location.getText().toString());
                }else{
                    pre_selected.frame.setBackgroundResource(R.drawable.corner_black);
                    holder.frame.setBackgroundResource(R.drawable.corner_red);
                    pre_selected = holder;
                    activity.setTextView(prelocation+" -> "+holder.location.getText().toString());
                    activity.setSelected_Location(holder.location.getText().toString());
                }
            }
        });
    }
}
