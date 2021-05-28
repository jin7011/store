package com.example.sns_project.Adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.R;
import com.example.sns_project.info.LetterInfo;
import com.example.sns_project.util.LetterInfo_DiffUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.sns_project.CustomLibrary.PostControler.MessageTime_to_String;
import static com.example.sns_project.util.Named.HOUR;
import static com.example.sns_project.util.Named.MIN;
import static com.example.sns_project.util.Named.MINE;
import static com.example.sns_project.util.Named.OTHER;
import static com.example.sns_project.util.Named.SEC;

public class LetterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<LetterInfo> letters;
    private Activity activity;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private PostControler postControler = new PostControler();

    private boolean NoMore_Load = false;
    private int visibleThreshold = 1;
    int firstVisibleItem, visibleItemCount, totalItemCount, lastVisibleItem;
    private LinearLayoutManager mLinearLayoutManager;
    private OnLoadMoreListener_top onLoadMoreListener;

    public interface OnLoadMoreListener_top{
        void onLoadMore();
    }

    public void setLinearLayoutManager(LinearLayoutManager linearLayoutManager){
        this.mLinearLayoutManager=linearLayoutManager;
    }

    public LetterAdapter(Activity activity,OnLoadMoreListener_top listener_top){
        this.activity = activity;
        this.letters = new ArrayList<>();
        this.onLoadMoreListener = listener_top;
    }

    public void LetterInfoDiffUtil(ArrayList<LetterInfo> NewLetters) {
        final LetterInfo_DiffUtil diffCallback = new LetterInfo_DiffUtil(this.letters, NewLetters);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        letters.clear();
        letters.addAll(NewLetters);

        diffResult.dispatchUpdatesTo(this);
    }

    //holder
    public static class Other_Holder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함
        //R.layout.item_comments에 존재하지 않는 뷰는 일반적으로는 설정하나마나임
        TextView other_msg ;
        TextView other_time ;
        TextView other_nick;

        public Other_Holder (@NonNull View itemView) {
            super(itemView);
            other_time = itemView.findViewById(R.id.other_time);
            other_msg = itemView.findViewById(R.id.other_msg);
            other_nick = itemView.findViewById(R.id.other_nick);
        }
    }

    //holder
    public static class My_Holder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함
        //R.layout.item_comments에 존재하지 않는 뷰는 일반적으로는 설정하나마나임
        TextView my_msg ;
        TextView my_time ;

        public My_Holder (@NonNull View itemView) {
            super(itemView);
            my_time = itemView.findViewById(R.id.my_time);
            my_msg = itemView.findViewById(R.id.my_msg);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == MINE){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_letter_others, parent, false);
            LetterAdapter.Other_Holder other = new LetterAdapter.Other_Holder(view);
            return other;
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_letter_mine, parent, false);
            LetterAdapter.My_Holder mine = new LetterAdapter.My_Holder(view);
            return mine;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        LetterInfo letter = letters.get(position);
        Date date = new Date();

        if(holder instanceof Other_Holder){
            Other_Holder otherHolder = (Other_Holder)holder;
            otherHolder.other_nick.setText(letter.getSender_nick());
            otherHolder.other_msg.setText(letter.getContents());
            otherHolder.other_time.setText(MessageTime_to_String(letter.getCreatedAt(),date));
//            Log.d("acdksld",letter.getCreatedAt()+" cr");
        }

        if(holder instanceof My_Holder){
            My_Holder myHolder = (My_Holder)holder;
            myHolder.my_msg.setText(letter.getContents());
            myHolder.my_time.setText(MessageTime_to_String(letter.getCreatedAt(),date));
        }

    }

    @Override
    public int getItemCount() {
        return letters.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(letters.get(position).getReciever_id().equals(user.getUid()))
            return MINE;
        else
            return OTHER;
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
                if (!NoMore_Load && firstVisibleItem == 0 && totalItemCount != 0 && onLoadMoreListener != null) {
                    onLoadMoreListener.onLoadMore();
                    NoMore_Load = true;
                }
            }
        });
    }

    public void Set_ReadMore(boolean b){
        this.NoMore_Load = b;
    }

}
