package com.example.sns_project.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.Activities.PostActivity;
import com.example.sns_project.Listener.Listener_BackButton;
import com.example.sns_project.Listener.Listener_CommentHolder;
import com.example.sns_project.R;
import com.example.sns_project.info.CommentInfo;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.util.CommentInfo_DiffUtil;
import com.example.sns_project.util.Named;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.sns_project.util.Named.HOUR;
import static com.example.sns_project.util.Named.MIN;
import static com.example.sns_project.util.Named.SEC;
import static com.example.sns_project.util.Named.Write_Recomment;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsHolder> implements Listener_BackButton {

    private PostActivity activity;
    private ArrayList<CommentInfo> comments = new ArrayList<>();
    private Named named = new Named();
    private Listener_CommentHolder listener_commentHolder;
    PostInfo postInfo;

    public void CommentInfo_DiffUtil(ArrayList<CommentInfo> newcomments) {
        final CommentInfo_DiffUtil diffCallback = new CommentInfo_DiffUtil(this.comments, newcomments);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.comments.clear();
        this.comments.addAll(newcomments);
        diffResult.dispatchUpdatesTo(this);
    }

    public CommentsAdapter(PostActivity activity,PostInfo postInfo,Listener_CommentHolder listener_commentHolder) {
        this.postInfo = postInfo;
        this.activity = activity;
        this.listener_commentHolder = listener_commentHolder;
    }

    //holder
    public class CommentsHolder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함

        //R.layout.item_comments에 존재하지 않는 뷰는 일반적으로는 설정하나마나임
        TextView contentT ;
        TextView dateT ;
        TextView goodNum;
        TextView nicknameT;
        ImageButton good_btn;
        ImageButton recomment_btn;
        ImageButton option_btn;
        LinearLayout commentbody;
        LinearLayout commentbody_goodframe;
        RecyclerView recyclerView;

        public CommentsHolder(@NonNull View itemView) {
            super(itemView);

            nicknameT = itemView.findViewById(R.id.nickname_commentT);
            contentT = itemView.findViewById(R.id.comment_commentT);
            dateT = itemView.findViewById(R.id.date_commentT);
            goodNum = itemView.findViewById(R.id.goodNum_commentT);
            good_btn = itemView.findViewById(R.id.good_comment_comments);
            recomment_btn = itemView.findViewById(R.id.re_comment);
            option_btn = itemView.findViewById(R.id.opt_comment);
            recyclerView = itemView.findViewById(R.id.recommentRecycler);
            commentbody = itemView.findViewById(R.id.commentbody_comments);
            commentbody_goodframe = itemView.findViewById(R.id.good_btn_Frame);

        }
    }

    @NonNull
    @Override
    public CommentsAdapter.CommentsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //비어있는 홀더에 비어있는 이미지뷰를 만들어줌
        View view  =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comments,parent,false);
        CommentsAdapter.CommentsHolder commentsHolder = new CommentsAdapter.CommentsHolder(view);

        commentsHolder.good_btn.setOnClickListener(new View.OnClickListener() { //좋아요
            @Override
            public void onClick(View v) {
            }
        });

        commentsHolder.recomment_btn.setOnClickListener(new View.OnClickListener() { //대댓글
            @Override
            public void onClick(View v) {
                RecommentDialog(commentsHolder);
    }
});
        commentsHolder.option_btn.setOnClickListener(new View.OnClickListener() { //옵션
            @Override
            public void onClick(View v) {
                if(isposter(commentsHolder.getAbsoluteAdapterPosition())){ //포지션 넘겨주려고 좀 지저분해도 어쩔수 없이 내부에 온클릭을 정의했음
                    DeleteDialog();
                }else{
                    OthersDialog();
                }
            }
        });

        return commentsHolder;
    }



    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CommentsAdapter.CommentsHolder holder, int position) { //포지션에 맞게 이미지 셋업

        CommentInfo commentInfo = comments.get(position);

        holder.contentT.setText(commentInfo.getContents());
        holder.dateT.setText(formatTimeString(commentInfo.getCreatedAt(),new Date()));
        holder.goodNum.setText(commentInfo.getGood()+"");

        if(isposter(position)) { //게시물 작성자가 댓글을 달았을 때,
            holder.nicknameT.setTextColor(ContextCompat.getColor(activity, R.color.maincolor));
            holder.nicknameT.setTypeface(null, Typeface.BOLD);
            holder.nicknameT.setText(commentInfo.getPublisher()+"(글쓴이)");
        }else{
            holder.nicknameT.setTextColor(ContextCompat.getColor(activity, R.color.textcolor)); //아니라면 색상변경해줘야 리사이클러뷰 재활용할 때 혼동안옴.
            holder.nicknameT.setTypeface(null, Typeface.NORMAL);
            holder.nicknameT.setText(commentInfo.getPublisher());
        }

        if(commentInfo.getRecomments().size() != 0){
            RecommentsAdapter recommentsAdapter = new RecommentsAdapter(activity,postInfo);
            Log.d("asss",commentInfo.getRecomments().size()+"");
            holder.recyclerView.setVisibility(View.VISIBLE);
            Add_and_Set_RecommentRecyclerView(activity,holder.recyclerView,recommentsAdapter);
            recommentsAdapter.RecommentInfo_DiffUtil(commentInfo.getRecomments());
        }else{
            holder.recyclerView.setVisibility(View.GONE);
        }

    }

    public void Add_and_Set_RecommentRecyclerView(Activity activity,RecyclerView recyclerView,RecommentsAdapter recommentsAdapter){

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recommentsAdapter);

    }

    @Override
    public int getItemCount() {
        return comments.size();
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

    public void RecommentDialog(CommentsAdapter.CommentsHolder commentsHolder){
        AlertDialog.Builder oDialog = new AlertDialog.Builder(activity,
                android.R.style.Theme_DeviceDefault_Light_Dialog);

        oDialog.setMessage("대댓글을 작성하시겠습니까?").setPositiveButton("예", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        activity.ShowKeyPad();
                        listener_commentHolder.onClickedholder(commentsHolder,Write_Recomment); //리스너의 응답으로써 postactivity로 해당 댓글의 holder를 넘겨준다.
                        On_CommentbodyColor(commentsHolder);
                    }
                })
                .setNeutralButton("아니오", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                    }
                })
                .show();
    }

    public void On_CommentbodyColor(CommentsAdapter.CommentsHolder commentsHolder){
        commentsHolder.commentbody.setBackgroundResource(R.drawable.get_nonline_selectedcolor);
        commentsHolder.commentbody_goodframe.setBackgroundResource(R.drawable.get_outline_rec_selectedcolor); //색을 똑같이 맞추면 다르게 보임.
    }

    public void Off_CommentbodyColor(CommentsAdapter.CommentsHolder commentsHolder){
        commentsHolder.commentbody.setBackgroundResource(R.drawable.get_nonline);
        commentsHolder.commentbody_goodframe.setBackgroundResource(R.drawable.get_outline_rec);
    }

    public void Showkeyboard(){
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @Override
    public void backpressed() {
 //todo 포스트에서 뒤로가기 버튼을 누르면 선택된 홀더를 취소하려고 리스터를 만들었는데 필요없어지면 삭제예정
    }

    public boolean isposter(int position){

        if(comments.get(position).getId().equals(postInfo.getId()))
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

