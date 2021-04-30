package com.example.sns_project.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sns_project.R;
import com.example.sns_project.util.Named;
import com.example.sns_project.util.Video_ExoUtil;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.ui.PlayerView;
import java.util.ArrayList;
import static com.example.sns_project.util.Named.IMAGE_TYPE;
import static com.example.sns_project.util.Named.VIDEO_TYPE;

public class Formats_PagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Named named = new Named();
    private ArrayList<String> formats;
    private Context context;
    public Video_ExoUtil exoUtil;
    LayoutInflater inflater;

    public Formats_PagerAdapter(Context context, ArrayList<String> formats) {
            this.context = context;
            this.formats = formats;
            this.exoUtil = new Video_ExoUtil(context);
            inflater = LayoutInflater.from(context);
        }

    public static class PagerHolder extends RecyclerView.ViewHolder {
        PhotoView imageView;
        public PagerHolder(@NonNull View itemView) { //뷰홀더에서 작업들 실행
            super(itemView);
            imageView = itemView.findViewById(R.id.formats_imageView);
        }
    }

    public static class Video_PagerHolder extends RecyclerView.ViewHolder {
        PlayerView playerView;
        public Video_PagerHolder(@NonNull View itemView) { //뷰홀더에서 작업들 실행
            super(itemView);
            playerView = itemView.findViewById(R.id.formats_exoPlayerView);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIDEO_TYPE) {
            View view = inflater.from(parent.getContext()).inflate(R.layout.item_videoforamats_pager, parent, false);
            return new Video_PagerHolder(view);
        }
        else {
            View view = inflater.from(parent.getContext()).inflate(R.layout.item_formats_pager, parent, false);
            return new PagerHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof Video_PagerHolder){
            exoUtil.initializePlayer(formats.get(position),((Video_PagerHolder) holder).playerView);
        }else if(holder instanceof PagerHolder){
            Glide.with(context).load(formats.get(position))
//                    .transform(new CenterCrop())
                    .thumbnail(0.4f).into(((PagerHolder) holder).imageView);
        }
    }

    @Override
    public int getItemCount() {
        return formats.size();
    }

    @Override
    public int getItemViewType(int position) { //oncreateviewholder로 반환
        if(exoUtil.isVideo(formats.get(position)))
            return VIDEO_TYPE;
        else
            return IMAGE_TYPE;
    }

    @Override
    public long getItemId(int position) {
        return formats.get(position).hashCode();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        if(holder instanceof Video_PagerHolder) {
            exoUtil.releasePlayer(((Video_PagerHolder) holder).playerView); //동영상같이 용량 큰 친구는 가능하면 릭안나게 잘빼주자
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        if(holder instanceof Video_PagerHolder)
            exoUtil.initializePlayer(formats.get(holder.getAbsoluteAdapterPosition()),((Video_PagerHolder) holder).playerView); //왔다갔따 하기때문에 다시 볼땐 다시 붙여주기~
    }
}

