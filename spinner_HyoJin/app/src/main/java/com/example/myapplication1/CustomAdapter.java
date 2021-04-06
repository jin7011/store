package com.example.myapplication1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<CustomItem> {
    public CustomAdapter(@NonNull Context context, ArrayList<CustomItem> customList) {
        super(context, 0, customList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //todo (플랜A) 맨 처음 아무것도 선택을 안했을 시에 보여지는 view에서 글자는 "지역을 선택해주세요." 였으면 좋겠습니다. (array에서 "지역을 선택해주세요"를 빼고)
        //todo (플랜B) 이것저것해보다가 스피너로는 기술적으로 한계가 있다고 생각이 드신다면 array에 이전처럼  "지역을 선택해주세요"를 넣고 쉽게 처리하는 것은 좋으나, 그것을 선택할 수는 없게끔 조건처리 해주시면 좋겠습니다.
        //todo 현재 레이아웃 색상은 제가 실험삼아 저희 어플시그니쳐색상으로 바꿔봤는데 천천히 수정해봐야 겠습니다만 크게 신경안쓰셔도 됩니다~ 뭔가 잘어울릴 것 같은 색상이 있다면 추천부탁드립니다. 
        if(convertView==null){
           convertView= LayoutInflater.from(getContext()).inflate(R.layout.custom_spinner_layout,parent,false);
            }

       CustomItem item = getItem(position);
        ImageView spinnerIV=convertView.findViewById(R.id.ivSpinnerLayout);
        TextView spinnerTV=convertView.findViewById(R.id.tvSpinnerLayout);
        if(item != null){
            spinnerIV.setImageResource(item.getSpinnerItemImage());
            spinnerTV.setText(item.getSpinnerItemName());
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.custom_dropdown_layout,parent,false);
        }

        CustomItem item = getItem(position);
        ImageView dropDownIV=convertView.findViewById(R.id.ivDropDownLayout);
        TextView dropDownTV=convertView.findViewById(R.id.tvDropDownLayout);
        if(item != null){
            dropDownIV.setImageResource(item.getSpinnerItemImage());
            dropDownTV.setText(item.getSpinnerItemName());
        }

        return convertView;
    }
}
