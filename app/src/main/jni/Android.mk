
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

OPENCV_JNI_PATH := $(LOCAL_PATH)/opencv_processing

IMAGE_PROCESSING_PATH := $(LOCAL_PATH)/../../../../image-processing/src

IMAGE_PROCESSING_JNI_PATH := $(LOCAL_PATH)/image_processing

TESS_TWO_PATH := $(LOCAL_PATH)/../../../../tess-two
LEPTONICA_SRC_PATH := $(TESS_TWO_PATH)/jni/com_googlecode_leptonica_android/src

LIBJPEG_PATH := $(LOCAL_PATH)/../../../../libjpeg
LIBPNG_PATH := $(LOCAL_PATH)/../../../../libpng-android/jni

LOCAL_LDFLAGS := -L$(TESS_TWO_PATH)/libs/$(TARGET_ARCH_ABI)/ -llept

include $(LIBJPEG_PATH)/Android.mk
include $(LIBPNG_PATH)/Android.mk
include $(OPENCV_JNI_PATH)/Android.mk
include $(IMAGE_PROCESSING_JNI_PATH)/Android.mk
