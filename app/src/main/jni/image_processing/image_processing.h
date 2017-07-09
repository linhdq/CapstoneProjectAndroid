#ifndef COMMON_H_
#define COMMON_H_
#include <iostream>
#include "allheaders.h"
#include <sstream>
#include <cmath>

#ifdef __ANDROID_API__
#include <android/log.h>
#define LOG_TAG "ImageProcessing"
#define printf(fmt,args...)  __android_log_print(ANDROID_LOG_INFO  ,LOG_TAG, fmt, ##args)
/*dont write debug images onto the sd card*/
#define pixWrite(name,pixs,format)  __android_log_print(ANDROID_LOG_INFO  ,LOG_TAG, name)
#endif



static const int MESSAGE_IMAGE_DETECTION = 0;
static const int MESSAGE_IMAGE_DEWARP = 1;
static const int MESSAGE_OCR = 2;
static const int MESSAGE_ASSEMBLE_PIX = 3;
static const int MESSAGE_ANALYSE_LAYOUT = 4;

#define MAX_INT16 0x7fff



#endif /* COMMON_H_ */
