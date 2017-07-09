#include <jni.h>
#include "common.h"
#include <cstring>
#include "android/bitmap.h"
#include "allheaders.h"
#include <sstream>
#include <iostream>
#include <pthread.h>
#include <cmath>
#include "pageseg.h"
#include "PixBlurDetect.h"
#include "PixBinarizer.h"
#include <image_processing_util.h>
#include "SkewCorrector.h"
#include <fcntl.h>

using namespace std;

#ifdef __cplusplus
extern "C" {
#endif  /* __cplusplus */
    
    static jmethodID onProgressImage, onProgressValues, onProgressText, onLayoutElements, onUTF8Result, onLayoutPix;
    
    static JNIEnv *cachedEnv;
    static jobject cachedObject;
    static FILE *inputFile;
    static int pipes[2];


    jint JNI_OnLoad(JavaVM* vm, void* reserved) {
        return JNI_VERSION_1_6;
    }

    
    void Java_com_ldz_fpt_businesscardscannerandroid_activity_ocr_1activity_OCR_nativeInit(JNIEnv *env, jobject _thiz) {
        jclass cls = env->FindClass("com/ldz/fpt/businesscardscannerandroid/activity/ocr_activity/OCR");
        onProgressImage = env->GetMethodID(cls, "onProgressImage", "(J)V");
        onProgressText = env->GetMethodID(cls, "onProgressText", "(I)V");
        onLayoutElements = env->GetMethodID(cls, "onLayoutElements", "(II)V");
        onUTF8Result = env->GetMethodID(cls, "onUTF8Result", "(Ljava/lang/String;)V");
        onLayoutPix = env->GetMethodID(cls, "onLayoutPix", "(J)V");
        
    }
    
    void JNI_OnUnload(JavaVM *vm, void *reserved) {
    }
    
    void initStateVariables(JNIEnv* env, jobject object) {
        cachedEnv = env;
        cachedObject = env->NewGlobalRef(object);
    }
    
    void resetStateVariables() {
        cachedEnv->DeleteGlobalRef(cachedObject);
        cachedEnv = NULL;
    }
    
    bool isStateValid() {
        if (cachedEnv != NULL) {
            return true;
        } else {
            return false;
            
        }
    }
    
    void messageJavaCallback(int message) {
        if (isStateValid()) {
            cachedEnv->CallVoidMethod(cachedObject, onProgressText, message);
        }
    }
    
    void pixJavaCallback(Pix* pix) {
        if (isStateValid()) {
            cachedEnv->CallVoidMethod(cachedObject, onProgressImage, (jlong) pix);
        }
    }
    
    void callbackLayout(const Pix* pixpreview) {
        if (isStateValid()) {
            cachedEnv->CallVoidMethod(cachedObject, onLayoutPix, (jlong)pixpreview);
        }
        messageJavaCallback(MESSAGE_ANALYSE_LAYOUT);
    }
    
    
    jlongArray Java_com_ldz_fpt_businesscardscannerandroid_activity_ocr_1activity_OCR_combineSelectedPixa(JNIEnv *env, jobject thiz, jlong nativePixaText, jlong nativePixaImage, jintArray selectedTexts, jintArray selectedImages) {
        LOGV(__FUNCTION__);
        Pixa *pixaTexts = (PIXA *) nativePixaText;
        Pixa *pixaImages = (PIXA *) nativePixaImage;
        initStateVariables(env, thiz);
        jint* textindexes = env->GetIntArrayElements(selectedTexts, NULL);
        jsize textCount = env->GetArrayLength(selectedTexts);
        jint* imageindexes = env->GetIntArrayElements(selectedImages, NULL);
        jsize imageCount = env->GetArrayLength(selectedImages);
        
        Pix* pixFinal;
        Pix* pixOcr;
        Boxa* boxaColumns;
        
        combineSelectedPixa(pixaTexts, pixaImages, textindexes, textCount, imageindexes, imageCount, messageJavaCallback, &pixFinal, &pixOcr, &boxaColumns, true);
        pixJavaCallback(pixFinal);
        
        jlongArray result;
        result = env->NewLongArray(3);
        if (result == NULL) {
            return NULL; /* out of memory error thrown */
        }
        
        jlong fill[3];
        fill[0] = (jlong) pixFinal;
        fill[1] = (jlong) pixOcr;
        fill[2] = (jlong) boxaColumns;
        // move from the temp structure to the java structure
        env->SetLongArrayRegion(result, 0, 3, fill);
        
        resetStateVariables();
        env->ReleaseIntArrayElements(selectedTexts, textindexes, 0);
        env->ReleaseIntArrayElements(selectedImages, imageindexes, 0);
        return result;
    }
    
    
    
    jint Java_com_ldz_fpt_businesscardscannerandroid_activity_ocr_1activity_OCR_nativeAnalyseLayout(JNIEnv *env, jobject thiz, jint nativePix) {
        LOGV(__FUNCTION__);
        Pix *pixOrg = (PIX *) nativePix;
        Pix* pixTextlines = NULL;
        Pixa* pixaTexts, *pixaImages;
        initStateVariables(env, thiz);
        
        Pix* pixb, *pixhm;
        messageJavaCallback(MESSAGE_IMAGE_DETECTION);
        
        PixBinarizer binarizer(false);
        Pix* pixOrgClone = pixClone(pixOrg);
        pixb = binarizer.binarize(pixOrgClone, pixJavaCallback);

        pixJavaCallback(pixb);
        
//        SkewCorrector skewCorrector(false);
//        Pix* pixbRotated = skewCorrector.correctSkew(pixb, NULL);
//        pixDestroy(&pixb);
//        pixb = pixbRotated;
//        pixJavaCallback(pixb);
        
        segmentComplexLayout(pixOrg, NULL, pixb, &pixaImages, &pixaTexts, callbackLayout, true);
        
        if (isStateValid()) {
            env->CallVoidMethod(thiz, onLayoutElements, pixaTexts, pixaImages);
        }
        
        resetStateVariables();
        return (jint) 0;
    }
    
    jobject Java_com_ldz_fpt_businesscardscannerandroid_blurdetection_Blur_nativeBlurDetect(JNIEnv *env, jobject thiz, jlong nativePix) {
        Pix *pixOrg = (PIX *) nativePix;
        PixBlurDetect blurDetector(true);
        l_float32 blurValue;
        L_TIMER timer = startTimerNested();
        Box* maxBlurLoc = NULL;
        Pix* pixBlended = blurDetector.makeBlurIndicator(pixOrg,&blurValue, &maxBlurLoc);
        l_int32 w,h,x,y;
        boxGetGeometry(maxBlurLoc,&x,&y,&w,&h);
        //pixRenderBox(pixBlended,maxBlurLoc,2,L_SET_PIXELS);
        //create result
        jclass cls = env->FindClass("com/ldz/fpt/businesscardscannerandroid/blurdetection/BlurDetectionResult");
        jmethodID constructor = env->GetMethodID(cls, "<init>", "(JDJ)V");
        return env->NewObject(cls, constructor, (jlong)pixBlended, (jdouble)blurValue, (jlong)maxBlurLoc);
    }
    
    
    
    jlong Java_com_ldz_fpt_businesscardscannerandroid_activity_ocr_1activity_OCR_nativeOCRBook(JNIEnv *env, jobject thiz, jlong nativePix) {
        LOGV(__FUNCTION__);
        Pix *pixOrg = (PIX *) nativePix;
        Pix* pixText;
        initStateVariables(env, thiz);

        bookpage(pixOrg, &pixText , messageJavaCallback, pixJavaCallback, false);

        resetStateVariables();
        
        return (jlong)pixText;
        
    }
    
#ifdef __cplusplus
}
#endif  /* __cplusplus */
