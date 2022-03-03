//package com.bsj.camerapreview;
//
//import com.bsj.cameramng.CameraManageNative;
//import com.bsj.videocoder.VideoCoderNative;
//
//import java.nio.ByteBuffer;
//
///**
// * Author: chenhao
// * Date: 2022/3/3-0003 下午 06:10:30
// * Describe:
// */
//public class Demo {
//    //比特率
//    public static final int BIT_512 = 512;
//
//    //帧率
//    public static final int FRAME_25 = 25;
//
//    //I帧间隔
//    public static final int INTERVAL_I = 25;
//
//    //摄像头id
//    public static int channelId = 0;
//
//    public static int inWidth = 1280;
//    public static int inHeight = 720;
//    public static int outWidth = 1280;
//    public static int outHeight = 720;
//
//    public static void main(String[] args) {
//        int openCode = CameraManageNative.getInstance().open(channelId);
//        int startCode = CameraManageNative.getInstance().start_by_byte(channelId, null);
//        //参数1：channelId 摄像头编号，从0开始依次类推
//        //参数2：strName 唯一标识符
//        //参数3：CameraManageNative.CameraCaptureNotify YUV数据回调接口
//        int registCaptureNotify = CameraManageNative.getInstance().registCaptureNotify(channelId, "", new CameraManageNative.CameraCaptureNotify() {
//            @Override
//            public int onCaptureNotify(int i, CameraManageNative.CameraBuffCache cameraBuffCache, String s) {
//                //参数1：摄像头状态
//                //参数2：摄像头数据缓冲
//                //参数3：唯一标识符
//                if (i == CameraManageNative.CAMERA_MNG_STATUS_CAPING) {
//                    //获取YUV数据
//                    ByteBuffer[] buffCacheList = cameraBuffCache.getBuffCacheList();
//                    byte[] array = new byte[buffCacheList[0].remaining()];
//                    buffCacheList[0].get(array, 0, array.length);
//                    //保存获取的YUV数据，在array中
//                }
//                return 0;
//            }
//
//            @Override
//            public int onErrorState(int errCode, long errTime) {
//                //参数1：错误代码
//                //CameraManageNative.CameraCaptureNotify.CAMERA_MNG_DEVERROR_STATE_TIMEOUT-->摄像头数据获取超时
//                //CameraManageNative.CameraCaptureNotify.CAMERA_MNG_DEVERROR_STATE_DQ-->摄像头数据获取异常
//                //CameraManageNative.CameraCaptureNotify.CAMERA_MNG_DEVERROR_STATE_UNLOCK-->摄像头失锁
//                //参数2：错误出现的时间戳
//                return 0;
//            }
//        });
//        VideoCoderNative.VideoCoderCreatConfig videoCoderCreateConfig =
//                new VideoCoderNative.VideoCoderCreatConfig(inWidth, inHeight, outWidth, outHeight, VideoCoderNative.VIDEO_CODER_FMT_NV21);
//        videoCoderCreateConfig.VideoCoderSetH264(BIT_512, FRAME_25, INTERVAL_I);
//        videoCoderCreateConfig.VideoCoderSetInFlowCamera(channelId);
//        videoCoderCreateConfig.VideoCoderSetOutFlowBSJFile(channelId);
//        long videoCoderId = VideoCoderNative.getInstance().creatCoder(videoCoderCreateConfig);
//        int registerCoderNotifyCode = VideoCoderNative.getInstance().registCoderNotify(videoCoderId, String.valueOf(videoCoderId), new VideoCoderNative.VideoCoderNotify() {
//            @Override
//            public int onCoderNotify(long l, ByteBuffer byteBuffer, int i, VideoCoderNative.EncoderStatus encoderStatus, String s) {
//                //参数1：编码器唯一标识符
//                //参数2：H264数据缓冲区
//                //参数3：H264编码数据大小
//                //参数4：帧数据类型，I帧或P帧
//                //参数5：唯一标识符
//                //获取该数据帧类型
//                VideoCoderNative.EncoderH264Status encoderH264Status = (VideoCoderNative.EncoderH264Status) encoderStatus;
//                //获取H264数据
//                byte[] h264Byte = new byte[i];
//                byteBuffer.get(h264Byte, 0, h264Byte.length);
//                return 0;
//            }
//
//            @Override
//            public int onCoderErrorState(long l, VideoCoderNative.EncoderErrorState encoderErrorState, String s) {
//                //参数1：编码器唯一标识符
//                //参数2：编码器错误类型
//                //VideoCoderNative.EncoderErrorState.VIDEO_CODER_ERROT_STATE_CAMERA-->编码器无法获取摄像头数据
//                //VideoCoderNative.EncoderErrorState.VIDEO_CODER_ERROT_STATE_VCODER-->编码器编码错误
//                //参数3：唯一标识符
//                return 0;
//            }
//        });
//        int startVideoCoderCode = VideoCoderNative.getInstance().startCoder(videoCoderId);
//    }
//}
