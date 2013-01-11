#include "SDL.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "MySDL.h"
#include <android/log.h>
#define LOG_TAG "debug"
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt,##args)

int main(int argc , char* argv[])
{
char* pYuv;
pYuv = (char*)malloc(176*144*1.5);
init_SDL(176,144,pYuv,480,300,1);
change_Overlay(352,288);
}
