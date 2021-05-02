package com.example.sns_project.util;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class Video_ExoUtil {
    private Boolean playWhenReady = true;
    private int currentWindow = 0;
    private Long playbackPosition = 0L;
    private SimpleExoPlayer player;
    private Context context;

    public Video_ExoUtil(Context context){
        this.context = context;
    }

    //확장자가 비디오인지 이미지인지 확인
    public Boolean isVideo(String path) {
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
    public String getExtension(String url) {
        return url.substring(url.lastIndexOf(".") + 1, url.lastIndexOf("?")+1);
    }

    public void initializePlayer(String uri,PlayerView playerView) {
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

    public MediaSource buildMediaSource(Uri uri) {
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

    public void just_releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();

            player.release();
            player = null;
        }
    }
}
