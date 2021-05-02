package com.example.sns_project.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.R;
import com.example.sns_project.info.CommentInfo;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.info.RecommentInfo;
import com.example.sns_project.util.CommentInfo_DiffUtil;
import com.example.sns_project.util.Named;
import com.example.sns_project.util.RecommentInfo_DiffUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.sns_project.util.Named.HOUR;
import static com.example.sns_project.util.Named.MIN;
import static com.example.sns_project.util.Named.SEC;

public class RecommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private ArrayList<RecommentInfo> recomments = new ArrayList<>();
    PostInfo postInfo;

    public void RecommentInfo_DiffUtil(ArrayList<RecommentInfo> newcomments) {
        final RecommentInfo_DiffUtil diffCallback = new RecommentInfo_DiffUtil(this.recomments, newcomments);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.recomments.clear();
        this.recomments.addAll(newcomments);
        diffResult.dispatchUpdatesTo(this);
    }

    public RecommentsAdapter(Activity activity,PostInfo postInfo) {
        this.postInfo = postInfo;
        this.activity = activity;
        Log.d("asss","불렸음1");
    }

    //holder
    public static class RecommentsHolder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함

        TextView contentT ;
        TextView dateT ;
        TextView goodNum;
        TextView nicknameT;
        ImageButton good_btn;
        ImageButton option_btn;

        public RecommentsHolder(@NonNull View itemView) {
            super(itemView);

            nicknameT = itemView.findViewById(R.id.nickname_RecommentT);
            contentT = itemView.findViewById(R.id.recomment_RecommentT);
            dateT = itemView.findViewById(R.id.date_RecommentT);
            goodNum = itemView.findViewById(R.id.goodNum_RecommentT);
            good_btn = itemView.findViewById(R.id.good_comment_Recomments);
            option_btn = itemView.findViewById(R.id.opt_Recomment);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //비어있는 홀더에 비어있는 이미지뷰를 만들어줌
        //todo 클릭이벤트 만들어줘야함.
        View view  =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recomments,parent,false);
        RecommentsAdapter.RecommentsHolder recommentsHolder = new RecommentsHolder(view);

        recommentsHolder.good_btn.setOnClickListener(new View.OnClickListener() { //좋아요
            @Override
            public void onClick(View v) {

            }
        });

        recommentsHolder.option_btn.setOnClickListener(new View.OnClickListener() { //옵션
            @Override
            public void onClick(View v) {
                if(isposter(recommentsHolder.getAbsoluteAdapterPosition())){ //포지션 넘겨주려고 좀 지저분해도 어쩔수 없이 내부에 온클릭을 정의했음
                    DeleteDialog();
                }else{
                    OthersDialog();
                }
            }
        });

        return recommentsHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder commentsHolder, int position) { //포지션에 맞게 이미지 셋업

        RecommentsHolder holder = (RecommentsHolder)commentsHolder;

        RecommentInfo recommentInfo = recomments.get(position);
        Log.d("asss",recommentInfo.getContents());

        holder.contentT.setText(recommentInfo.getContents());
        holder.dateT.setText(formatTimeString(recommentInfo.getCreatedAt(),new Date()));
        holder.goodNum.setText(recommentInfo.getGood()+"");

        if(isposter(position)) { //게시물 작성자가 댓글을 달았을 때,
            holder.nicknameT.setTextColor(ContextCompat.getColor(activity, R.color.maincolor));
            holder.nicknameT.setTypeface(null, Typeface.BOLD);
            holder.nicknameT.setText(recommentInfo.getPublisher()+"(글쓴이)");
        }else{
            holder.nicknameT.setTextColor(ContextCompat.getColor(activity, R.color.textcolor)); //아니라면 색상변경해줘야 리사이클러뷰 재활용할 때 혼동안옴.
            holder.nicknameT.setTypeface(null, Typeface.NORMAL);
            holder.nicknameT.setText(recommentInfo.getPublisher());
        }

    }

    @Override
    public int getItemCount() {
        return recomments.size();
    }

    public void DeleteDialog(){
        final String[] items = {"삭제"};
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    Toast.makeText(activity,"삭제",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

    public void OthersDialog(){
        final String[] items = {"알림설정","쪽지보내기","신고"};
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //todo 기능추가해줘야함.
                switch (which){
                    case 0:
                        Toast.makeText(activity,"알림설정이 되었습니다.",Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(activity,"쪽지를 보냅시당",Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(activity,"신고 접수되었습니다.",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });

        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

    public boolean isposter(int position){

        if(recomments.get(position).getId().equals(postInfo.getId()))
            return true;
        else
            return false;

    }

    public static String formatTimeString(Date postdate,Date nowDate){

        long ctime = nowDate.getTime();
        long regTime = postdate.getTime();

        long diffTime = (ctime - regTime) / 1000;
        String msg = null;

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


}

