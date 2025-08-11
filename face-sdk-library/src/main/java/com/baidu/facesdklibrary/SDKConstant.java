package com.baidu.facesdklibrary;

/**
 * @Time: 2019/5/24
 * @Author: v_zhangxiaoqing01
 */

public class SDKConstant {
    public static final String TAG = "SDKConstant";

    // 模型在asset 下path 为空

    public static final String PATH = "";
    public static final String FEATURE_DIR = "faceiddir";
    public static final String FEATURE_EXNAME = ".faceid";
    public static final String FEATURE_EXIMGNAME = ".facdidimg";

    public static final String SAVEPIC_PATH = "/storage/emulated/0/bdface/night-hawk/savepic/";
    public static final String SAVELOG_PATH = "/storage/emulated/0/bdface/night-hawk/debug.txt";

    // 模型在SD 卡下写对应的绝对路径
    // public static final String PATH = "/storage/emulated/0/baidu_face/model/";

    public static final int FEATURE_SIZE = 512;

    public static final String TIME_TAG = "face_time";

    // 遮罩比例

    public static final float SURFACE_RATIO = 0.6f;

    public static final String DETECT_VIS_MODEL = PATH
            + "face-sdk-models/detect/detect_rgb-customized-pa-mix_detecttion.model.float32-0.0.16.3";
    public static final String DETECT_NIR_MODE = PATH
            + "face-sdk-models/detect/detect_rgb-customized-pa-mix_detecttion.model.float32-0.0.16.3";

    //    public static final String ALIGN_RGB_MODEL = PATH
//            + "align/align_rgb-mobilenet-pa-bigmodel_191224.model.int8-0.7.6.1";

    public static final String ALIGN_RGB_MODEL = PATH
            + "face-sdk-models/align/align_rgb-customized-pa-float32.model.float32-6.4.14.3";
    public static final String ALIGN_NIR_MODEL = PATH
            + "face-sdk-models/align/align_rgb-customized-pa-float32.model.float32-6.4.14.3";
    public static final String ALIGN_TRACK_MODEL = PATH
            + "face-sdk-models/align/align_rgb-customized-pa-fast.model.float32-0.7.5.5";
    public static final String LIVE_VIS_MODEL = PATH
            + "face-sdk-models/silent_live/liveness_rgb-customized-pa-embededfix_71.model.float32-1.1.47.1";
    public static final String LIVE_NIR_MODEL = PATH
            + "face-sdk-models/silent_live/liveness_nir-customized-pa-faceid7_general.model.float32-1.1.44.2";
    public static final String LIVE_VIS_2DMASK_MODEL = PATH
            + "face-sdk-models/silent_live/" +
            "liveness_rgb-customized-pa-2dmask_sdk_224.model.float32-e49746c4c90d415d849ffb2dd68ad883";
    public static final String LIVE_VIS_HAND_MODEL = PATH
            + "face-sdk-models/silent_live/" +
            "liveness_rgb-customized-pa-hand_sdk_224.model.float32-d80dc9193263481d8da1b49cd080a99d";
    public static final String LIVE_VIS_REFLECTION_MODEL = PATH
            + "face-sdk-models/silent_live/" +
            "liveness_rgb-customized-pa-reflection_sdk_224.model.float32-2818e39d148d4500bfa2387228e5a895";
    public static final String LIVE_DEPTH_MODEL = PATH
            + "face-sdk-models/silent_live/liveness_depth-customized-pa-paddle_60.model.float32-1.1.13.2";
    public static final String RECOGNIZE_VIS_MODEL = PATH
            + "face-sdk-models/feature/feature_live-mnasnet-pa-int8_7_0.model.int8-2.0.185.2";
    public static final String RECOGNIZE_IDPHOTO_MODEL = PATH
            + "face-sdk-models/feature/feature_live-mnasnet-pa-int8_7_0.model.int8-2.0.185.2";
    public static final String RECOGNIZE_NIR_MODEL = PATH
            + "face-sdk-models/feature/feature_nir-mnasnet-pa-foreign.model.int8-2.0.189.1";
    public static final String RECOGNIZE_RGBD_MODEL = PATH
            + "face-sdk-models/feature/feature_live-mnasnet-pa-RGBD_FaceID_5.model.int8-2.0.88.3";
    public static final String OCCLUSION_MODEL = PATH
            + "face-sdk-models/occlusion/occlusion-customized-pa-paddle.model.float32-2.0.7.3";
    public static final String BLUR_MODEL = PATH
            + "face-sdk-models/blur/blur-customized-pa-addcloud_quant_e19.model.float32-3.0.13.3";

    public static final String ATTRIBUTE_MODEL = PATH
            + "face-sdk-models/attribute/attribute-customized-pa-mobile.model.float32-1.0.9.5";
    public static final String EMOTION_MODEL = PATH
            + "";
    public static final String DARK_ENHANCE_MODEL = PATH
            + "face-sdk-models/dark_enhance/dark_enhance-customized-pa-zero_depthwise.model.float32-1.0.2.2";
    public static final String GAZE_MODEL = PATH
            + "face-sdk-models/gaze/gaze-customized-pa-mobile.model.float32-1.0.3.4";
    public static final String DRIVEMONITOR_MODEL = PATH
            + "face-sdk-models/driver_monitor/driver_monitor_nir-customized-pa-DMS_rgb_nir_detect" +
            ".model.float32-1.0.1.2";
    public static final String MOUTH_MASK = PATH
            + "face-sdk-models/mouth_mask/mouth_mask-customized-pa-20200306.model.float32-1.0.6.4";
    public static final String BEST_IMAGE = PATH
            + "face-sdk-models/best_image/best_image-mobilenet-pa-dcqe449_live_e51_relu.model.float32-1.0.2.1";

    // 图片尺寸限制大小

    public static final int PICTURE_SIZE = 1000000;

    // 摄像头类型

    public static final String TYPE_CAMERA = "TYPE_CAMERA";
    public static final int ORBBEC = 1;
    public static final int IMIMECT = 2;
    public static final int ORBBECPRO = 3;
    public static final int ORBBECPROS1 = 4;
    public static final int ORBBECPRODABAI = 5;
    public static final int ORBBECPRODEEYEA = 6;
    public static final int ORBBECATLAS = 7;

}