package com.baidu.idl.face.main.finance.setting;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import com.baidu.idl.face.main.finance.activity.BaseActivity;
import com.baidu.idl.face.main.finance.model.SingleBaseConfig;
import com.baidu.idl.face.main.finance.utils.FinanceConfigUtils;
import com.baidu.idl.face.main.finance.utils.PWTextUtils;
import com.baidu.idl.main.facesdk.financelibrary.R;


/**
 * 镜像调节页面
 * Created by v_liujialu01 on 2019/6/17.
 */

public class MirrorSettingActivity extends BaseActivity implements View.OnClickListener {
    private Switch switchDetectFrame;
    private ImageView mButtonMirrorSave;
    private int zero = 0;
    private int one = 1;
    public static final int cancle = 404;

    private LinearLayout linerDetectMirror;
    private TextView tvDetectMirror;
    private Button cwDetectMirror;

    private String msgTag = "";

    private LinearLayout mirrorRepresent;
    private int showWidth;
    private int showXLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mirror_setting);

        initView();
        initData();
    }

    private void initView() {
        mirrorRepresent = findViewById(R.id.mirrorRepresent);

        switchDetectFrame = findViewById(R.id.switch_detect_frame);
        mButtonMirrorSave = findViewById(R.id.button_mirror_save);

        linerDetectMirror = findViewById(R.id.linerdetectmirror);
        tvDetectMirror = findViewById(R.id.tvdetectmirror);
        cwDetectMirror = findViewById(R.id.cwdetectmirror);


        mButtonMirrorSave.setOnClickListener(this);

        PWTextUtils.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDismiss() {
                cwDetectMirror.setBackground(getDrawable(R.mipmap.icon_setting_question));
            }
        });

        cwDetectMirror.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                if (msgTag.equals(getString(R.string.cw_detectframe))) {
                    msgTag = "";
                    return;
                }
                msgTag = getString(R.string.cw_detectframe);
                cwDetectMirror.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
                PWTextUtils.showDescribeText(linerDetectMirror, tvDetectMirror, MirrorSettingActivity.this,
                        getString(R.string.cw_detectframe), showWidth, showXLocation);
            }
        });
    }

    private void initData() {

        if (SingleBaseConfig.getBaseConfig().getRgbRevert()) {
            switchDetectFrame.setChecked(true);
        } else {
            switchDetectFrame.setChecked(false);
        }

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.button_mirror_save) {
            if (switchDetectFrame.isChecked()) {
                SingleBaseConfig.getBaseConfig().setRgbRevert(true);
            } else {
                SingleBaseConfig.getBaseConfig().setRgbRevert(false);
            }

            FinanceConfigUtils.modityJson();
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        showWidth = mirrorRepresent.getWidth();
        showXLocation = (int) mirrorRepresent.getX();
    }
}
