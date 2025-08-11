package com.baidu.idl.face.main.activity.start;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.activity.FaceSDKManager;
import com.baidu.idl.face.main.finance.utils.FinanceConfigUtils;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.activity.gate.FaceDepthGateActivity;
import com.baidu.idl.main.facesdk.activity.gate.FaceNIRGateActivriy;
import com.baidu.idl.main.facesdk.activity.gate.FaceRGBGateActivity;
import com.baidu.idl.main.facesdk.activity.gate.FaceRgbNirDepthGataActivity;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.AttendanceConfigUtils;
import com.baidu.idl.main.facesdk.identifylibrary.utils.IdentifyConfigUtils;
import com.baidu.idl.main.facesdk.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.RegisterConfigUtils;
import com.baidu.idl.main.facesdk.utils.GateConfigUtils;

import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends BaseActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mContext = this;
        boolean isConfigExit = GateConfigUtils.isConfigExit(this);
        boolean isInitConfig = GateConfigUtils.initConfig();
        boolean isAttendanceConfigExit = AttendanceConfigUtils.isConfigExit(this);
        boolean isAttendanceInitConfig = AttendanceConfigUtils.initConfig();
        boolean isIdentifyConfigExit = IdentifyConfigUtils.isConfigExit(this);
        boolean isIdentifyInitConfig = IdentifyConfigUtils.initConfig();
        boolean isRegisterConfigExit = RegisterConfigUtils.isConfigExit(this);
        boolean isRegisterInitConfig = RegisterConfigUtils.initConfig();
        boolean isFinanceConfigExit = FinanceConfigUtils.isConfigExit(this);
        boolean isFinanceInitConfig = FinanceConfigUtils.initConfig();
        if (isInitConfig && isConfigExit
                && isAttendanceInitConfig && isAttendanceConfigExit
                && isIdentifyInitConfig && isIdentifyConfigExit
                && isRegisterInitConfig && isRegisterConfigExit
                && isFinanceInitConfig && isFinanceConfigExit) {
            Toast.makeText(StartActivity.this, "初始配置加载成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(StartActivity.this, "初始配置失败,将重置文件内容为默认配置", Toast.LENGTH_SHORT).show();
            GateConfigUtils.modityJson();
            AttendanceConfigUtils.modityJson();
            IdentifyConfigUtils.modityJson();
            RegisterConfigUtils.modityJson();
            FinanceConfigUtils.modityJson();
        }
        initLicense();
    }
    private void initLicense() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//
//            }
//        }).start();
//        findViewById(R.id.abut).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent(StartActivity.this, ActivitionTwoctivity.class);
//                startActivity(intent);
//            }
//        });
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//
//                Intent intent = new Intent(StartActivity.this, Activitionctivity.class);
//                startActivity(intent);
//                finish();
//            }
//        };
//        Timer timer = new Timer();
//        timer.schedule(task, 500);
        FaceSDKManager.getInstance().init(mContext, new SdkInitListener() {
            @Override
            public void initStart() {

            }

            @Override
            public void initLicenseSuccess() {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 闸机模块
                                /*judgeLiveType(1,
                                        FaceRGBGateActivity.class,
                                        FaceNIRGateActivriy.class,
                                        FaceDepthGateActivity.class,
                                        FaceRgbNirDepthGataActivity.class);*/

                                startActivity(new Intent(mContext, HomeActivity.class));
                                finish();
                            }
                        });
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 2000);
            }

            @Override
            public void initLicenseFail(int errorCode, String msg) {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                startActivity(new Intent(mContext, Activitionctivity.class));
                                finish();
                            }
                        });
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 2000);
            }

            @Override
            public void initModelSuccess() {

            }

            @Override
            public void initModelFail(int errorCode, String msg) {

            }
        }/*new SdkInitListener() {
            @Override
            public void initStart() {

            }

            public void initLicenseSuccess() {

                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {


                                startActivity(new Intent(mContext, HomeActivity.class));
                                finish();
                            }
                        });
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 2000);
            }

            @Override
            public void initLicenseFail(int errorCode, String msg) {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                startActivity(new Intent(mContext, Activitionctivity.class));
                                finish();
                            }
                        });
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 2000);
            }

            @Override
            public void initModelSuccess() {
            }

            @Override
            public void initModelFail(int errorCode, String msg) {

            }
        }*/);
    }

    private void judgeLiveType(int type, Class<?> rgbCls, Class<?> nirCls, Class<?> depthCls, Class<?> rndCls) {
        switch (type) {
            case 0: { // 不使用活体
                startActivity(new Intent(this, rgbCls));
                break;
            }

            case 1: { // RGB活体
                startActivity(new Intent(this, rgbCls));
                break;
            }

            case 2: { // NIR活体
                startActivity(new Intent(this, nirCls));
                break;
            }

            case 3: { // 深度活体
                int cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
                judgeCameraType(cameraType, depthCls);
                break;
            }

            case 4: { // rgb+nir+depth活体
                int cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
                judgeCameraType(cameraType, rndCls);
            }
        }
    }

    private void judgeCameraType(int cameraType, Class<?> depthCls) {
        switch (cameraType) {
            case 1: { // pro
                startActivity(new Intent(this, depthCls));
                break;
            }

            case 2: { // atlas
                startActivity(new Intent(this, depthCls));
                break;
            }

            case 6: { // Pico
                //  startActivity(new Intent(HomeActivity.this,
                // PicoFaceDepthLivenessActivity.class));
                break;
            }

            default:
                startActivity(new Intent(this, depthCls));
                break;
        }
    }
}
