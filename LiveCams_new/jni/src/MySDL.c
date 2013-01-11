#include "SDL.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <malloc.h>
#include <math.h>
#include "MySDL.h"
#define LOG_TAG "JNI MYSDL"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
extern SDL_Surface* screen = NULL;
extern SDL_Overlay* overlay = NULL;
int play_SDL(int w, int h, char* pYuv, int zoom,int cutedW,int cutedH,int offsetX,int offsetY) {
	if (NULL == pYuv || w <= 0 || h <= 0)
	{
		return -1;
	}
	SDL_Rect rectSrc;
	if(cutedW>0 || cutedH>0)
	{
		change_Overlay(cutedW,cutedH);
	}
	SDL_LockSurface(screen);
	SDL_LockYUVOverlay(overlay);
	if(cutedW==0 && cutedH==0)
	{
		memcpy(overlay->pixels[0], pYuv, w * h);
		memcpy(overlay->pixels[2], pYuv + w * h, w * h / 4);
		memcpy(overlay->pixels[1], pYuv + w * h + w * h / 4, w * h / 4);
		rectSrc.w = w*zoom;
		rectSrc.h = h*zoom;
		rectSrc.x = rectSrc.y = 0;
	}
	else
	{
	int count=0,count1=0,count2=0;
	int x=0,xU=0,xV=0;
	int len=0,len1=0,len2=0;
	char tempY[cutedW*cutedH];
	char tempU[cutedW*cutedH/4];
	char tempV[cutedW*cutedH/4];
	x=w*offsetY+offsetX;
	for(count;count<cutedH;count++)
	{
		memcpy(tempY+len, pYuv+x,cutedW);
		x+=w;
		len+=cutedW;
	}
	xU=w/2*offsetY/2+offsetX/2;
	for(count1;count1<cutedH/2;count1++)
	{
		memcpy(tempU+len1, pYuv+w*h+xU,cutedW/2);
		xU+=w/2;
		len1+=cutedW/2;
	}
	xV=w/2*offsetY/2+offsetX/2;
		for(count2;count2<cutedH/2;count2++)
		{
			memcpy(tempV+len2, pYuv+w*h+w*h/4+xV,cutedW/2);
			xV+=w/2;
			len2+=cutedW/2;
		}
	memcpy(overlay->pixels[0], (const char *)tempY, cutedW * cutedH);
	memcpy(overlay->pixels[2], (const char *)tempU, cutedW * cutedH/4);
	memcpy(overlay->pixels[1], (const char *)tempV, cutedW * cutedH/4);
	rectSrc.w = cutedW*zoom;
	rectSrc.h = cutedH*zoom;
	rectSrc.x = rectSrc.y = 0;

	}
	SDL_UnlockYUVOverlay(overlay);
	SDL_UnlockSurface(screen);
	SDL_DisplayYUVOverlay(overlay, &rectSrc);
	return 0;
}
int init_SDL(int w, int h, char* pYuv, int swidth, int sheight, int zoom) {
	if (SDL_Init(SDL_INIT_VIDEO) < 0) {
		fprintf(stderr, "can not initialize SDL:%s\n", SDL_GetError());
		LOGI("can not initialize SDL");
		exit(1);
	}
	atexit(SDL_Quit);
	screen = SDL_SetVideoMode(swidth, sheight, 0, 0);  //创建屏幕
	LOGI("2!\n");
	LOGI("screen creat success");
	if (screen == NULL ) {
		LOGI("create surface error!\n");
		exit(1);
	}
	overlay = SDL_CreateYUVOverlay(w, h, SDL_YV12_OVERLAY, screen);
	LOGI("overlay creat success");
	if (overlay == NULL ) {
		fprintf(stderr, "create overlay error!\n");
		exit(1);
	}
	return 0;
}
int change_Overlay(int newWidth, int newHeight) {
	if (overlay) {
		SDL_FreeYUVOverlay(overlay);
	}
	overlay = SDL_CreateYUVOverlay(newWidth, newHeight, SDL_YV12_OVERLAY, screen);
	return 0;
}
int release_SDL() {
	if (overlay) {
		SDL_FreeYUVOverlay(overlay);
	}
	if (screen) {
		SDL_FreeSurface(screen);
	}
	LOGI("RELEASE SUCCESS");
	return 2;
}
