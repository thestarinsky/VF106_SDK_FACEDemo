package com.baidu.idl.main.facesdk.identifylibrary.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.identifylibrary.BaseActivity;
import com.baidu.idl.main.facesdk.identifylibrary.R;
import com.baidu.idl.main.facesdk.identifylibrary.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.identifylibrary.utils.PreferencesManager;
import com.baidu.idl.main.facesdk.license.BDFaceLicenseAuthInfo;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IdentifySettingActivity extends BaseActivity implements View.OnClickListener {

    private ImageView gateSetttingBack;
    private LinearLayout gateFaceDetection;
    private LinearLayout gateConfigQualtify;
    private LinearLayout gateHuotiDetection;
    private LinearLayout gateFaceRecognition;
    private LinearLayout gateLensSettings;
    private View gatePictureOptimization;
    private View gateLogSettings;
    private TextView tvSettingQualtify;
    private TextView logSettingQualtify;
    private TextView tvSettingLiviness;
    private LinearLayout configVersionMessage;
    private TextView tvSettingEffectiveDate;
    private FaceAuth faceAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify_setting);
        init();
    }

    private void init() {
        faceAuth = new FaceAuth();
        // 返回
        gateSetttingBack = findViewById(R.id.gate_settting_back);
        gateSetttingBack.setOnClickListener(this);
        // 人脸检测
        gateFaceDetection = findViewById(R.id.gate_face_detection);
        gateFaceDetection.setOnClickListener(this);
        // 质量检测
        gateConfigQualtify = findViewById(R.id.gate_config_qualtify);
        gateConfigQualtify.setOnClickListener(this);
        // 活体检测
        gateHuotiDetection = findViewById(R.id.gate_huoti_detection);
        gateHuotiDetection.setOnClickListener(this);
        // 人脸识别
        gateFaceRecognition = findViewById(R.id.gate_face_recognition);
        gateFaceRecognition.setOnClickListener(this);
        // 镜头设置
        gateLensSettings = findViewById(R.id.gate_lens_settings);
        gateLensSettings.setOnClickListener(this);
        // 图像优化
        gatePictureOptimization = findViewById(R.id.gate_picture_optimization);
        gatePictureOptimization.setOnClickListener(this);
        // 日志设置
        gateLogSettings = findViewById(R.id.gate_log_settings);
        gateLogSettings.setOnClickListener(this);
        findViewById(R.id.cpu_settings).setOnClickListener(this);
        // 版本信息
        configVersionMessage = findViewById(R.id.configVersionMessage);
        configVersionMessage.setOnClickListener(this);
        tvSettingQualtify = findViewById(R.id.tvSettingQualtify);
        logSettingQualtify = findViewById(R.id.logSettingQualtify);
        tvSettingLiviness = findViewById(R.id.tvSettingLiviness);

        tvSettingEffectiveDate = findViewById(R.id.tvSettingEffectiveDate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SingleBaseConfig.getBaseConfig().isLog()) {
            logSettingQualtify.setText("开启");
        } else {
            logSettingQualtify.setText("关闭");
        }
        if (SingleBaseConfig.getBaseConfig().isQualityControl()) {
            tvSettingQualtify.setText("开启");
        } else {
            tvSettingQualtify.setText("关闭");
        }
        if (SingleBaseConfig.getBaseConfig().isLivingControl()) {
            tvSettingLiviness.setText("开启");
        } else {
            tvSettingLiviness.setText("关闭");
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        BDFaceLicenseAuthInfo bdFaceLicenseAuthInfo = faceAuth.getAuthInfo(this);
        Date dateLong = new Date(bdFaceLicenseAuthInfo.expireTime * 1000L);
        String dateTime = simpleDateFormat.format(dateLong);
        if ("2037-01-01".equals(dateTime) || "1970-01-01".equals(dateTime)){

            tvSettingEffectiveDate.setVisibility(View.GONE);
        }else {

            tvSettingEffectiveDate.setText("有效期至" + dateTime);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.gate_settting_back) {
            PreferencesManager.getInstance(this.getApplicationContext())
                    .setType(SingleBaseConfig.getBaseConfig().getType());
            finish();
        } else if (id == R.id.gate_face_detection) {
            Intent intent = new Intent(IdentifySettingActivity.this, IdentifyMinFaceActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_config_qualtify) {
            Intent intent = new Intent(IdentifySettingActivity.this, IdentifyConfigQualtifyActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_huoti_detection) {
            Intent intent = new Intent(IdentifySettingActivity.this, FaceLivinessTypeActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_face_recognition) {
            Intent intent = new Intent(IdentifySettingActivity.this, IdentifyFaceDetectActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_lens_settings) {
            Intent intent = new Intent(IdentifySettingActivity.this, IdentifyLensSettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.configVersionMessage) {
            Intent intent = new Intent(IdentifySettingActivity.this, VersionMessageActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_picture_optimization){
            Intent intent = new Intent(IdentifySettingActivity.this, IdentifyPictureOptimizationActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_log_settings) {
            Intent intent = new Intent(IdentifySettingActivity.this, IdentifyLogSettingActivity.class);
            startActivity(intent);
        } else if (id == R.id.cpu_settings){
            Intent intent = new Intent(IdentifySettingActivity.this, CpuUpActivity.class);
            startActivity(intent);
        }
    }
}