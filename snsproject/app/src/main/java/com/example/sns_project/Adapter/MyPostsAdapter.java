package com.example.sns_project.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.sns_project.R;
import com.example.sns_project.activity.PostActivity;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.util.PostInfo_DiffUtil;
import java.util.ArrayList;
import java.util.Date;
import static com.example.sns_project.CustomLibrary.PostControler.Time_to_String;
import static com.example.sns_project.util.Named.POSTHODER_TO_POSTACTIVITY;

public class MyPostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private ArrayList<PostInfo> postList = new ArrayList<>();

    public void PostInfoDiffUtil(ArrayList<PostInfo> newPosts) {
        final PostInfo_DiffUtil diffCallback = new PostInfo_DiffUtil(this.postList, newPosts);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        postList.clear();
        postList.addAll(newPosts);

        diffResult.dispatchUpdatesTo(this);
    }

    public MyPostsAdapter(Activity activity) {
        this.activity = activity;
    }

    //holder
    public static class MyPostHolder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함

        TextView titleT ;
        TextView contentT ;
        TextView dateT ;
        TextView goodNum;
        TextView commentNum;
        ImageView imageView;
        TextView nicknameT;
        TextView whereT;

        public MyPostHolder(@NonNull View itemView) {
            super(itemView);

            nicknameT = itemView.findViewById(R.id.nicknameT);
            titleT = itemView.findViewById(R.id.titleT);
            contentT = itemView.findViewById(R.id.contentT);
            dateT = itemView.findViewById(R.id.dateT);
            goodNum = itemView.findViewById(R.id.goodNum_postItem);
            commentNum = itemView.findViewById(R.id.commentNum_postItem);
            imageView = itemView.findViewById(R.id.postImage);
            whereT = itemView.findViewById(R.id.whereT);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //비어있는 홀더에 비어있는 이미지뷰를 만들어줌

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_myposts, parent, false);
        MyPostHolder myPostHolder = new MyPostHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                Intent intent = new Intent(activity, PostActivity.class);
                intent.putExtra("postInfo", (PostInfo) postList.get(myPostHolder.getBindingAdapterPosition()));
                activity.startActivityForResult(intent, POSTHODER_TO_POSTACTIVITY);
            }
        });

        return myPostHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder Holder, int position) { //포지션에 맞게 이미지 셋업

        if(Holder instanceof MyPostHolder) {
            PostInfo postInfo = postList.get(position);
            postInfo.setHow_Long(Time_to_String(postInfo.getCreatedAt(), new Date()));

            MyPostHolder holder = (MyPostHolder)Holder;

            holder.titleT.setText(postInfo.getTitle());
            holder.contentT.setText(postInfo.getContents());
            holder.dateT.setText(postInfo.getHow_Long());
            holder.goodNum.setText(postInfo.getGood() + "");
            holder.commentNum.setText(postInfo.getComment() + "");
            holder.nicknameT.setText(postInfo.getPublisher());
            holder.whereT.setText(postInfo.getLocation());

            if (postInfo.getFormats() != null) {
                String format = postInfo.getFormats().get(0);
                Log.d("bind 사진 바인드", "foramt: " + format);
                holder.imageView.setVisibility(View.VISIBLE);
                Glide.with(activity).load(format).diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(new CenterCrop(), new RoundedCorners(50))
                        .thumbnail(0.3f).into(holder.imageView);
            } else {
                holder.imageView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

}
