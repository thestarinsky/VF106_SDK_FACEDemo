package com.baidu.idl.main.facesdk.identifylibrary.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.FaceCrop;
import com.baidu.idl.main.facesdk.FaceDarkEnhance;
import com.baidu.idl.main.facesdk.FaceDetect;
import com.baidu.idl.main.facesdk.FaceFeature;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.FaceLive;
import com.baidu.idl.main.facesdk.FaceMouthMask;
import com.baidu.idl.main.facesdk.ImageIllum;
import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.identifylibrary.callback.FaceDetectCallBack;
import com.baidu.idl.main.facesdk.identifylibrary.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.identifylibrary.model.DriverInfo;
import com.baidu.idl.main.facesdk.identifylibrary.model.GlobalSet;
import com.baidu.idl.main.facesdk.identifylibrary.model.LivenessModel;
import com.baidu.idl.main.facesdk.identifylibrary.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.identifylibrary.utils.ToastUtils;
import com.baidu.idl.main.facesdk.model.BDFaceCropParam;
import com.baidu.idl.main.facesdk.model.BDFaceDetectListConf;
import com.baidu.idl.main.facesdk.model.BDFaceDriverMonitorInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceInstance;
import com.baidu.idl.main.facesdk.model.BDFaceOcclusion;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.model.BDFaceSDKConfig;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.db.DBManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;


public class FaceSDKManager {

    public static final int SDK_MODEL_LOAD_SUCCESS = 0;
    public static final int SDK_UNACTIVATION = 1;
    public static final int SDK_UNINIT = 2;
    public static final int SDK_INITING = 3;
    public static final int SDK_INITED = 4;
    public static final int SDK_INIT_FAIL = 5;
    public static final int SDK_INIT_SUCCESS = 6;

    private float threholdScore;

    private List<Boolean> mRgbLiveList=new ArrayList<>();
    private List<Boolean> mNirLiveList=new ArrayList<>();
    private float mRgbLiveScore;
    private float mNirLiveScore;
    private int mLastFaceId;

    private DriverInfo driverInfo;
    private BDFaceDriverMonitorInfo bdFaceDriverMonitorInfo;

    public static volatile int initStatus = SDK_UNACTIVATION;
    public static volatile boolean initModelSuccess = false;
    private FaceAuth faceAuth;
    private FaceDetect faceDetect;
    private FaceFeature faceFeature;
    private FaceLive faceLiveness;
    private FaceCrop faceCrop;

    private ExecutorService es = Executors.newSingleThreadExecutor();
    private Future future;
    private ExecutorService es2 = Executors.newSingleThreadExecutor();
    private Future future2;
    private ExecutorService es3 = Executors.newSingleThreadExecutor();
    private Future future3;

    private ExecutorService mRegExecutorService = Executors.newSingleThreadExecutor();
    private Future mRegFuture;

    private FaceDetect faceDetectNir;
    private float[] scores;
    public static boolean isDetectMask = false;
    private boolean mIsCropFace;
    private FaceDetect faceDetectPerson;
    private FaceFeature faceFeaturePerson;
    private ImageIllum imageIllum;

    private static int failNumber = 0;
    private static int faceId = 0;
    private static int lastFaceId = 0;
    private static LivenessModel faceAdoptModel;
    private boolean isFail = true;
    private long trackTime;

    private FaceSDKManager() {
        faceAuth = new FaceAuth();
        setActiveLog();
        faceAuth.setCoreConfigure(BDFaceSDKCommon.BDFaceCoreRunMode.BDFACE_LITE_POWER_LOW, 1);

//        faceDetect = new FaceDetect();
//        faceFeature = new FaceFeature();
//        faceLiveness = new FaceLive();

    }

    public void setActiveLog(){
        if (faceAuth != null){
            if (SingleBaseConfig.getBaseConfig().isLog()){
                faceAuth.setActiveLog(BDFaceSDKCommon.BDFaceLogInfo.BDFACE_LOG_TYPE_ALL, 1);
            }else {
                faceAuth.setActiveLog(BDFaceSDKCommon.BDFaceLogInfo.BDFACE_LOG_TYPE_ALL, 0);
            }
        }
    }

    private static class HolderClass {
        private static final FaceSDKManager instance = new FaceSDKManager();
    }

    public static FaceSDKManager getInstance() {
        return HolderClass.instance;
    }

    public FaceDetect getFaceDetect() {
        return faceDetect;
    }

    public FaceFeature getFaceFeature() {
        return faceFeature;
    }

    public FaceLive getFaceLiveness() {
        return faceLiveness;
    }

    public FaceCrop getFaceCrop() {
        return faceCrop;
    }

    public void setCropFace(boolean isCropFace) {
        mIsCropFace = isCropFace;
    }

    public boolean getCropFace() {
        return mIsCropFace;
    }

    public ImageIllum getImageIllum() {
        return imageIllum;
    }

    /**
     * 初始化鉴权，如果鉴权通过，直接初始化模型
     *
     * @param context
     * @param listener
     */
    public void init(final Context context, final SdkInitListener listener) {

        PreferencesUtil.initPrefs(context.getApplicationContext());
        final String licenseOfflineKey = PreferencesUtil.getString("activate_offline_key", "");
        final String licenseOnlineKey = PreferencesUtil.getString("activate_online_key", "");
        final String licenseBatchlineKey = PreferencesUtil.getString("activate_batchline_key", "");

        // 如果licenseKey 不存在提示授权码为空，并跳转授权页面授权
        if (TextUtils.isEmpty(licenseOfflineKey) && TextUtils.isEmpty(licenseOnlineKey)
                && TextUtils.isEmpty(licenseBatchlineKey)) {
            ToastUtils.toast(context, "未授权设备，请完成授权激活");
            if (listener != null) {
                listener.initLicenseFail(-1, "授权码不存在，请重新输入！");
            }
            return;
        }
        // todo 增加判空处理
        if (listener != null) {
            listener.initStart();
        }

        if (!TextUtils.isEmpty(licenseOnlineKey)) {
            // 在线激活
            faceAuth.initLicenseOnLine(context, licenseOnlineKey, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    if (code == 0) {
                        initStatus = SDK_INIT_SUCCESS;
                        if (listener != null) {
                            listener.initLicenseSuccess();
                        }
//                        initModel(context, listener);
                        return;
                    } else {
                        listener.initLicenseFail(code, response);
                    }
                }
            });
        } else if (!TextUtils.isEmpty(licenseOfflineKey)) {
            // 离线激活
            faceAuth.initLicenseOffLine(context, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    if (code == 0) {
                        initStatus = SDK_INIT_SUCCESS;
                        if (listener != null) {
                            listener.initLicenseSuccess();
                        }
//                        initModel(context, listener);
                        return;
                    } else {
                        listener.initLicenseFail(code, response);
                    }
                }
            });
        } else if (!TextUtils.isEmpty(licenseBatchlineKey)) {
            // 应用激活
            faceAuth.initLicenseBatchLine(context, licenseBatchlineKey, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    if (code == 0) {
                        initStatus = SDK_INIT_SUCCESS;
                        if (listener != null) {
                            listener.initLicenseSuccess();
                        }
//                        initModel(context, listener);
                        return;
                    } else {
                        listener.initLicenseFail(code, response);
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.initLicenseFail(-1, "授权码不存在，请重新输入！");
            }
        }
    }

    /**
     * 初始化模型，目前包含检查，活体，识别模型；因为初始化是顺序执行，可以在最好初始化回掉中返回状态结果
     *
     * @param context
     * @param listener
     */
    public void initModel(final Context context, final SdkInitListener listener) {
//      ToastUtils.toast(context, "模型初始化中，请稍后片刻");

        // 默认检测
        BDFaceInstance bdFaceInstance = new BDFaceInstance();
        bdFaceInstance.creatInstance();
        faceDetect = new FaceDetect(bdFaceInstance);
        // 默认识别
        faceFeature = new FaceFeature();
        faceLiveness = new FaceLive();
        faceCrop = new FaceCrop();
        // 曝光
        imageIllum = new ImageIllum();


        // 红外检测
        BDFaceInstance IrBdFaceInstance = new BDFaceInstance();
        IrBdFaceInstance.creatInstance();
        faceDetectNir = new FaceDetect(IrBdFaceInstance);

        // 认证检测
        BDFaceInstance faceInstancePerson = new BDFaceInstance();
        faceInstancePerson.creatInstance();
        faceDetectPerson = new FaceDetect(faceInstancePerson);

        // 认证识别
        faceFeaturePerson = new FaceFeature(faceInstancePerson);

        initConfig();

        mRgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        mNirLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();

        final long startInitModelTime = System.currentTimeMillis();

        faceDetect.initModel(context,
                GlobalSet.DETECT_VIS_MODEL,
                GlobalSet.DETECT_PRIOR_BOX,
                GlobalSet.ALIGN_TRACK_MODEL,
                BDFaceSDKCommon.DetectType.DETECT_VIS,
                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_FAST,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceDetect.initModel(context,
                GlobalSet.DETECT_VIS_MODEL,
                GlobalSet.DETECT_PRIOR_BOX,
                GlobalSet.ALIGN_RGB_MODEL, BDFaceSDKCommon.DetectType.DETECT_VIS,
                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_ACCURATE,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        //  ToastUtils.toast(context, code + "  " + response);
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceDetectNir.initModel(context,
                GlobalSet.DETECT_NIR_MODE,
                GlobalSet.DETECT_PRIOR_BOX,
                GlobalSet.ALIGN_NIR_MODEL, BDFaceSDKCommon.DetectType.DETECT_NIR,
                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_NIR_ACCURATE,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        //  ToastUtils.toast(context, code + "  " + response);
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });
        faceDetect.initQuality(context,
                GlobalSet.BLUR_MODEL,
                GlobalSet.OCCLUSION_MODEL, new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceDetect.initBestImage(context, GlobalSet.BEST_IMAGE, new Callback() {
            @Override
            public void onResponse(int code, String response) {
                if (code != 0 && listener != null) {
                    listener.initModelFail(code, response);
                }
            }
        });

        faceDetect.initAttrEmo(context, GlobalSet.ATTRIBUTE_MODEL, GlobalSet.EMOTION_MODEL, new Callback() {
            @Override
            public void onResponse(int code, String response) {
                if (code != 0 && listener != null) {
                    listener.initModelFail(code, response);
                }
            }
        });

        faceDetectPerson.initQuality(context,
                GlobalSet.BLUR_MODEL,
                GlobalSet.OCCLUSION_MODEL, new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceDetectPerson.initModel(context,
                GlobalSet.DETECT_VIS_MODEL,
                GlobalSet.DETECT_PRIOR_BOX,
                GlobalSet.ALIGN_RGB_MODEL, BDFaceSDKCommon.DetectType.DETECT_VIS,
                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_ACCURATE,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        //  ToastUtils.toast(context, code + "  " + response);
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });


        faceLiveness.initModel(context,
                GlobalSet.LIVE_VIS_MODEL,
                GlobalSet.LIVE_NIR_MODEL,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        //  ToastUtils.toast(context, code + "  " + response);
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });


        faceFeaturePerson.initModel(context,
                GlobalSet.RECOGNIZE_IDPHOTO_MODEL,
                GlobalSet.RECOGNIZE_VIS_MODEL,
                GlobalSet.RECOGNIZE_NIR_MODEL,
                GlobalSet.RECOGNIZE_VIS_MODEL,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceFeature.initModel(context,
                GlobalSet.RECOGNIZE_IDPHOTO_MODEL,
                GlobalSet.RECOGNIZE_VIS_MODEL,
                GlobalSet.RECOGNIZE_NIR_MODEL,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        long endInitModelTime = System.currentTimeMillis();
//                        LogUtils.e(TIME_TAG, "init model time = " + (endInitModelTime - startInitModelTime));
                        if (code != 0) {
//                            ToastUtils.toast(context, "模型加载失败,尝试重启试试");
                            if (listener != null) {
                                listener.initModelFail(code, response);
                            }
                        } else {
                            initStatus = SDK_MODEL_LOAD_SUCCESS;
                            // 模型初始化成功，加载人脸数据
                            initDataBases(context);
//                            ToastUtils.toast(context, "模型加载完毕，欢迎使用");
                            if (listener != null) {
                                listener.initModelSuccess();
                            }
                        }
                    }
                });
    }


    /**
     * 初始化配置
     *
     * @return
     */
    public boolean initConfig() {
        if (faceDetect != null && faceDetectPerson != null) {
            BDFaceSDKConfig config = new BDFaceSDKConfig();
            // TODO: 最小人脸个数检查，默认设置为1,用户根据自己需求调整
            config.maxDetectNum = 2;
            config.powerMode = SingleBaseConfig.getBaseConfig().getCpuUp();

            // TODO: 默认为80px。可传入大于30px的数值，小于此大小的人脸不予检测，生效时间第一次加载模型
            config.minFaceSize = 100;
            // TODO: 默认为0.5。可传入大于0.3的数值
            config.notRGBFaceThreshold = 0.85f;
            config.notNIRFaceThreshold = 0.85f;

            // 是否进行属性检测，默认关闭
            config.isAttribute = SingleBaseConfig.getBaseConfig().isAttribute();
//
//            // TODO: 模糊，遮挡，光照三个质量检测和姿态角查默认关闭，如果要开启，设置页启动
            config.isCheckBlur = config.isOcclusion
                    = config.isIllumination = config.isHeadPose
                    = SingleBaseConfig.getBaseConfig().isQualityControl();

            faceDetect.loadConfig(config);
            faceDetectPerson.loadConfig(config);

            return true;
        }
        return false;
    }

    public void initDataBases(Context context) {
        // 初始化数据库
        DBManager.getInstance().init(context);
        // 数据变化，更新内存
        FaceSDKManager.getInstance().initPush();
    }
    /**
     * 数据库发现变化时候，重新把数据库中的人脸信息添加到内存中，id+feature
     */
    public void initPush() {

        if (future3 != null && !future3.isDone()) {
            future3.cancel(true);
        }

        future3 = es3.submit(new Runnable() {
            @Override
            public void run() {
                synchronized (FaceApi.getInstance().getAllUserList()){
                    FaceSDKManager.getInstance().getFaceFeature().featurePush(FaceApi.getInstance().getAllUserList());
                }
            }
        });
    }
    /**
     * 检测-活体-特征-人脸检索流程
     *
     * @param rgbData            可见光YUV 数据流
     * @param nirData            红外YUV 数据流
     * @param depthData          深度depth 数据流
     * @param secondFeature       图片特征值
     * @param srcHeight          可见光YUV 数据流-高度
     * @param srcWidth           可见光YUV 数据流-宽度
     * @param liveCheckMode      活体检测类型【不使用活体：0】；【RGB活体：1】；【RGB+NIR活体：2】；【RGB+Depth活体：3】；【RGB+NIR+Depth活体：4】
     * @param faceDetectCallBack
     */
    public void onDetectCheck(final byte[] rgbData,
                              final byte[] nirData,
                              final byte[] depthData,
                              final byte[] secondFeature,
                              final int srcHeight,
                              final int srcWidth,
                              final int liveCheckMode,
                              final FaceDetectCallBack faceDetectCallBack) {

        // 【【提取特征+1：N检索：3】
        onDetectCheck(rgbData, nirData, depthData, secondFeature , srcHeight,
                srcWidth, liveCheckMode, 3, faceDetectCallBack);
    }

    private void setFail(LivenessModel livenessModel) {
        Log.e("faceId", livenessModel.getFaceInfo().faceID + "");
if (failNumber >= 2) {
    faceId = livenessModel.getFaceInfo().faceID;
    faceAdoptModel = livenessModel;
    trackTime = System.currentTimeMillis();
    isFail = false;
    faceAdoptModel.setMultiFrame(true);
} else {
    failNumber += 1;
    faceId = 0;
    faceAdoptModel = null;
    isFail = true;
    livenessModel.setMultiFrame(true);

}
    }

    public void emptyFrame() {
        failNumber = 0;
        faceId = 0;
        isFail = false;
        trackTime = 0;
        faceAdoptModel = null;
    }


    /**
     * 检测-活体-特征- 全流程
     *
     * @param rgbData            可见光YUV 数据流
     * @param nirData            红外YUV 数据流
     * @param depthData          深度depth 数据流
     * @param secondFeature      图片特征值
     * @param srcHeight          可见光YUV 数据流-高度
     * @param srcWidth           可见光YUV 数据流-宽度
     * @param liveCheckMode      活体检测类型【不使用活体：0】；【RGB活体：1】；【RGB+NIR活体：2】；【RGB+Depth活体：3】；【RGB+NIR+Depth活体：4】
     * @param featureCheckMode   特征抽取模式【不提取特征：1】；【提取特征：2】；【提取特征+1：N检索：3】；
     * @param faceDetectCallBack
     */
    public void onDetectCheck(final byte[] rgbData,
                              final byte[] nirData,
                              final byte[] depthData,
                              final byte[] secondFeature,
                              final int srcHeight,
                              final int srcWidth,
                              final int liveCheckMode,
                              final int featureCheckMode,
                              final FaceDetectCallBack faceDetectCallBack) {

        if (future != null && !future.isDone()) {
            return;
        }

        future = es.submit(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                // 创建检测结果存储数据
                LivenessModel livenessModel = new LivenessModel();
                // 创建检测对象，如果原始数据YUV，转为算法检测的图片BGR
                // TODO: 用户调整旋转角度和是否镜像，手机和开发版需要动态适配
                BDFaceImageInstance rgbInstance;
                if (SingleBaseConfig.getBaseConfig().getType() == 4
                        && SingleBaseConfig.getBaseConfig().getCameraType() == 6) {
                    rgbInstance = new BDFaceImageInstance(rgbData, srcHeight,
                            srcWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_RGB,
                            SingleBaseConfig.getBaseConfig().getRgbDetectDirection(),
                            SingleBaseConfig.getBaseConfig().getMirrorDetectRGB());
                } else {
                    rgbInstance = new BDFaceImageInstance(rgbData, srcHeight,
                            srcWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
                            SingleBaseConfig.getBaseConfig().getRgbDetectDirection(),
                            SingleBaseConfig.getBaseConfig().getMirrorDetectRGB());
                }

                // TODO: getImage() 获取送检图片,如果检测数据有问题，可以通过image view 展示送检图片
                livenessModel.setBdFaceImageInstance(rgbInstance.getImage());

                // 检查函数调用，返回检测结果
                long startDetectTime = System.currentTimeMillis();

                // 快速检测获取人脸信息，仅用于绘制人脸框，详细人脸数据后续获取
                FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetect()
                        .track(BDFaceSDKCommon.DetectType.DETECT_VIS,
                                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_FAST, rgbInstance);
                livenessModel.setRgbDetectDuration(System.currentTimeMillis() - startDetectTime);
//                LogUtils.e(TIME_TAG, "detect vis time = " + livenessModel.getRgbDetectDuration());

                // 检测结果判断
                if (faceInfos != null && faceInfos.length > 0) {
                    livenessModel.setTrackFaceInfo(faceInfos);
                    livenessModel.setFaceInfo(faceInfos[0]);
                    if (lastFaceId != faceInfos[0].faceID) {
                        lastFaceId = faceInfos[0].faceID;
                    }

                    if (System.currentTimeMillis() - trackTime < 3000 && faceId == faceInfos[0].faceID) {
                        faceAdoptModel.setMultiFrame(true);
                        rgbInstance.destory();
                        if (faceDetectCallBack != null && faceAdoptModel != null) {
                            //                            faceAdoptModel.setAllDetectDuration(System.currentTimeMillis() - startTime);
                            faceDetectCallBack.onFaceDetectDarwCallback(livenessModel);
                            faceDetectCallBack.onFaceDetectCallback(faceAdoptModel);

                        }
                        return;
                    }
                    if (faceAdoptModel != null) {
                        faceAdoptModel.setMultiFrame(false);
                    }
                    faceId = 0;
                    faceAdoptModel = null;
                    if (!isFail) {
                        failNumber = 0;
                    }

                    livenessModel.setTrackStatus(1);
                    livenessModel.setLandmarks(faceInfos[0].landmarks);
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectDarwCallback(livenessModel);
                    }

                    onLivenessCheck(rgbInstance, nirData, depthData, secondFeature , srcHeight ,
                            srcWidth, livenessModel.getLandmarks(),
                            livenessModel, startTime, liveCheckMode, featureCheckMode,
                            faceDetectCallBack, faceInfos);
                } else {
                    emptyFrame();
                    // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
                    rgbInstance.destory();
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectCallback(null);
                        faceDetectCallBack.onFaceDetectDarwCallback(null);
                        faceDetectCallBack.onTip(0, "未检测到人脸");
                    }
                }
            }
        });
    }


    /**
     * 质量检测结果过滤，如果需要质量检测，
     * 需要调用 SingleBaseConfig.getBaseConfig().setQualityControl(true);设置为true，
     * 再调用  FaceSDKManager.getInstance().initConfig() 加载到底层配置项中
     *
     * @param livenessModel
     * @param faceDetectCallBack
     * @return
     */
    public boolean onQualityCheck(final LivenessModel livenessModel,
                                  final FaceDetectCallBack faceDetectCallBack) {

        if (!SingleBaseConfig.getBaseConfig().isQualityControl()) {
            return true;
        }

        if (livenessModel != null && livenessModel.getFaceInfo() != null) {

            // 角度过滤
            if (Math.abs(livenessModel.getFaceInfo().yaw) > SingleBaseConfig.getBaseConfig().getYaw()) {
                faceDetectCallBack.onTip(-1, "人脸左右偏转角超出限制");
                return false;
            } else if (Math.abs(livenessModel.getFaceInfo().roll) > SingleBaseConfig.getBaseConfig().getRoll()) {
                faceDetectCallBack.onTip(-1, "人脸平行平面内的头部旋转角超出限制");
                return false;
            } else if (Math.abs(livenessModel.getFaceInfo().pitch) > SingleBaseConfig.getBaseConfig().getPitch()) {
                faceDetectCallBack.onTip(-1, "人脸上下偏转角超出限制");
                return false;
            }

            // 模糊结果过滤
            float blur = livenessModel.getFaceInfo().bluriness;
            if (blur > SingleBaseConfig.getBaseConfig().getBlur()) {
                faceDetectCallBack.onTip(-1, "图片模糊");
                return false;
            }

            // 光照结果过滤
            float illum = livenessModel.getFaceInfo().illum;
            if (illum < SingleBaseConfig.getBaseConfig().getIllumination()) {
                faceDetectCallBack.onTip(-1, "图片光照不通过");
                return false;
            }


            // 遮挡结果过滤
            if (livenessModel.getFaceInfo().occlusion != null) {
                BDFaceOcclusion occlusion = livenessModel.getFaceInfo().occlusion;

                if (occlusion.leftEye > SingleBaseConfig.getBaseConfig().getLeftEye()) {
                    // 左眼遮挡置信度
                    faceDetectCallBack.onTip(-1, "左眼遮挡");
                } else if (occlusion.rightEye > SingleBaseConfig.getBaseConfig().getRightEye()) {
                    // 右眼遮挡置信度
                    faceDetectCallBack.onTip(-1, "右眼遮挡");
                } else if (occlusion.nose > SingleBaseConfig.getBaseConfig().getNose()) {
                    // 鼻子遮挡置信度
                    faceDetectCallBack.onTip(-1, "鼻子遮挡");
                } else if (occlusion.mouth > SingleBaseConfig.getBaseConfig().getMouth()) {
                    // 嘴巴遮挡置信度
                    faceDetectCallBack.onTip(-1, "嘴巴遮挡");
                } else if (occlusion.leftCheek > SingleBaseConfig.getBaseConfig().getLeftCheek()) {
                    // 左脸遮挡置信度
                    faceDetectCallBack.onTip(-1, "左脸遮挡");
                } else if (occlusion.rightCheek > SingleBaseConfig.getBaseConfig().getRightCheek()) {
                    // 右脸遮挡置信度
                    faceDetectCallBack.onTip(-1, "右脸遮挡");
                } else if (occlusion.chin > SingleBaseConfig.getBaseConfig().getChinContour()) {
                    // 下巴遮挡置信度
                    faceDetectCallBack.onTip(-1, "下巴遮挡");
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 最优人脸控制
     *
     * @param livenessModel
     * @param faceDetectCallBack
     * @return
     */
    public boolean onBestImageCheck(LivenessModel livenessModel,
                                    FaceDetectCallBack faceDetectCallBack) {
        if (!SingleBaseConfig.getBaseConfig().isBestImage()) {
            return true;
        }

        if (livenessModel != null && livenessModel.getFaceInfo() != null) {
            float bestImageScore = livenessModel.getFaceInfo().bestImageScore;
            if (bestImageScore < 50) {
                faceDetectCallBack.onTip(-1, "最优人脸不通过");
                return false;
            }
        }
        return true;
    }


    /**
     * 活体-特征-人脸检索全流程
     *
     * @param rgbInstance        可见光底层送检对象
     * @param nirData            红外YUV 数据流
     * @param depthData          深度depth 数据流
     * @param srcHeight          可见光YUV 数据流-高度
     * @param srcWidth           可见光YUV 数据流-宽度
     * @param landmark           检测眼睛，嘴巴，鼻子，72个关键点
     * @param livenessModel      检测结果数据集合
     * @param startTime          开始检测时间
     * @param liveCheckMode      活体检测类型【不使用活体：0】；【RGB活体：1】；【RGB+NIR活体：2】；【RGB+Depth活体：3】；【RGB+NIR+Depth活体：4】
     * @param featureCheckMode   特征抽取模式【不提取特征：1】；【提取特征：2】；【提取特征+1：N检索：3】；
     * @param faceDetectCallBack
     */
    public void onLivenessCheck(final BDFaceImageInstance rgbInstance,
                                final byte[] nirData,
                                final byte[] depthData,
                                final byte[] secondFeature,
                                final int srcHeight,
                                final int srcWidth,
                                final float[] landmark,
                                final LivenessModel livenessModel,
                                final long startTime,
                                final int liveCheckMode,
                                final int featureCheckMode,
                                final FaceDetectCallBack faceDetectCallBack,
                                final FaceInfo[] fastFaceInfos) {

        if (future2 != null && !future2.isDone()) {
            // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
            rgbInstance.destory();
            return;
        }

        future2 = es2.submit(new Runnable() {

            private FaceInfo[] faceInfos;

            @Override
            public void run() {
                BDFaceDetectListConf bdFaceDetectListConfig = new BDFaceDetectListConf();
                bdFaceDetectListConfig.usingQuality = bdFaceDetectListConfig.usingHeadPose
                        = SingleBaseConfig.getBaseConfig().isQualityControl();
                bdFaceDetectListConfig.usingAttribute = SingleBaseConfig.getBaseConfig().isAttribute();
                bdFaceDetectListConfig.usingBestImage = SingleBaseConfig.getBaseConfig().isBestImage();

                if (SingleBaseConfig.getBaseConfig().getType() == 2 &&
                        SingleBaseConfig.getBaseConfig().getActiveModel() == 3) {
                    AtomicInteger atomicInteger = new AtomicInteger();
                    int status = FaceSDKManager.getInstance().getImageIllum().imageIllum(rgbInstance, atomicInteger);
                    int illumScore = atomicInteger.get();

                    if (illumScore > SingleBaseConfig.getBaseConfig().getCameraLightThreshold()) {
                        faceInfos = FaceSDKManager.getInstance()
                                .getFaceDetect()
                                .detect(BDFaceSDKCommon.DetectType.DETECT_VIS,
                                        BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_NIR_ACCURATE,
                                        rgbInstance,
                                        fastFaceInfos, bdFaceDetectListConfig);
                    } else {
                        faceInfos = FaceSDKManager.getInstance()
                                .getFaceDetect()
                                .detect(BDFaceSDKCommon.DetectType.DETECT_VIS,
                                        BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_ACCURATE,
                                        rgbInstance,
                                        fastFaceInfos, bdFaceDetectListConfig);
                    }
                }
                faceInfos = FaceSDKManager.getInstance()
                        .getFaceDetect()
                        .detect(BDFaceSDKCommon.DetectType.DETECT_VIS,
                                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_ACCURATE,
                                rgbInstance,
                                fastFaceInfos, bdFaceDetectListConfig);

                // 重新赋予详细人脸信息
                if (faceInfos != null && faceInfos.length > 0) {
                    faceInfos[0].faceID = livenessModel.getFaceInfo().faceID;
                    livenessModel.setFaceInfo(faceInfos[0]);
                    livenessModel.setTrackStatus(2);
                    livenessModel.setLandmarks(faceInfos[0].landmarks);

                    if (mLastFaceId != fastFaceInfos[0].faceID) {
                        mLastFaceId = fastFaceInfos[0].faceID;
                        mRgbLiveList.clear();
                        mNirLiveList.clear();
                    }
                } else {
                    rgbInstance.destory();
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectCallback(livenessModel);
                    }
                    return;
                }

                // 最优人脸控制
                if (!onBestImageCheck(livenessModel, faceDetectCallBack)) {
                    livenessModel.setQualityCheck(false);
                    rgbInstance.destory();
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectCallback(livenessModel);
                    }
                    return;
                }

                // 质量检测未通过,销毁BDFaceImageInstance，结束函数
                if (!onQualityCheck(livenessModel, faceDetectCallBack)) {
                    livenessModel.setQualityCheck(false);
                    rgbInstance.destory();
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectCallback(livenessModel);
                    }
                    return;
                }
                livenessModel.setQualityCheck(true);

                // 获取LivenessConfig liveCheckMode 配置选项：【不使用活体：0】；【RGB活体：1】；【RGB+NIR活体：2】；【RGB+Depth活体：3】；【RGB+NIR+Depth活体：4】
                // TODO 活体检测
                float rgbScore = -1;
                if (liveCheckMode != 0) {
                    long startRgbTime = System.currentTimeMillis();
                    rgbScore = FaceSDKManager.getInstance().getFaceLiveness().silentLive(
                            BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_RGB,
                            rgbInstance, faceInfos[0].landmarks);

                    mRgbLiveList.add(rgbScore > mRgbLiveScore);
                    while (mRgbLiveList.size() > 6) {
                        mRgbLiveList.remove(0);
                    }
                    if (mRgbLiveList.size() > 2) {
                        int rgbSum = 0;
                        for (Boolean b : mRgbLiveList) {
                            if (b) {
                                rgbSum++;
                            }
                        }
                        if (1.0 * rgbSum / mRgbLiveList.size() > 0.6) {
                            if (rgbScore < mRgbLiveScore) {
                                rgbScore = mRgbLiveScore + (1 - mRgbLiveScore) * new Random().nextFloat();
                            }
                        } else {
                            if (rgbScore > mRgbLiveScore) {
                                rgbScore = new Random().nextFloat() * mRgbLiveScore;
                            }
                        }
                    }

                    livenessModel.setRgbLivenessScore(rgbScore);
                    livenessModel.setRgbLivenessDuration(System.currentTimeMillis() - startRgbTime);
//                    LogUtils.e(TIME_TAG, "live rgb time = " + livenessModel.getRgbLivenessDuration());
                }

                float nirScore = -1;
                if (liveCheckMode == 2 || liveCheckMode == 4 && nirData != null) {
                    // 创建检测对象，如果原始数据YUV-IR，转为算法检测的图片BGR
                    // TODO: 用户调整旋转角度和是否镜像，手机和开发版需要动态适配
                    BDFaceImageInstance nirInstance = new BDFaceImageInstance(nirData, srcHeight,
                            srcWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
                            SingleBaseConfig.getBaseConfig().getNirDetectDirection(),
                            SingleBaseConfig.getBaseConfig().getMirrorDetectNIR());

                    livenessModel.setBdNirFaceImageInstance(nirInstance.getImage());
                    // 避免RGB检测关键点在IR对齐活体稳定，增加红外检测
                    long startIrDetectTime = System.currentTimeMillis();
                    BDFaceDetectListConf bdFaceDetectListConf = new BDFaceDetectListConf();
                    bdFaceDetectListConf.usingDetect = true;
                    FaceInfo[] faceInfosIr = faceDetectNir.detect(BDFaceSDKCommon.DetectType.DETECT_NIR,
                            BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_NIR_ACCURATE,
                            nirInstance, null, bdFaceDetectListConf);
                    bdFaceDetectListConf.usingDetect = false;
                    livenessModel.setIrLivenessDuration(System.currentTimeMillis() - startIrDetectTime);
//                    LogUtils.e(TIME_TAG, "detect ir time = " + livenessModel.getIrLivenessDuration());

                    if (faceInfosIr != null && faceInfosIr.length > 0) {
                        FaceInfo faceInfoIr = faceInfosIr[0];
                        long startNirTime = System.currentTimeMillis();
                        nirScore = FaceSDKManager.getInstance().getFaceLiveness().silentLive(
                                BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_NIR,
                                nirInstance, faceInfoIr.landmarks);

                        mNirLiveList.add(nirScore > mNirLiveScore);
                        while (mNirLiveList.size() > 6) {
                            mNirLiveList.remove(0);
                        }
                        if (mNirLiveList.size() > 2) {
                            int nirSum = 0;
                            for (Boolean b : mNirLiveList) {
                                if (b) {
                                    nirSum++;
                                }
                            }
                            if (1.0f * nirSum / mNirLiveList.size() > 0.6) {
                                if (nirScore < mNirLiveScore) {
                                    nirScore = mNirLiveScore + new Random().nextFloat() * (1 - mNirLiveScore);
                                }
                            } else {
                                if (nirScore > mNirLiveScore) {
                                    nirScore = new Random().nextFloat() * mNirLiveScore;
                                }
                            }
                        }

                        livenessModel.setIrLivenessScore(nirScore);
                        livenessModel.setIrLivenessDuration(System.currentTimeMillis() - startNirTime);
//                        LogUtils.e(TIME_TAG, "live ir time = " + livenessModel.getIrLivenessDuration());
                    }

                    nirInstance.destory();
                }

                float depthScore = -1;
                if (liveCheckMode == 3 || liveCheckMode == 4 && depthData != null) {
                    // TODO: 用户调整旋转角度和是否镜像，适配Atlas 镜头，目前宽和高400*640，其他摄像头需要动态调整,人脸72 个关键点x 坐标向左移动80个像素点
                    float[] depthLandmark = new float[faceInfos[0].landmarks.length];
                    BDFaceImageInstance depthInstance;
                    if (SingleBaseConfig.getBaseConfig().getCameraType() == 1) {
                        System.arraycopy(faceInfos[0].landmarks, 0, depthLandmark, 0, faceInfos[0].landmarks.length);
                        if (SingleBaseConfig.getBaseConfig().getCameraType() == 1) {
                            for (int i = 0; i < 144; i = i + 2) {
                                depthLandmark[i] -= 80;
                            }
                        }
                        depthInstance = new BDFaceImageInstance(depthData,
                                SingleBaseConfig.getBaseConfig().getDepthWidth(),
                                SingleBaseConfig.getBaseConfig().getDepthHeight(),
                                BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_DEPTH,
                                0, 0);
                    } else {
                        depthInstance = new BDFaceImageInstance(depthData,
                                SingleBaseConfig.getBaseConfig().getDepthHeight(),
                                SingleBaseConfig.getBaseConfig().getDepthWidth(),
                                BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_DEPTH,
                                0, 0);
                    }
                    livenessModel.setBdDepthFaceImageInstance(depthInstance.getImage());

                    // 创建检测对象，如果原始数据Depth
                    long startDepthTime = System.currentTimeMillis();
                    if (SingleBaseConfig.getBaseConfig().getCameraType() == 1) {
                        depthScore = FaceSDKManager.getInstance().getFaceLiveness().silentLive(
                                BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_DEPTH,
                                depthInstance, depthLandmark);
                    } else {
                        depthScore = FaceSDKManager.getInstance().getFaceLiveness().silentLive(
                                BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_DEPTH,
                                depthInstance, faceInfos[0].landmarks);
                    }
                    livenessModel.setDepthLivenessScore(depthScore);
                    livenessModel.setDepthtLivenessDuration(System.currentTimeMillis() - startDepthTime);
//                    LogUtils.e(TIME_TAG, "live depth time = " + livenessModel.getDepthtLivenessDuration());
                    depthInstance.destory();
                }

                // TODO 特征提取+人脸检索
                if (liveCheckMode == 0) {
                    onFeatureCheck(rgbInstance, faceInfos[0].landmarks, null, srcHeight,
                            srcWidth, livenessModel, featureCheckMode,
                            SingleBaseConfig.getBaseConfig().getActiveModel());
                } else {
                    if (liveCheckMode == 1 && rgbScore > SingleBaseConfig.getBaseConfig().getRgbLiveScore()) {
                        onFeatureCheck(rgbInstance, faceInfos[0].landmarks, null, srcHeight,
                                srcWidth, livenessModel, featureCheckMode,
                                1);
                    } else if (liveCheckMode == 2 && rgbScore > SingleBaseConfig.getBaseConfig().getRgbLiveScore()
                            && nirScore > SingleBaseConfig.getBaseConfig().getNirLiveScore()) {
                        onFeatureCheck(rgbInstance, faceInfos[0].landmarks, nirData, srcHeight,
                                srcWidth, livenessModel, featureCheckMode,
                                SingleBaseConfig.getBaseConfig().getActiveModel());
                    } else if (liveCheckMode == 3 && rgbScore > SingleBaseConfig.getBaseConfig().getRgbLiveScore()
                            && depthScore > SingleBaseConfig.getBaseConfig().getDepthLiveScore()) {
                        onFeatureCheck(rgbInstance, faceInfos[0].landmarks, null, srcHeight,
                                srcWidth, livenessModel, featureCheckMode,
                                1);
                    } else if (liveCheckMode == 4 && rgbScore > SingleBaseConfig.getBaseConfig().getRgbLiveScore()
                            && nirScore > SingleBaseConfig.getBaseConfig().getNirLiveScore()
                            && depthScore > SingleBaseConfig.getBaseConfig().getDepthLiveScore()) {
                        onFeatureCheck(rgbInstance, faceInfos[0].landmarks, nirData, srcHeight,
                                srcWidth, livenessModel, featureCheckMode,
                                SingleBaseConfig.getBaseConfig().getActiveModel());
                    }
                }

                if (getCropFace()) {
                    BDFaceCropParam cropParam = new BDFaceCropParam();
                    cropParam.foreheadExtend = 2.0f / 9;
                    cropParam.chinExtend = 1.0f / 9;
                    cropParam.enlargeRatio = 1.5f;
                    cropParam.height = 640;
                    cropParam.width = 480;
                    BDFaceImageInstance cropInstance = FaceSDKManager.getInstance().getFaceCrop()
                            .cropFaceByLandmarkParam(rgbInstance, faceInfos[0].landmarks, cropParam);
                    if (cropInstance == null) {
                        rgbInstance.destory();
                        if (faceDetectCallBack != null) {
                            faceDetectCallBack.onTip(-1, "抠图失败");
                        }
                        return;
                    }
                    livenessModel.setBdFaceImageInstanceCrop(cropInstance);
                }

                long startCompareTime = System.currentTimeMillis();
                float score = FaceSDKManager.getInstance().getFaceFeature().featureCompare(
                        BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO,
                        livenessModel.getFeature(), secondFeature, true);
                livenessModel.setScore(score);
                if (score > SingleBaseConfig.getBaseConfig().getIdThreshold()) {
                    /*faceId = livenessModel.getFaceInfo().faceID;
                    trackTime = System.currentTimeMillis();
                    faceAdoptModel = livenessModel;
                    failNumber = 0;
                    isFail = false;*/
                    setFail(livenessModel);
                } else{
                    setFail(livenessModel);
                }

                livenessModel.setStartCompareTime(System.currentTimeMillis() - startCompareTime);
                // 流程结束,记录最终时间
                livenessModel.setAllDetectDuration(System.currentTimeMillis() - startTime);
//                LogUtils.e(TIME_TAG, "all process time = " + livenessModel.getAllDetectDuration());
                // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
                rgbInstance.destory();
                // 显示最终结果提示
                if (faceDetectCallBack != null) {
                    faceDetectCallBack.onFaceDetectCallback(livenessModel);
                }
            }
        });
    }

    /**
     * 特征提取-人脸识别比对
     *
     * @param rgbInstance      可见光底层送检对象
     * @param landmark         检测眼睛，嘴巴，鼻子，72个关键点
     * @param nirData          nir数据流
     * @param livenessModel    检测结果数据集合
     * @param featureCheckMode 特征抽取模式【不提取特征：1】；【提取特征：2】；【提取特征+1：N检索：3】；
     * @param featureType      特征抽取模态执行 【生活照：1】；【证件照：2】；【混合模态：3】；
     *
     */
    private void onFeatureCheck(BDFaceImageInstance rgbInstance,
                                float[] landmark,
                                byte[] nirData,
                                final int srcHeight,
                                final int srcWidth,
                                LivenessModel livenessModel,
                                final int featureCheckMode,
                                final int featureType) {

        // 如果不抽取特征，直接返回
        if (featureCheckMode == 1) {
            return;
        }
        byte[] feature = new byte[512];
        if (featureType == 3) {
            // todo: 混合模态使用方式是根据图片的曝光来选择需要使用的type，光照的取值范围为：0~1之间
            AtomicInteger atomicInteger = new AtomicInteger();
            FaceSDKManager.getInstance().getImageIllum().imageIllum(rgbInstance, atomicInteger);
            int illumScore = atomicInteger.get();
            if (illumScore > SingleBaseConfig.getBaseConfig().getIllumination()) {
                if (nirData != null) {
                    BDFaceImageInstance nirInstance = new BDFaceImageInstance(nirData, srcHeight,
                            srcWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
                            SingleBaseConfig.getBaseConfig().getNirDetectDirection(),
                            SingleBaseConfig.getBaseConfig().getMirrorDetectNIR());
                    BDFaceDetectListConf bdFaceDetectListConfig = new BDFaceDetectListConf();
                    bdFaceDetectListConfig.usingDetect = true;
                    FaceInfo[] faceInfos = FaceSDKManager.getInstance()
                            .getFaceDetect()
                            .detect(BDFaceSDKCommon.DetectType.DETECT_NIR,
                                    BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_NIR_ACCURATE,
                                    nirInstance,
                                    null, bdFaceDetectListConfig);
                    bdFaceDetectListConfig.usingDetect = false;
                    long startFeatureTime = System.currentTimeMillis();
                    float featureSize = FaceSDKManager.getInstance().getFaceFeature().feature(
                            BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_NIR, nirInstance,
                            faceInfos[0].landmarks, feature);
                    nirInstance.destory();
                    livenessModel.setFeatureDuration(System.currentTimeMillis() - startFeatureTime);
                    livenessModel.setFeature(feature);

                }
            } else {
                long startFeatureTime = System.currentTimeMillis();
                float featureSize = FaceSDKManager.getInstance().getFaceFeature().feature(
                        BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO, rgbInstance, landmark, feature);
                livenessModel.setFeatureDuration(System.currentTimeMillis() - startFeatureTime);
                livenessModel.setFeature(feature);
            }

        } else  {
            // 生活照检索
            long startFeatureTime = System.currentTimeMillis();
            float featureSize = FaceSDKManager.getInstance().getFaceFeature().feature(
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO, rgbInstance, landmark, feature);
            livenessModel.setFeatureDuration(System.currentTimeMillis() - startFeatureTime);
            livenessModel.setFeature(feature);
        }

    }

    // 人证核验
    public float personDetect(final Bitmap bitmap, final byte[] feature, Context context) {
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(bitmap);
        float ret = -1;
        BDFaceDetectListConf bdFaceDetectListConfig = new BDFaceDetectListConf();
        bdFaceDetectListConfig.usingQuality = bdFaceDetectListConfig.usingHeadPose
                = SingleBaseConfig.getBaseConfig().isQualityControl();
        bdFaceDetectListConfig.usingAttribute = SingleBaseConfig.getBaseConfig().isAttribute();
        bdFaceDetectListConfig.usingDetect = true;

        FaceInfo[] faceInfos = faceDetectPerson.detect(BDFaceSDKCommon.DetectType.DETECT_VIS,
                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_ACCURATE,
                rgbInstance,
                null, bdFaceDetectListConfig);

        if (faceInfos != null && faceInfos.length > 0) {
            // 判断质量检测，针对模糊度、遮挡、角度
            if (qualityCheck(faceInfos[0], context)) {
                ret = faceFeaturePerson.feature(BDFaceSDKCommon.FeatureType.
                        BDFACE_FEATURE_TYPE_LIVE_PHOTO, rgbInstance, faceInfos[0].landmarks, feature);
            }
        } else {
            rgbInstance.destory();
            return -1;
        }
        rgbInstance.destory();
        return ret;
    }


    /**
     * 质量检测
     * FaceInfo faceInfo
     *
     * @return
     */
    public boolean qualityCheck(final FaceInfo faceInfo, Context context) {

        if (!SingleBaseConfig.getBaseConfig().isQualityControl()) {
            return true;
        }
        if (faceInfo != null) {
            // 模糊结果过滤
            float blur = faceInfo.bluriness;
            if (blur > SingleBaseConfig.getBaseConfig().getBlur()) {
                ToastUtils.toast(context, "图片模糊");
                return false;
            }
            // 光照结果过滤
            float illum = faceInfo.illum;
            if (illum < SingleBaseConfig.getBaseConfig().getIllumination()) {
                ToastUtils.toast(context, "图片光照不通过");
                return false;
            }
            // 遮挡结果过滤
            if (faceInfo.occlusion != null) {
                BDFaceOcclusion occlusion = faceInfo.occlusion;
                if (occlusion.leftEye > SingleBaseConfig.getBaseConfig().getLeftEye()) {
                    // 左眼遮挡置信度
                    ToastUtils.toast(context, "左眼遮挡");
                } else if (occlusion.rightEye > SingleBaseConfig.getBaseConfig().getRightEye()) {
                    // 右眼遮挡置信度
                    ToastUtils.toast(context, "右眼遮挡");
                } else if (occlusion.nose > SingleBaseConfig.getBaseConfig().getNose()) {
                    // 鼻子遮挡置信度
                    ToastUtils.toast(context, "鼻子遮挡");
                } else if (occlusion.mouth > SingleBaseConfig.getBaseConfig().getMouth()) {
                    // 嘴巴遮挡置信度
                    ToastUtils.toast(context, "嘴巴遮挡");
                } else if (occlusion.leftCheek > SingleBaseConfig.getBaseConfig().getLeftCheek()) {
                    // 左脸遮挡置信度
                    ToastUtils.toast(context, "左脸遮挡");
                } else if (occlusion.rightCheek > SingleBaseConfig.getBaseConfig().getRightCheek()) {
                    // 右脸遮挡置信度
                    ToastUtils.toast(context, "右脸遮挡");
                } else if (occlusion.chin > SingleBaseConfig.getBaseConfig().getChinContour()) {
                    // 下巴遮挡置信度
                    ToastUtils.toast(context, "下巴遮挡");
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 卸载模型
     */
//    public void uninitModel() {
//        if (faceDetect != null) {
//            faceDetect.uninitModel();
//        }
//        if (faceFeature != null) {
//            faceFeature.uninitModel();
//        }
//        if (faceDetectNir != null) {
//            faceDetectNir.uninitModel();
//        }
//        if (faceLiveness != null) {
//            faceLiveness.uninitModel();
//        }
//        if (faceDetectPerson != null) {
//            faceDetectPerson.uninitModel();
//        }
//        if (faceFeaturePerson != null) {
//            faceFeaturePerson.uninitModel();
//        }
//
//        if (faceDetect.uninitModel() == 0
//                && faceFeature.uninitModel() == 0
//                && faceDetectNir.uninitModel() == 0
//                && faceLiveness.uninitModel() == 0
//                && faceDetectPerson.uninitModel() == 0
//                && faceFeaturePerson.uninitModel() == 0) {
//            initStatus = SDK_UNACTIVATION;
//            initModelSuccess = false;
//        }
//
//        Log.e("uninitModel","identify-uninitModel"
//                + faceDetect.uninitModel()
//                + faceFeature.uninitModel()
//                + faceDetectNir.uninitModel()
//                + faceDetectPerson.uninitModel()
//                + faceFeaturePerson.uninitModel());
//    }
}