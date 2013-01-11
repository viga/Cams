#include "SDL.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <malloc.h>
#include <math.h>
#include "MySDL.h"
#define LOG_TAG "SDL JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
extern SDL_Surface* screen = NULL;
extern SDL_Overlay* overlay = NULL;
int play_SDL(int w, int h, char* pYuv, int zoom,int cutedW,int cutedH,int offsetX,int offsetY) {
	//iwidth/iheight 视频分辨率：宽度 高度  pyuv 一帧yuv数据  ilen yuv数据的长度
	if (NULL == pYuv || w <= 0 || h <= 0) {
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
		rectSrc.w = w*zoom;
		rectSrc.h = h*zoom;
		rectSrc.x = rectSrc.y = 0;
		memcpy(overlay->pixels[0], pYuv, w * h);
		memcpy(overlay->pixels[2], pYuv + w * h, w * h / 4);
		memcpy(overlay->pixels[1], pYuv + w * h + w * h / 4, w * h / 4);
	}else
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
	//memcpy(overlay->pixels[0], pyuv, w * h);
	//memcpy(overlay->pixels[2], pYuv + w * h, w * h / 4);
	//memcpy(overlay->pixels[1], pYuv + w * h + w * h / 4, w * h / 4);
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
	//cutScreen = SDL_AllocSurface(0,swidth, sheight,24,0,0, 0, 0);  //创建屏幕
	LOGI("screen creat success");
	if (screen == NULL ) {
		LOGI("create surface error!\n");
		exit(1);
	}
	overlay = SDL_CreateYUVOverlay(w, h, SDL_YV12_OVERLAY, screen);
	//newoverlay = SDL_CreateYUVOverlay(w, h, SDL_YV12_OVERLAY, cutScreen);
	LOGI("overlay creat success");
	if (overlay == NULL ) {
		fprintf(stderr, "create overlay error!\n");
		exit(1);
	}
	FILE* fp;
	char filename[64];
	char szdbg[256] = "";
	int ireadlen = 0;
	int i = 1;
	int z = 0;
	while (1) {
		if (i == 96) {
		//	if(zoom==1){
				//change_Overlay(352,288);
			//zoom = 3;
			//}
			i = 1;
		}
		sprintf(filename, "/sdcard/carphone/carphone%03d.yuv", i);
		printf("%s\n", filename);

		fp = fopen(filename, "rb");
		if (fp == NULL ) {
			sprintf(szdbg, "open file(%s) error!\n", filename);
			i++;
			continue;
			free(pYuv);
			exit(1);
		} else {
			fprintf(stderr, "Open file(%s) succeed!\n", filename);
		}
		ireadlen = fread(pYuv, 1, w * h * 1.5, fp);
		//传 一帧yuv数据的byte数组 pYuv
		sprintf(szdbg, "filename:%s,readlen:%d\n", filename, ireadlen);

		//播放函数
		play_SDL(w, h, pYuv, 1,76,44,100,100);
		sprintf(szdbg, "sdl_yuvrender ok+%d   \n", z);
		fclose(fp);
		i += 1;
		z++;
	}
	// SDL_UnlockYUVOverlay(overlay);
	//  SDL_UnlockSurface(screen);
	exit(0);
	release_SDL();
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
	char* pYuv;
	pYuv = (char*) malloc(176 * 144 * 1.5);
	LOGI("RELEASE SUCCESS");
	return 2;
}
