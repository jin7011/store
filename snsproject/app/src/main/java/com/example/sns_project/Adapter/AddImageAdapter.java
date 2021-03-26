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

public class AddImageAdapter extends RecyclerView.Adapter<AddImageAdapter.AddImageHolder> {

    //    private com.example.sns_project.Info.ImageList imageList = ImageList.getimageListInstance();
    private Activity activity;
    private ArrayList<Uri> UriFormats = new ArrayList<>();
    private LiveData_WritePost liveData_writePost;

    public AddImageAdapter(Activity activity) {
        liveData_writePost = new ViewModelProvider((ViewModelStoreOwner)activity).get(LiveData_WritePost.class);
        this.activity = activity;
        this.UriFormats = liveData_writePost.get().getValue();
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
    public AddImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //비어있는 홀더에 비어있는 이미지뷰를 만들어줌

        ImageView imageView = (ImageView)LayoutInflater.from(parent.getContext()).inflate(R.layout.item_addimage,parent,false);
//        liveData_writePost = new ViewModelProvider((ViewModelStoreOwner)activity).get(LiveData_WritePost.class);
        AddImageHolder addImageHolder = new AddImageHolder(imageView);

        return addImageHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AddImageHolder holder, int position) { //포지션에 맞게 이미지 셋업
        setPostData(holder, position);
    }

    private void setPostData(AddImageHolder holder, int position) {
        RequestOptions option_circle = new RequestOptions().circleCrop();
        Glide.with(activity).load(UriFormats.get(position).toString()).transform(new FitCenter()).override(500,500).apply(option_circle).into(holder.imageView);
        setClickListenerOnHolder(holder, position);
    }

    private void setClickListenerOnHolder(AddImageHolder holder, int position) {

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("삭제");
                builder.setMessage("해당 항목을 삭제하시겠습니까?");
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                UriFormats.remove(position); //해당 포지션의 공용데이터리스트를 제거
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, UriFormats.size());
                                liveData_writePost.get().setValue(UriFormats);

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
        return liveData_writePost.get().getValue().size();
    }




}

