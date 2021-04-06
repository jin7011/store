package com.example.myapplication1;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Spinner customSpinner;
    private ArrayList<CustomItem> customList;
    private int width=150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        customSpinner = findViewById(R.id.customIconSpinner);
        customList = getCustomList();
        CustomAdapter adapter = new CustomAdapter(this, customList);
        if (customSpinner !=null) {
            customSpinner.setAdapter(adapter);
            customSpinner.setOnItemSelectedListener(this);
        }
    }

    private ArrayList<CustomItem> getCustomList() {
        String[] locations = getResources().getStringArray(R.array.Spinner_Items); //value.string에 들어있는 array 이곳에서 붙여넣어줬습니다.
        customList=new ArrayList<>();

        for(String location : locations){ //들어있는 value값만큼 새로운 arrayList에 넣었습니다.
            customList.add(new CustomItem(location,R.drawable.ic_baseline_radio_button_checked_24));
        }

        return customList;
    }

    @Override
    public void onItemSelected(AdapterView<?>adapterView, View view, int position, long id) {
        try {
            LinearLayout linnerLayout=findViewById(R.id.customSpinnerItemLayout);
            width=linnerLayout.getWidth();
        } catch (Exception e) {
        }
        customSpinner.setDropDownWidth(width);
       CustomItem item =(CustomItem) adapterView.getSelectedItem();
       Toast.makeText(this,item.getSpinnerItemName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}