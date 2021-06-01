package com.example.sns_project.Adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
import com.example.sns_project.R;
import com.example.sns_project.data.LiveData_WritePost;

import java.util.ArrayList;

public class AddImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private ArrayList<Uri> UriFormats = new ArrayList<>();
    private Listener_Delete listener_delete;

    public interface Listener_Delete{
        void onDelete(int position);
    }

    public AddImageAdapter(Activity activity,Listener_Delete listener_delete) {
        this.listener_delete = listener_delete;
        this.activity = activity;
    }

    public void Set_Uri( ArrayList<Uri> UriFormats){
        this.UriFormats = UriFormats;
    }

    //holder
    static class AddImageHolder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함
        ImageView imageView;

        public AddImageHolder(@NonNull ImageView imageView) {
            super(imageView);
            this.imageView = imageView;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //비어있는 홀더에 비어있는 이미지뷰를 만들어줌

        ImageView imageView = (ImageView)LayoutInflater.from(parent.getContext()).inflate(R.layout.item_addimage,parent,false);
        AddImageHolder addImageHolder = new AddImageHolder(imageView);
        setClickListenerOnHolder(addImageHolder);
        return addImageHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) { //포지션에 맞게 이미지 셋업
        if (holder instanceof AddImageHolder) {
            RequestOptions option_circle = new RequestOptions().circleCrop();
            Glide.with(activity).load(UriFormats.get(position).toString()).transform(new FitCenter()).override(500, 500).apply(option_circle).into(((AddImageHolder)holder).imageView);
        }
    }

    private void setClickListenerOnHolder(AddImageHolder holder) {

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("삭제");
                builder.setMessage("해당 항목을 삭제하시겠습니까?");
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                listener_delete.onDelete(holder.getAbsoluteAdapterPosition());
                            }
                        });
                builder.setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.show();

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return UriFormats.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}

