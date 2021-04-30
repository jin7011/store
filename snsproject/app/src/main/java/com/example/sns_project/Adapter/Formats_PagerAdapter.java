package com.example.sns_project.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.sns_project.R;
import com.example.sns_project.util.Named;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

import static com.example.sns_project.util.Named.IMAGE_TYPE;
import static com.example.sns_project.util.Named.VIDEO_TYPE;

public class Formats_PagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Boolean playWhenReady = true;
    private int currentWindow = 0;
    private Long playbackPosition = 0L;
    private Named named = new Named();

    private ArrayList<String> formats;
    private Context context;
    private SimpleExoPlayer player;
    LayoutInflater inflater;

    public Formats_PagerAdapter(Context context, ArrayList<String> formats) {
            this.context = context;
            this.formats = formats;
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
            initializePlayer(((Video_PagerHolder) holder).playerView,formats.get(position));
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
        if(isVideo(formats.get(position)))
            return VIDEO_TYPE;
        else
            return IMAGE_TYPE;
    }

    @Override
    public long getItemId(int position) {
        return formats.get(position).hashCode();
    }

    //확장자가 비디오인지 이미지인지 확인
    private Boolean isVideo(String path) {
        String extension = getExtension(path);

//        if (extension.contains("mp4") || extension.contains("MP4") || extension.contains("MOV") || extension.contains("mov") || extension.contains("AVI") || extension.contains("avi") ||
//                extension.contains("MKV") || extension.contains("mkv") || extension.contains("WMV") || extension.contains("wmv") || extension.contains("TS") || extension.contains("ts") ||
//                extension.contains("TP") || extension.contains("tp") || extension.contains("FLV") || extension.contains("flv") || extension.contains("3GP") || extension.contains("3gp") ||
//                extension.contains("MPG") || extension.contains("mpg") || extension.contains("MPEG") || extension.contains("mpeg") || extension.contains("MPE") || extension.contains("mpe") ||
//                extension.contains("ASF") || extension.contains("asf") || extension.contains("ASX") || extension.contains("asx") || extension.contains("DAT") || extension.contains("dat") ||
//                extension.contains("RM") || extension.contains("rm"))
        if (extension.contains("mp4") || extension.contains("MP4") || extension.contains("MOV") || extension.contains("mov") || extension.contains("AVI") || extension.contains("avi") ||
                extension.contains("MKV") || extension.contains("mkv") || extension.contains("WMV") || extension.contains("wmv") || extension.contains("TS") || extension.contains("ts") ||
                extension.contains("TP") || extension.contains("tp") || extension.contains("FLV") || extension.contains("flv") || extension.contains("3GP") || extension.contains("3gp") ||
                extension.contains("MPG") || extension.contains("mpg") || extension.contains("MPEG") || extension.contains("mpeg") || extension.contains("MPE") || extension.contains("mpe") ||
                extension.contains("ASF") || extension.contains("asf") || extension.contains("ASX") || extension.contains("asx") || extension.contains("DAT") || extension.contains("dat") ||
                extension.contains("RM") || extension.contains("rm"))
        {
            return true;
        } else {
            return false;
        }
    }

    //확장자 나누기
    private String getExtension(String url) {
        return url.substring(url.lastIndexOf(".") + 1, url.lastIndexOf("?")+1);
    }

    private void initializePlayer(PlayerView playerView,String uri) {
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(context.getApplicationContext());
            //플레이어 연결
            playerView.setPlayer(player);
        }

        MediaSource mediaSource = buildMediaSource(Uri.parse(uri));

        //prepare
        player.prepare(mediaSource, true, false);
        //start,stop
        player.setPlayWhenReady(playWhenReady);
    }

    private MediaSource buildMediaSource(Uri uri) {
        String userAgent = Util.getUserAgent(context, "blackJin");

        return new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory(userAgent))
                .createMediaSource(uri);
    }

    public void releasePlayer(PlayerView playerView) {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();

            playerView.setPlayer(null);
            player.release();
            player = null;

        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        if(holder instanceof Video_PagerHolder)
            releasePlayer(((Video_PagerHolder) holder).playerView); //동영상같이 용량 큰 친구는 가능하면 릭안나게 잘빼주자
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        if(holder instanceof Video_PagerHolder)
            initializePlayer(((Video_PagerHolder) holder).playerView,formats.get(holder.getAbsoluteAdapterPosition())); //동영상같이 용량 큰 친구는 가능하면 릭안나게 잘빼주자
    }
}

