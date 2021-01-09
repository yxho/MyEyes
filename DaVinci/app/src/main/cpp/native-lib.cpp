#include <jni.h>
#include <string>
#include<opencv2/opencv.hpp>
#include<iostream>
#include <opencv2/imgproc/types_c.h>
#include "ImageProcess.h"
using namespace cv;
using namespace std;

extern "C" JNIEXPORT jstring JNICALL
Java_com_yxh_davinci_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_yxh_davinci_Native_ImageHandleInterface_ImageHandleInterface(JNIEnv *env, jclass clazz,
                                                                      jintArray buf, jint w,
                                                                      jint h, jstring method) {
    jint *cbuf;
    jboolean ptfalse = false;
    cbuf = env->GetIntArrayElements(buf, &ptfalse);
    if(cbuf == NULL){
        return 0;
    }

    ImageProcess mImageProcess = ImageProcess();
    // TODO:添加图像处理方法
    //switch
    Mat imgData(h, w, CV_8UC4, (unsigned char*)cbuf);
//    // 注意，Android的Bitmap是ARGB四通道,而不是RGB三通道
//    cvtColor(imgData,imgData,CV_BGRA2GRAY);
//    cvtColor(imgData,imgData,CV_GRAY2BGRA);

    mImageProcess.PixelArt(imgData);



    int size=w * h;
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, (jint*)imgData.data);
    env->ReleaseIntArrayElements(buf, cbuf, 0);
    return result;
}