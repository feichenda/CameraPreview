package com.bsj.camerapreview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Button cameraOne;
    private Button cameraTwo;
    private Button cameraThree;
    private Button cameraFour;
    private ImageView exitImg;
    private byte[] mPreviewDataOne;
    private CameraManager cameraManager;
    private ImageReader imageReader;
    private String[] cameraIdList;
    private CameraDevice cameraDevice;
    private HandlerThread backgroundCamera;
    private Handler backgroundCameraHandler;
    private String index;
    private String selected = "-1";
    private volatile boolean isOpening;
    private volatile boolean isSaving;

    private Handler cameraHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
//                    index = (String) msg.obj;
                    initCamera(index, 1280, 720);
                    break;
                case 2:
                    closeCamera();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        backgroundCamera = new HandlerThread("backgroundCamera");
        backgroundCamera.start();
        backgroundCameraHandler = new Handler(backgroundCamera.getLooper());
        isOpening = false;
        try {
            cameraIdList = cameraManager.getCameraIdList();
            for (String s : cameraIdList) {
                Log.d(TAG, "cameraIdList" + s);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (cameraIdList.length > 0) {
            initSurfaceView("0", 1280, 720);
        } else {
            Toast.makeText(this, "没有可以使用的摄像头", Toast.LENGTH_LONG).show();
        }
        isShowBtn();
        updateBtn("0");
    }

    private void findView() {
        surfaceView = findViewById(R.id.surfaceView);
        cameraOne = findViewById(R.id.one);
        cameraTwo = findViewById(R.id.two);
        cameraThree = findViewById(R.id.three);
        cameraFour = findViewById(R.id.four);
        exitImg = findViewById(R.id.exit);
        setOnClickListener();
    }

    private void isShowBtn() {
        if (cameraIdList.length == 1) {
            cameraOne.setVisibility(View.GONE);
            cameraTwo.setVisibility(View.GONE);
            cameraThree.setVisibility(View.GONE);
            cameraFour.setVisibility(View.GONE);
        } else if (cameraIdList.length == 2) {
            cameraThree.setVisibility(View.GONE);
            cameraFour.setVisibility(View.GONE);
        } else if (cameraIdList.length == 3) {
            cameraFour.setVisibility(View.GONE);
        }
    }

    private void setOnClickListener() {
        exitImg.setOnClickListener(v -> {
            closeCamera();
            finish();
        });
        cameraOne.setOnClickListener(v -> {
            updateBtn("0");
            if (AntiShakeUtils.isInvalidClick(v)) {
                Log.d(TAG, "0 time");
                return;
            }
            if (isOpening) {
                Log.d(TAG, "0" + isOpening);
                return;
            }
//            if (isCurCamera("0")) {
//                Log.d(TAG, "0");
//                return;
//            }
            cameraHandler.sendEmptyMessage(2);
//            cameraHandler.removeMessages(2);
            cameraHandler.sendEmptyMessage(1);
//            Message message = new Message();
//            message.what = 1;
//            message.obj = "0";
//            cameraHandler.sendMessage(message);
        });
        cameraTwo.setOnClickListener(v -> {
            updateBtn("1");
            if (AntiShakeUtils.isInvalidClick(v)) {
                Log.d(TAG, "1 time");
                return;
            }
            if (isOpening) {
                Log.d(TAG, "1" + isOpening);
                return;
            }
//            if (isCurCamera("1")) {
//                Log.d(TAG, "1");
//                return;
//            }
            cameraHandler.sendEmptyMessage(2);
//            cameraHandler.removeMessages(2);
            cameraHandler.sendEmptyMessage(1);
//            Message message = new Message();
//            message.what = 1;
//            message.obj = "1";
//            cameraHandler.sendMessage(message);
        });
        cameraThree.setOnClickListener(v -> {
            updateBtn("2");
            if (AntiShakeUtils.isInvalidClick(v)) {
                Log.d(TAG, "2 time");
                return;
            }
            if (isOpening) {
                Log.d(TAG, "2" + isOpening);
                return;
            }
//            if (isCurCamera("2")) {
//                Log.d(TAG, "2");
//                return;
//            }
            cameraHandler.sendEmptyMessage(2);
//            cameraHandler.removeMessages(2);
            cameraHandler.sendEmptyMessage(1);
//            Message message = new Message();
//            message.what = 1;
//            message.obj = "2";
//            cameraHandler.sendMessage(message);
        });
        cameraFour.setOnClickListener(v -> {
            updateBtn("3");
            if (AntiShakeUtils.isInvalidClick(v)) {
                Log.d(TAG, "3 time");
                return;
            }
            if (isOpening) {
                Log.d(TAG, "3" + isOpening);
                return;
            }
//            if (isCurCamera("3")) {
//                Log.d(TAG, "3");
//                return;
//            }
            cameraHandler.sendEmptyMessage(2);
//            cameraHandler.removeMessages(2);
            cameraHandler.sendEmptyMessage(1);
//            Message message = new Message();
//            message.what = 1;
//            message.obj = "3";
//            cameraHandler.sendMessage(message);
        });
    }

    private boolean isCurCamera(String select) {
        return TextUtils.equals(select, index);
    }

    private void initSurfaceView(String index, int width, int height) {
        this.index = index;
        if (surfaceHolder != null) {
            surfaceHolder.getSurface().release();
            surfaceHolder = null;
        }
        Log.d(TAG, "index " + index);
        surfaceHolder = surfaceView.getHolder();
        Log.d(TAG, "surfaceHolder getHolder");
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                Log.d(TAG, "surfaceCreated ");
                initCamera(index, width, height);
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                Log.d(TAG, "surfaceChanged ");
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                Log.d(TAG, "surfaceDestroyed ");
                closeCamera();
            }
        });
        Log.d(TAG, "surfaceHolder addCallback");
    }

    private void initCamera(String index, int width, int height) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            ReentrantLock lock = new ReentrantLock();
            imageReader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 1);
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    lock.lock();
                    Image image = imageReader.acquireLatestImage();
                    if (image == null) {
                        return;
                    }
                    int format = image.getFormat();
                    Log.d(TAG, "format" + format);
                    if (mPreviewDataOne == null) {
                        // YUV420 大小老是 width * height * 3 / 2
                        mPreviewDataOne = new byte[width * height * 3 / 2];
                    }

                    // YUV_420_888
                    Image.Plane[] planes = image.getPlanes();

                    // Y通道，对应planes[0]
                    // Y size = width * height
                    // yBuffer.remaining() = width * height;
                    // pixelStride = 1
                    ByteBuffer yBuffer = planes[0].getBuffer();
                    int yLen = width * height;
                    yBuffer.get(mPreviewDataOne, 0, yLen);
                    // U通道，对应planes[1]
                    // U size = width * height / 4;
                    // uBuffer.remaining() = width * height / 2;
                    // pixelStride = 2
                    ByteBuffer uBuffer = planes[1].getBuffer();
                    int pixelStride = planes[1].getPixelStride(); // pixelStride = 2
                    for (int i = 0; i < uBuffer.remaining(); i += pixelStride) {
                        mPreviewDataOne[yLen++] = uBuffer.get(i);
                    }
                    // V通道，对应planes[2]
                    // V size = width * height / 4;
                    // vBuffer.remaining() = width * height / 2;
                    // pixelStride = 2
                    ByteBuffer vBuffer = planes[2].getBuffer();
                    pixelStride = planes[2].getPixelStride(); // pixelStride = 2
                    for (int i = 0; i < vBuffer.remaining(); i += pixelStride) {
                        mPreviewDataOne[yLen++] = vBuffer.get(i);
                    }
                    lock.unlock();
                    // 必定不能忘记close
                    image.close();
                }
            }, backgroundCameraHandler);
            if (!selected.equals(index)) {
                cameraManager.openCamera(index, cameraStateCallback, backgroundCameraHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeCamera();
//        if (surfaceHolder != null) {
//            surfaceHolder.getSurface().release();
//            surfaceHolder = null;
//        }
        if (backgroundCamera != null) {
            backgroundCamera.interrupt();
            backgroundCamera = null;
        }
        if (backgroundCameraHandler != null) {
            backgroundCameraHandler = null;
        }
    }

    private void closeCamera() {
        if (isOpening) {
            return;
        }
        Log.d(TAG, "closeCamera");
        selected = "-1";
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        if (backgroundCamera != null) {
            backgroundCamera.interrupt();
//            backgroundCamera.stop();
            backgroundCamera = null;
        }
        if (backgroundCameraHandler != null) {
            backgroundCameraHandler = null;
        }
    }

    private void updateBtn(String index) {
        this.index = index;
        cameraOne.setTextColor(Color.BLACK);
        cameraTwo.setTextColor(Color.BLACK);
        cameraThree.setTextColor(Color.BLACK);
        cameraFour.setTextColor(Color.BLACK);
        switch (index) {
            case "0":
                cameraOne.setTextColor(Color.RED);
                break;
            case "1":
                cameraTwo.setTextColor(Color.RED);
                break;
            case "2":
                cameraThree.setTextColor(Color.RED);
                break;
            case "3":
                cameraFour.setTextColor(Color.RED);
                break;
        }
    }

    CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            if (isOpening) {
                Log.d(TAG, "isopening");
                return;
            }
            isOpening = true;
            Log.d(TAG, "onOpened" + cameraDevice.getId());
            MainActivity.this.cameraDevice = cameraDevice;
            try {
                CaptureRequest.Builder captureRequestBuilder = cameraDevice
                        .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                captureRequestBuilder.addTarget(surfaceHolder.getSurface());
                captureRequestBuilder.addTarget(imageReader.getSurface());
                List<Surface> list = new ArrayList<>();
                list.add(surfaceHolder.getSurface());
                list.add(imageReader.getSurface());
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
                            cameraCaptureSession.setRepeatingRequest(captureRequest, null, backgroundCameraHandler);
                            selected = cameraDevice.getId();
                        } catch (Exception e) {
                            e.printStackTrace();
                            closeCamera();
                        } finally {
                            isOpening = false;
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
                }, backgroundCameraHandler);
            } catch (Exception e) {
                e.printStackTrace();
                closeCamera();
            } finally {
                isOpening = false;
            }
        }

        @Override
        public void onClosed(@NonNull CameraDevice cameraDevice) {
            Log.d(TAG, "onClosed");
            isOpening = false;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Log.d(TAG, "onDisconnected");
            isOpening = false;
            closeCamera();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            Log.e(TAG, "onError" + i);
            isOpening = false;
            closeCamera();
        }
    };

}