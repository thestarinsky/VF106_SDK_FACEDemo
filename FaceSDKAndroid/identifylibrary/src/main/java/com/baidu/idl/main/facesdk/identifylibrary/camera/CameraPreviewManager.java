package com.baidu.idl.main.facesdk.identifylibrary.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import com.baidu.idl.main.facesdk.identifylibrary.callback.CameraDataCallback;
import com.baidu.idl.main.facesdk.identifylibrary.model.SingleBaseConfig;

import java.io.IOException;
import java.util.List;

/**
 * Time: 2019/1/24
 * Author: v_chaixiaogang
 * Description:
 */
public class CameraPreviewManager implements TextureView.SurfaceTextureListener {

    private static final String TAG = "camera_preview";


    AutoTexturePreviewView mTextureView;
    boolean mPreviewed = false;
    private boolean mSurfaceCreated = false;
    private SurfaceTexture mSurfaceTexture;

    public static final int CAMERA_FACING_BACK = 0;

    public static final int CAMERA_FACING_FRONT = 1;

    public static final int CAMERA_USB = 2;

    public static final int CAMERA_ORBBEC = 3;

    /**
     * 垂直方向
     */
    public static final int ORIENTATION_PORTRAIT = 0;
    /**
     * 水平方向
     */
    public static final int ORIENTATION_HORIZONTAL = 1;

    /**
     * 当前相机的ID。
     */
    private int cameraFacing = CAMERA_FACING_BACK;

    private int previewWidth;
    private int previewHeight;

    private int videoWidth;
    private int videoHeight;

    private int tempWidth;
    private int tempHeight;

    private int textureWidth;
    private int textureHeight;

    private Camera mCamera;
    private int mCameraNum;

    private int displayOrientation = 0;
    private int cameraId = 0;
    private int mirror = 1; // 镜像处理

    public void setmCameraDataCallback(CameraDataCallback mCameraDataCallback) {
        this.mCameraDataCallback = mCameraDataCallback;
    }

    private CameraDataCallback mCameraDataCallback;
    private static volatile CameraPreviewManager instance = null;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {

        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    public static CameraPreviewManager getInstance() {
        synchronized (CameraPreviewManager.class) {
            if (instance == null) {
                instance = new CameraPreviewManager();
            }
        }
        return instance;
    }

    public int getCameraFacing() {
        return cameraFacing;
    }

    public void setCameraFacing(int cameraFacing) {
        this.cameraFacing = cameraFacing;
    }

    public int getDisplayOrientation() {
        return displayOrientation;
    }

    public void setDisplayOrientation(int displayOrientation) {
        this.displayOrientation = displayOrientation;
    }

    /**
     * 开启预览
     * @param textureView
     */
    public void startPreview(/*Context context,*/ AutoTexturePreviewView textureView, int width,
                             int height/*, CameraDataCallback cameraDataCallback*/) {
        Log.e(TAG, "开启预览模式");
//        Context mContext = context;
//        this.mCameraDataCallback = cameraDataCallback;
        mTextureView = textureView;
        this.previewWidth = width;
        this.previewHeight = height;
        mSurfaceTexture = mTextureView.getTextureView().getSurfaceTexture();
        mTextureView.getTextureView().setSurfaceTextureListener(this);
    }
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int i, int i1) {
        /*Log.e(TAG, "--surfaceTexture--SurfaceTextureAvailable");
        mSurfaceTexture = texture;
        mSurfaceCreated = true;
        textureWidth = i;
        textureHeight = i1;*/

        try {
            if (mCamera != null && !mPreviewed) {
                mSurfaceTexture = texture;
                mCamera.setPreviewTexture(mSurfaceTexture);
                mCamera.startPreview();
                mPreviewed = true;
                mSurfaceCreated = true;
            }
        } catch (IOException exception) {
            Log.e("chaixiaogang", "IOException caused by setPreviewDisplay()", exception);
        }
//        openCamera();

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int i, int i1) {
        Log.e(TAG, "--surfaceTexture--TextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {

        if (mCamera != null) {
            // mCamera.stopPreview();
            mPreviewed = false;
//            mCamera.setPreviewCallback(null);
//            mCamera.stopPreview();
//            mCamera.release();
//            mCamera = null;
        }
        mSurfaceCreated = false;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        // Log.e(TAG, "--surfaceTexture--Updated");
    }


    /**
     * 关闭预览
     */
    public void stopPreview() {
        if (mCamera != null) {
            try {
//                mCamera.setPreviewTexture(null);
//                mSurfaceCreated = false;
//                mTextureView = null;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mPreviewed = false;
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                Log.e("qing", "camera destory error");
                e.printStackTrace();

            }
        }
    }


    /**
     * 开启摄像头
     */

    public void openCamera() {

        try {
            if (mCamera == null) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (i == cameraFacing) {
                        cameraId = i;
                    }
                }
                mCamera = Camera.open(0);
                Log.e(TAG, "initCamera---open camera");
            }

            // 摄像头图像预览角度
            int cameraRotation = SingleBaseConfig.getBaseConfig().getRgbVideoDirection();
            mCamera.setDisplayOrientation(cameraRotation);
            if (cameraRotation == 90 || cameraRotation == 270) {
                int isRgbRevert = SingleBaseConfig.getBaseConfig().getMirrorVideoRGB();
                if (isRgbRevert == 1) {
                    mTextureView.setRotationY(180);
                } else {
                    mTextureView.setRotationY(0);
                }
                // 旋转90度或者270，需要调整宽高
                mTextureView.setPreviewSize(previewHeight, previewWidth);
            } else {
                int isRgbRevert = SingleBaseConfig.getBaseConfig().getMirrorVideoRGB();
                if (isRgbRevert == 1) {
                    mTextureView.setRotationY(180);
                } else {
                    mTextureView.setRotationY(0);
                }
                mTextureView.setPreviewSize(previewWidth, previewHeight);
            }
            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> sizeList = params.getSupportedPreviewSizes(); // 获取所有支持的camera尺寸
            final Camera.Size optionSize = getOptimalPreviewSize(sizeList, previewWidth,
                    previewHeight); // 获取一个最为适配的camera.size
            if (optionSize.width == previewWidth && optionSize.height == previewHeight) {
                videoWidth = previewWidth;
                videoHeight = previewHeight;
            } else {
                videoWidth = optionSize.width;
                videoHeight = optionSize.height;
            }
            params.setPreviewSize(videoWidth, videoHeight);

            mCamera.setParameters(params);
            try {
                mCamera.setPreviewTexture(mSurfaceTexture);
                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] bytes, Camera camera) {
                        if (mCameraDataCallback != null) {
                            mCameraDataCallback.onGetCameraData(bytes, camera,
                                    videoWidth, videoHeight);
                        }
                    }
                });
                mCamera.startPreview();
//
//
//                // 创建Rect区域
//                Rect focusArea = new Rect();
//                focusArea.left = 0; // 取最大或最小值，避免范围溢出屏幕坐标
//                focusArea.top = 0;
//                focusArea.right = 640;
//                focusArea.bottom = 480;
//                // 创建Camera.Area
//                Camera.Area cameraArea = new Camera.Area(focusArea, 1000);
//                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
//                List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
//                Camera.Parameters mParameters = mCamera.getParameters();
//                if (mParameters.getMaxNumMeteringAreas() > 0) {
//                    meteringAreas.add(cameraArea);
//                    focusAreas.add(cameraArea);
//                }
//                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO); // 设置对焦模式
//                mParameters.setFocusAreas(focusAreas); // 设置对焦区域
//                mParameters.setMeteringAreas(meteringAreas); // 设置测光区域
//                try {
//                    mCamera.cancelAutoFocus(); // 每次对焦前，需要先取消对焦
//                    mCamera.setParameters(mParameters); // 设置相机参数
//                    mCamera.autoFocus(new Camera.AutoFocusCallback() {
//                        @Override
//                        public void onAutoFocus(boolean b, Camera camera) {
//
//                        }
//                    }); // 开启对焦
//                } catch (Exception e) {
//                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        } catch (RuntimeException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    private int getCameraDisplayOrientation(int degrees, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation + degrees) % 360;
            rotation = (360 - rotation) % 360; // compensate the mirror
        } else { // back-facing
            rotation = (info.orientation - degrees + 360) % 360;
        }
        return rotation;
    }


    /**
     * 解决预览变形问题
     *
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double aspectTolerance = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > aspectTolerance) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    int a = 0;
    public void huan(){
        if (mCamera!=null) {
            mCamera.setDisplayOrientation(a);
            a += 90;
            if (a > 270) {
                a = 0;
            }
        }
    }
}
