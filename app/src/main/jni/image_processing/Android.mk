LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

# jni
LOCAL_SRC_FILES += \
  binarize.cpp \
  pageseg.cpp \
  RunningStats.cpp \
  PixBlurDetect.cpp \
  image_processing_util.cpp \
  dewarp.cpp \
  TimerUtil.cpp \
  PixEdgeDetector.cpp \
  PixAdaptiveBinarizer.cpp \
  PixBinarizer.cpp \
  SkewCorrector.cpp \
  image_processing.cpp

LOCAL_MODULE := libimage_processing_jni

LOCAL_LDLIBS += \
  -llog \
  -lstdc++ \

  LOCAL_C_INCLUDES += $(IMAGE_PROCESSING_PATH)
  LOCAL_C_INCLUDES += $(LEPTONICA_SRC_PATH)/src

LOCAL_LDFLAGS := -L$(TESS_TWO_PATH)/libs/$(TARGET_ARCH_ABI)/ -llept

  
#common
LOCAL_SHARED_LIBRARIES:= libjpeg
LOCAL_PRELINK_MODULE:= false
LOCAL_DISABLE_FORMAT_STRING_CHECKS:=true
include $(BUILD_SHARED_LIBRARY)