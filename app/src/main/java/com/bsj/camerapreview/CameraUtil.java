package com.bsj.camerapreview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: chenhao
 * Date: 2022/3/3-0003 下午 06:48:30
 * Describe:
 */
public class CameraUtil {
    private static final String TAG = "CameraUtil";
    private static Context mContext;
    private static String mSelected;
    private CameraManager mCameraManager;
    private HandlerThread mCameraHandlerThread;
    private Handler mCameraHandler;
    private CameraDevice mCameraDevice;
    private ImageReader mImageReader;
    private byte[] mPreviewData;


    private CameraUtil() {
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mCameraHandlerThread = new HandlerThread(TAG);
        mCameraHandler = new Handler(mCameraHandlerThread.getLooper());
    }

    private static class CameraUtilHolder{
        private static final CameraUtil instance = new CameraUtil();
    }

    public static CameraUtil getInstance(@NonNull Context context) {
        mContext = context;
        return CameraUtilHolder.instance;
    }

    public void init(int width,int height,@NonNull PreviewDataCallback callback) {
        mImageReader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 1);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader imageReader) {
                Image image = imageReader.acquireLatestImage();
                if (image == null) {
                    return;
                }

                if (mPreviewData == null) {
                    // YUV420 大小老是 width * height * 3 / 2
                    mPreviewData = new byte[width * height * 3 / 2];
                }

                // YUV_420_888
                Image.Plane[] planes = image.getPlanes();


                // Y通道，对应planes[0]
                // Y size = width * height
                // yBuffer.remaining() = width * height;
                // pixelStride = 1
                ByteBuffer yBuffer = planes[0].getBuffer();
                int yLen = width * height;
                yBuffer.get(mPreviewData, 0, yLen);
                // U通道，对应planes[1]
                // U size = width * height / 4;
                // uBuffer.remaining() = width * height / 2;
                // pixelStride = 2
                ByteBuffer uBuffer = planes[1].getBuffer();
                int pixelStride = planes[1].getPixelStride(); // pixelStride = 2
                for (int i = 0; i < uBuffer.remaining(); i += pixelStride) {
                    mPreviewData[yLen++] = uBuffer.get(i);
                }
                // V通道，对应planes[2]
                // V size = width * height / 4;
                // vBuffer.remaining() = width * height / 2;
                // pixelStride = 2
                ByteBuffer vBuffer = planes[2].getBuffer();
                pixelStride = planes[2].getPixelStride(); // pixelStride = 2
                for (int i = 0; i < vBuffer.remaining(); i += pixelStride) {
                    mPreviewData[yLen++] = vBuffer.get(i);
                }
                callback.onPreviewData(mPreviewData,image.getTimestamp());
                // 必定不能忘记close
                image.close();
            }
        }, mCameraHandler);
    }

    private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            try {
                mCameraDevice = cameraDevice;
                CaptureRequest.Builder captureRequestBuilder = cameraDevice
                        .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//                captureRequestBuilder.addTarget(surfaceHolder.getSurface());
//                captureRequestBuilder.addTarget(imageReader.getSurface());
                List<Surface> list = new ArrayList<>();
//                list.add(surfaceHolder.getSurface());
//                list.add(imageReader.getSurface());
                cameraDevice.createCaptureSession(list, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        Log.d(TAG, "设置连续自动对焦和自动曝光");
                        // 设置连续自动对焦和自动曝光
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        CaptureRequest captureRequest = captureRequestBuilder.build();
                        try {
                            Log.d(TAG, "请求数据");
                            cameraCaptureSession.setRepeatingRequest(captureRequest, null, mCameraHandler);
//                            selected = cameraDevice.getId();
                        } catch (Exception e) {
                            e.printStackTrace();
                            closeCamera();
                        } finally {
//                            isOpening = false;
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (cameraCaptureSession.isReprocessable()) {
                                    cameraCaptureSession.stopRepeating();
                                }
                            }
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        } finally {
                            cameraCaptureSession.close();
                        }
                    }
                }, mCameraHandler);
            } catch (Exception e) {
                e.printStackTrace();
                closeCamera();
            } finally {
//                isOpening = false;
            }
        }

        @Override
        public void onClosed(@NonNull CameraDevice cameraDevice) {

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
        }
    };

    @SuppressLint("MissingPermission")
    public void openCamera(@NonNull String index) throws CameraAccessException {
        mSelected = index;
        mCameraManager.openCamera(index,cameraStateCallback,mCameraHandler);
    }

    private void closeCamera() {
        mCameraDevice.close();
    }

    public void closeCamera(@NonNull String index) {

    }

    public interface PreviewDataCallback{
        void onPreviewData(byte[] data,long time);
    }
}
