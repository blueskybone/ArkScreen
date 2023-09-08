//
//
// Created by blueSkyBone on 2023/1/27.
//

#include <jni.h>
#include <string>
#include <cstdio>
#include <android/bitmap.h>
#include <vector>
#include "utils.h"

using namespace std;
#ifndef _Included_com_godot17_arksc_activity_ScreenTaskActivity
#ifdef __cplusplus
extern "C"
{
#endif
/*
* Class:     com_godot17_arksc_activity_ScreenCapture
* Method:    getTagText
* Signature: (Landroid/graphics/Bitmap;)[Ljava/lang/String;
*/
JNIEXPORT jstring JNICALL
Java_com_godot17_arksc_activity_ScreenTaskActivity_getTagText(JNIEnv *env, jclass clazz,
                                                                 jobject jBitmap, jstring jDataPath,
                                                                 jint jNum) {
    int result;
    string bitmapReadErr;
    AndroidBitmapInfo sourceInfo;
    result = AndroidBitmap_getInfo(env, jBitmap , &sourceInfo);
    if (result < 0)
    {
        bitmapReadErr = "WRONG,get bitmap info failed.";
        return env->NewStringUTF(bitmapReadErr.c_str());
    }
    if(sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888)
    {
        bitmapReadErr = "WRONG,get bitmap wrong format.";
        return env->NewStringUTF(bitmapReadErr.c_str());
    }
    uint32_t* sourceData;// ARGB
    result = AndroidBitmap_lockPixels(env, jBitmap, (void**)& sourceData);
    if (result < 0)
    {
        bitmapReadErr = "WRONG,get bitmap wrong format.";
        return env->NewStringUTF(bitmapReadErr.c_str());
    }
    jobjectArray array1;
    mBitmap8 bitmap;
    AndroidBitmap2mBitmap(bitmap, sourceInfo, sourceData);
    mBitmap8 n_bitmap;
    bitmap.copyMakeBorderTo(n_bitmap, BORDER_TOP, BORDER_BOTTOM, BORDER_LEFT, BORDER_RIGHT, BG_COLOR);

    std::vector<mRect> rectList;
    findAllOuterRect(n_bitmap, rectList);
    n_bitmap.release();
    if (rectList.size()!=5)
    {
        bitmap.release();
        string getTagsWrong= "NONE,found numbers of tag not correct.";
        return env->NewStringUTF(getTagsWrong.c_str());
    }

    jboolean *isCopy = nullptr;
    const char * filePath = (*env).GetStringUTFChars(jDataPath, isCopy);
    FILE *fp;
    if (!(fp = fopen( filePath, "rb")))
    {
        AndroidBitmap_unlockPixels(env, jBitmap);
        (*env).ReleaseStringUTFChars(jDataPath,filePath);
        return env->NewStringUTF(filePath);
    }
    else
    {
        int cnt = 0;
        char result_tag_all[100] = "";
        strcat(result_tag_all,"RECRUIT,");
        for (auto rect : rectList)
        {
            mBitmap8 rect_tag;
            mBitmap8 dst_tag;

            fseek(fp, 0, SEEK_SET);

            bitmap.copyRectTo(rect_tag, rect);
            cutTagBorder(rect_tag, dst_tag, jNum);
            rect_tag.release();

            mBitmap8 img_resize;
            char * result_tag = getTagText(dst_tag, img_resize, fp);
            strcat(result_tag_all,result_tag);
            strcat(result_tag_all,"_");
            cnt++;
            dst_tag.release();
        }
        bitmap.release();
        fclose(fp);
        AndroidBitmap_unlockPixels(env, jBitmap);
        std::string result_str = result_tag_all;
        (*env).ReleaseStringUTFChars(jDataPath,filePath);
        return env->NewStringUTF(result_str.c_str());
    }
}
}
#endif

