package com.unipi.danaeb.myagenda;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class NewEventActivity extends AppCompatActivity {

    Dialog myDialog;
    Button color_bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        myDialog = new Dialog(this);
    }

    public void ShowPopup(View v) {
        myDialog.setContentView(R.layout.custompopup);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();

        RadioGroup radioGroup = (RadioGroup) myDialog.findViewById(R.id.RadioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioButtonID = radioGroup.getCheckedRadioButtonId(); // get selected radio button from radioGroup
                View radioButton = radioGroup.findViewById(radioButtonID); // find the radiobutton by returned id
                int radioId = radioGroup.indexOfChild(radioButton);
                RadioButton btn = (RadioButton) radioGroup.getChildAt(radioId);
                String selection = (String) btn.getText();
                if (selection == "Purple"){
                    color_bt.setBackgroundColor(color_bt.getContext().getResources().getColor(R.color.Purple));
                } else if (selection == "Red"){
                    color_bt.setBackgroundColor(color_bt.getContext().getResources().getColor(R.color.Red));
                } else if (selection == "Green"){
                    color_bt.setBackgroundColor(color_bt.getContext().getResources().getColor(R.color.Green));
                } else if (selection == "Teal"){
                    color_bt.setBackgroundColor(color_bt.getContext().getResources().getColor(R.color.Teal));
                } else if (selection == "Black"){
                    color_bt.setBackgroundColor(color_bt.getContext().getResources().getColor(R.color.Black));
                } else if (selection == "White"){
                    color_bt.setBackgroundColor(color_bt.getContext().getResources().getColor(R.color.White));
                }
                myDialog.dismiss();
            }
        });
    }
}