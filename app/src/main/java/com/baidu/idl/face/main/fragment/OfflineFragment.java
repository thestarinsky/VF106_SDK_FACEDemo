package com.baidu.idl.face.main.fragment;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.idl.face.main.activity.start.HomeActivity;
import com.baidu.idl.face.main.activity.start.StartSettingActivity;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.callback.Callback;

public class OfflineFragment extends BaseFragment implements View.OnClickListener{
    private TextView accreditDeviceTv;
    private FaceAuth faceAuth;
    private Button accreditOffBtn;
    private TextView accreditHintTv;
    private TextView accreditOffhiteTv;
    @Override
    protected Object getContentLayout() {
        return R.layout.offline_activation_layout;
    }
    @Override
    protected void initView(View view){
        // 激活失败提示
        accreditHintTv =  view.findViewById(R.id.accredit_hintTv);

        // 复制按钮
        faceAuth = new FaceAuth();
        // 复制序列码
        accreditDeviceTv = view.findViewById(R.id.accredit_deviceTv);
        accreditDeviceTv.setText(faceAuth.getDeviceId(getAppActivity()));
        accreditOffBtn = view.findViewById(R.id.accredit_offBtn);
        accreditOffBtn.setOnClickListener(this);
        accreditOffhiteTv = view.findViewById(R.id.accredit_offhiteTv);
        accreditOffhiteTv.setOnClickListener(this);

        // 长按点击复制
        accreditDeviceTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager)
                        getAppActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setText(accreditDeviceTv.getText());
                Log.e("aaaaaaaaa" , accreditDeviceTv.getText().toString());
                ToastUtils.toast(getAppActivity(), "deviceID 复制成功");
                return false;
            }
        });
    }

    private void bangListener() {
        Intent intent = new Intent(getAppActivity(), StartSettingActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            // 离线激活遇到问题
            case R.id.accredit_offhiteTv:
                bangListener();
                break;
            // 离线激活
            case R.id.accredit_offBtn:
                faceAuth.initLicenseOffLine(getAppActivity(), new Callback() {
                    @Override
                    public void onResponse(final int code, final String response) {
                        if (code == 0) {
                            getAppActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 授权成功跳转功能入口页面
                                    accreditHintTv.setText("");
                                    startActivity(new Intent(getAppActivity(), HomeActivity.class));
//                                    finish();
                                }
                            });
                        } else {
                            getAppActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (code == 7) {
                                        accreditHintTv.setText("激活失败，设备硬件指纹与License.zip不符");
                                    } else if (code == 11) {
                                        accreditHintTv.setText("激活失败，License.zip文件对应的序列号不在有效期范围内");
                                    } else if (code == -1) {
                                        accreditHintTv.setText("未检测到License.zip文件");
                                    } else if (code == 14) {
                                        accreditHintTv.setText("激活失败，License.zip文件对应的序列号不在有效期范围内");
                                    } else if (code == 4) {
                                        accreditHintTv.setText("激活失败，设备硬件指纹与License.zip不符");
                                    } else {
                                        accreditHintTv.setText(code);
                                    }
//                                    isTrue = false;
//                                    SharedPreferences sharedPreferences = getSharedPreferences("ws", MODE_PRIVATE);
//                                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                                    editor.putBoolean("accredit", isTrue);
                                }
                            });

                        }
                    }
                });
//                if (System.currentTimeMillis() - lastClickTime < FAST_CLICK_DELAY_TIME) {
//                    count++;
//                }
//                lastClickTime = System.currentTimeMillis();
//
//                if (count == 3) {
//                    popupWindow.showAsDropDown(accredit_offhiteTv, -15, 10);
//                    count = 0;
//                    initHandler();
//                    accredit_offhiteTv.setTextColor(Color.parseColor("#00BAF2"));
//                }
                break;
        }
    }
}
