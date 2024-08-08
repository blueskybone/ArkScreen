/*
* Created by blueskybone on 2023/1/27.
*/

#include <jni.h>
#include <string>
#include <cstdio>
#include <android/bitmap.h>
#include <vector>
#include "core.h"

using namespace std;
#ifndef _Included_com_blueskybone_arkscreen_base_ImageProcessor
#ifdef __cplusplus
extern "C"
{
#endif
/*
* Class:     com_blueskybone_arkscreen_task_screenshot_ImageProcessor
* Method:    getTagText
* Signature: (Landroid/graphics/Bitmap;)[Ljava/lang/String;
*/
extern "C"
JNIEXPORT jstring JNICALL
Java_com_blueskybone_arkscreen_task_screenshot_ImageProcessor_getTagText(JNIEnv *env,
                                                                         jobject clazz,
                                                                         jobject jBitmap,
                                                                         jstring jDataPath,
                                                                         jint jNum) {
    int result;
    string bitmapReadErr;
    AndroidBitmapInfo sourceInfo;
    result = AndroidBitmap_getInfo(env, jBitmap, &sourceInfo);
    if (result < 0) {
        bitmapReadErr = "WRONG,get bitmap info failed.";
        return env->NewStringUTF(bitmapReadErr.c_str());
    }
    if (sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        bitmapReadErr = "WRONG,get bitmap wrong format.";
        return env->NewStringUTF(bitmapReadErr.c_str());
    }
    uint32_t *sourceData;// ARGB
    result = AndroidBitmap_lockPixels(env, jBitmap, (void **) &sourceData);
    if (result < 0) {
        bitmapReadErr = "WRONG,get bitmap wrong format.";
        return env->NewStringUTF(bitmapReadErr.c_str());
    }

    arkscreen::Bitmap8 bitmap;
    arkscreen::AndroidBitmapToBitmap(bitmap, sourceInfo, sourceData);
    arkscreen::Bitmap8 n_bitmap;
    bitmap.copyMakeBorderTo(n_bitmap, BORDER_TOP, BORDER_BOTTOM, BORDER_LEFT, BORDER_RIGHT,
                            BG_COLOR);

    std::vector <arkscreen::Rect> rectList;
    arkscreen::tag::FindAllOuterRect(n_bitmap, rectList);
    n_bitmap.release();
    if (rectList.size() != 5) {
        bitmap.release();
        string getTagsWrong = "NONE,found numbers of tag not correct.";
        return env->NewStringUTF(getTagsWrong.c_str());
    }

    jboolean *isCopy = nullptr;
    const char *filePath = (*env).GetStringUTFChars(jDataPath, isCopy);
    FILE *fp;
    if (!(fp = fopen(filePath, "rb"))) {
        AndroidBitmap_unlockPixels(env, jBitmap);
        (*env).ReleaseStringUTFChars(jDataPath, filePath);
        return env->NewStringUTF(filePath);
    } else {
        //TODO:change result format
        int cnt = 0;
        char result_tag_all[100] = "";
        strcat(result_tag_all, "RECRUIT,");
        for (auto rect: rectList) {
            arkscreen::Bitmap8 rect_tag;
            arkscreen::Bitmap8 dst_tag;

            fseek(fp, 0, SEEK_SET);

            bitmap.copyRectTo(rect_tag, rect);
            arkscreen::tag::CutTagBorder(rect_tag, dst_tag, jNum);
            rect_tag.release();

            arkscreen::Bitmap8 img_resize;
            const char * result_tag = arkscreen::tag::GetTagText(dst_tag, img_resize, fp);
            strcat(result_tag_all, result_tag);
            strcat(result_tag_all, "_");
            cnt++;
            dst_tag.release();
        }
        bitmap.release();
        fclose(fp);
        AndroidBitmap_unlockPixels(env, jBitmap);
        std::string result_str = result_tag_all;
        result_str.pop_back();
        (*env).ReleaseStringUTFChars(jDataPath, filePath);
        return env->NewStringUTF(result_str.c_str());
    }
}
}
#endif