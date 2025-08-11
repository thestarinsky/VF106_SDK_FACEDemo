package com.baidu.idl.main.facesdk.attendancelibrary.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.baidu.idl.main.facesdk.attendancelibrary.BaseActivity;
import com.baidu.idl.main.facesdk.attendancelibrary.R;
import com.baidu.idl.main.facesdk.attendancelibrary.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.AttendanceConfigUtils;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.RegisterConfigUtils;

public class CpuUpActivity extends BaseActivity {
    private CheckBox[] high;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpu_up);
        initView();
    }
    private void initView(){
        CheckBox highBox = findViewById(R.id.fls_high);
        CheckBox inBox = findViewById(R.id.fls_in);
        CheckBox lowBox = findViewById(R.id.fls_low);
        high = new CheckBox[]{highBox ,
                inBox ,
                lowBox
        };

        for (int i = 0 , k = high.length; i < k ; i++){
            high[i].setClickable(false);
            if (i == SingleBaseConfig.getBaseConfig().getCpuUp()){
                high[i].setChecked(true);
            }
        }
        View highGroup = findViewById(R.id.high_group);
        highGroup.setTag(0);
        highGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCheckedChange((int) view.getTag());
            }
        });
        View inGroup = findViewById(R.id.in_group);
        inGroup.setTag(1);
        inGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCheckedChange((int) view.getTag());
            }
        });
        View lowGroup = findViewById(R.id.low_group);
        lowGroup.setTag(2);
        lowGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCheckedChange((int) view.getTag());
            }
        });

        findViewById(R.id.fls_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0 , k = high.length; i < k ; i++){
                    if (high[i].isChecked()){
                        SingleBaseConfig.getBaseConfig().setCpuUp(i);
                        AttendanceConfigUtils.modityJson();
                        RegisterConfigUtils.modityJson();
                        break;
                    }
                }
                finish();
            }
        });
    }
    private void setCheckedChange(int tag){
                for (int i = 0 , k = high.length ; i < k; i++){
                    if (i != tag){
                        high[i].setChecked(false);
                    }else {
                        high[i].setChecked(true);
                    }
                }
    }
}
