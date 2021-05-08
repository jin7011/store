package com.example.sns_project.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.sns_project.Activities.PostActivity;
import com.example.sns_project.R;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.util.PostInfo_DiffUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.sns_project.util.Named.HOUR;
import static com.example.sns_project.util.Named.LOADING_VIEWTYPE;
import static com.example.sns_project.util.Named.MIN;
import static com.example.sns_project.util.Named.POSTHODER_TO_POSTACTIVITY;
import static com.example.sns_project.util.Named.POSTING_VIEWTYPE;
import static com.example.sns_project.util.Named.SEC;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private ArrayList<PostInfo> postList = new ArrayList<>();

    private OnLoadMoreListener onLoadMoreListener; //todo take care of if after eating dinner
    private LinearLayoutManager mLinearLayoutManager;

    private boolean NoMore_Load = false;
    private int visibleThreshold = 1;
    int firstVisibleItem, visibleItemCount, totalItemCount, lastVisibleItem;

    public interface OnLoadMoreListener{
        void onLoadMore();
//        void Updated();
    }

    public void PostInfoDiffUtil(ArrayList<PostInfo> newPosts) {
        final PostInfo_DiffUtil diffCallback = new PostInfo_DiffUtil(this.postList, newPosts);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        final PostAdapter postAdapter = this;

        new Handler().post(new Runnable() { // 디프유틸이 스크롤리스너에 의해서 boardfrag에서 백그라운드로 작동하니까 내부 동작도 이렇게 해주자.
            @Override
            public void run() {
                Clear_AddAll(newPosts);
                diffResult.dispatchUpdatesTo(postAdapter);
            }
        });
    }

    public PostAdapter(Activity activity,OnLoadMoreListener onLoadMoreListener) {
        this.activity = activity;
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void setLinearLayoutManager(LinearLayoutManager linearLayoutManager){
        this.mLinearLayoutManager=linearLayoutManager;
    }

    //holder
    public static class PostHolder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함

        TextView titleT ;
        TextView contentT ;
        TextView dateT ;
        TextView goodNum;
        TextView commentNum;
        ImageView imageView;
        TextView nicknameT;

        public PostHolder(@NonNull View itemView) {
            super(itemView);

            nicknameT = itemView.findViewById(R.id.nicknameT);
            titleT = itemView.findViewById(R.id.titleT);
            contentT = itemView.findViewById(R.id.contentT);
            dateT = itemView.findViewById(R.id.dateT);
            goodNum = itemView.findViewById(R.id.goodNum_postItem);
            commentNum = itemView.findViewById(R.id.commentNum_postItem);
            imageView = itemView.findViewById(R.id.postImage);

        }
    }

    public static class LoadingHolder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함
        ProgressBar loading;
        public LoadingHolder(@NonNull View itemView) {
            super(itemView);
            loading = itemView.findViewById(R.id.pBar_recyclerview_loader);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //비어있는 홀더에 비어있는 이미지뷰를 만들어줌

        if(viewType == POSTING_VIEWTYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
            PostAdapter.PostHolder postHolder = new PostAdapter.PostHolder(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, PostActivity.class);
//                Log.d("포스트어댑터","getCreatedAt: "+postList.get(postHolder.getBindingAdapterPosition()).getCreatedAt());
//                Log.d("포스트어댑터","getDocid: "+postList.get(postHolder.getBindingAdapterPosition()).getDocid());
//                Log.d("포스트어댑터","getGood_user: "+postList.get(postHolder.getBindingAdapterPosition()).getGood_user());
//                Log.d("포스트어댑터","getGood: "+postList.get(postHolder.getBindingAdapterPosition()).getGood());
//                Log.d("포스트어댑터","getComment: "+postList.get(postHolder.getBindingAdapterPosition()).getComment());
//                Log.d("포스트어댑터","getFormats: "+postList.get(postHolder.getBindingAdapterPosition()).getFormats());
//                Log.d("포스트어댑터","getId: "+postList.get(postHolder.getBindingAdapterPosition()).getId());
//                Log.d("포스트어댑터","getContents: "+postList.get(postHolder.getBindingAdapterPosition()).getContents());
//                Log.d("포스트어댑터","getLocation: "+postList.get(postHolder.getBindingAdapterPosition()).getLocation());
//                Log.d("포스트어댑터","getPublisher: "+postList.get(postHolder.getBindingAdapterPosition()).getPublisher());
//                Log.d("포스트어댑터","getTitle: "+postList.get(postHolder.getBindingAdapterPosition()).getTitle());
//                Log.d("포스트어댑터","getStoragePath: "+postList.get(postHolder.getBindingAdapterPosition()).getStoragePath());
//                Log.d("포스트어댑터","comment_good_user: "+postList.get(postHolder.getBindingAdapterPosition()).getComments().get(postHolder.getAbsoluteAdapterPosition()).getGood_user());
                    intent.putExtra("postInfo", (PostInfo) postList.get(postHolder.getBindingAdapterPosition()));
                    activity.startActivityForResult(intent, POSTHODER_TO_POSTACTIVITY);
                }
            });
            return postHolder;
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_recyclerview_loader, parent, false);
            return new LoadingHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) { //포지션에 맞게 이미지 셋업

        if(holder instanceof PostHolder) {
            PostInfo postInfo = postList.get(position);
            postInfo.setHow_Long(formatTimeString(postInfo.getCreatedAt(), new Date()));

            ((PostHolder) holder).titleT.setText(postInfo.getTitle());
            ((PostHolder) holder).contentT.setText(postInfo.getContents());
            ((PostHolder) holder).dateT.setText(postInfo.getHow_Long());
            ((PostHolder) holder).goodNum.setText(postInfo.getGood() + "");
            ((PostHolder) holder).commentNum.setText(postInfo.getComment() + "");
            ((PostHolder) holder).nicknameT.setText(postInfo.getPublisher());

            if (postInfo.getFormats() != null) {
                String format = postInfo.getFormats().get(0);
                Log.d("bind 사진 바인드", "foramt: " + format);
                ((PostHolder) holder).imageView.setVisibility(View.VISIBLE);
                Glide.with(activity).load(format).diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(new CenterCrop(), new RoundedCorners(50))
                        .thumbnail(0.3f).into(((PostHolder) holder).imageView);
            } else {
                ((PostHolder) holder).imageView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return postList.get(position) != null ? POSTING_VIEWTYPE : LOADING_VIEWTYPE;
    }

    public void setRecyclerView(RecyclerView mView){
        mView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = mLinearLayoutManager.getItemCount();
                firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
                lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();
//                Log.d("total", totalItemCount + "");
//                Log.d("visible", visibleItemCount + "");
//                Log.d("first", firstVisibleItem + "");
//                Log.d("last", lastVisibleItem + "");
                if (!NoMore_Load && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold) && totalItemCount != 0) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore();
                    }
                    NoMore_Load = true;
                }
            }
        });
    }

    public void setProgressMore(final boolean isProgress) {
        if (isProgress) {
            ArrayList<PostInfo> temp = deepCopy(postList); //딥카피해서 널넣고 디프해줌
            temp.add(null);
            this.PostInfoDiffUtil(temp); //디프의 결과는 얕은카피되므로 postlist = temp가 됨 -> postlist의 마지막은 널.
        }
        //디프내부에서는 널값을 항상 다른 아이템으로 표시하였음.
    }

    public void NoMore_Load(boolean NoMore_Load) {
        this.NoMore_Load = NoMore_Load;
    }

    public static String formatTimeString(Date postdate, Date nowDate){

        long ctime = nowDate.getTime();
        long regTime = postdate.getTime();

        long diffTime = (ctime - regTime) / 1000;
        String msg;

        if (diffTime < SEC) {
            msg = "방금 전";
        } else if ((diffTime /= SEC) < MIN) {
            msg = diffTime + "분 전";
        } else if ((diffTime /= MIN) < HOUR) {
            msg = new SimpleDateFormat("HH:mm").format(postdate);
//        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
//            msg = (diffTime) + "일 전";
//        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
//            msg = (diffTime) + "달 전";
        } else {
            msg = new SimpleDateFormat("MM월dd일").format(postdate);
        }
        return msg;
    }

    public ArrayList<PostInfo> deepCopy(ArrayList<PostInfo> oldone){

        ArrayList<PostInfo> newone = new ArrayList<>();

        for(int x=0; x<oldone.size(); x++) {
            if (oldone.get(x) == null) {
                newone.add(null);
            }else
                newone.add(new PostInfo(oldone.get(x)));
        }

        return newone;
    }

    public void Clear_AddAll(ArrayList<PostInfo> newPosts){
        this.postList.clear();
        this.postList.addAll(newPosts);
    }
}
