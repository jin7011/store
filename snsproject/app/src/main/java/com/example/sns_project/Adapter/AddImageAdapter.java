package com.example.sns_project.Adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sns_project.Info.ImageList;
import com.example.sns_project.R;

public class AddImageAdapter extends RecyclerView.Adapter<AddImageAdapter.AddImageHolder> {

    private com.example.sns_project.Info.ImageList imageList = ImageList.getimageListInstance();
    private Activity activity;

    public AddImageAdapter(Activity activity) {
        this.activity = activity;
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
        final AddImageHolder addImageHolder = new AddImageHolder(imageView);

        return addImageHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AddImageHolder holder, int position) { //포지션에 맞게 이미지 셋업
        RequestOptions option_circle = new RequestOptions().circleCrop();

        ImageView imageView = holder.imageView;
        Glide.with(activity).load(imageList.getImageList().get(position)).override(500).apply(option_circle).into(imageView);

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("삭제");
                builder.setMessage("해당 항목을 삭제하시겠습니까?");
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
//                                imageList.getImageList().remove(addImageHolder.getAdapterPosition());
//                                notifyItemRemoved(addImageHolder.getAdapterPosition());
//                                notifyItemRangeChanged(addImageHolder.getAdapterPosition(), imageList.getImageList().size());

                                imageList.getImageList().remove(position); //해당 포지션의 공용데이터리스트를 제거
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, imageList.getImageList().size());
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
        return imageList.getImageList().size();
    }


}

