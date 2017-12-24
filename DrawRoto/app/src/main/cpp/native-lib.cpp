#include <jni.h>
#include <string>
#include "merck_org_drawroto_NDKUtils.h"
#include <android/bitmap.h>
#include <opencv2/opencv.hpp>

#include <jni.h>
#include <string>

using namespace cv;
using namespace std;

extern "C"
JNIEXPORT void

JNICALL
Java_merck_org_drawroto_MainActivity_stringFromJNI(JNIEnv *env, jobject thizz, jobject bitmap) {

    AndroidBitmapInfo info;
    void *pixels;
    CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
    CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
              info.format == ANDROID_BITMAP_FORMAT_RGB_565);
    CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
    CV_Assert(pixels);
    if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        Mat temp(info.height, info.width, CV_8UC4, pixels);
        Mat gray;
        cvtColor(temp, gray, COLOR_RGBA2GRAY);
        Canny(gray, gray, 3, 9, 3);
        cvtColor(gray, temp, COLOR_GRAY2RGBA);
    } else {
        Mat temp(info.height, info.width, CV_8UC2, pixels);
        Mat gray;
        cvtColor(temp, gray, COLOR_RGB2GRAY);
        Canny(gray, gray, 3, 9, 3);
        cvtColor(gray, temp, COLOR_GRAY2RGB);
    }
    AndroidBitmap_unlockPixels(env, bitmap);

    //std::string hello = "Hello from C++";
    //return env->NewStringUTF(hello.c_str());
}

