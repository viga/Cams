#include "SDL.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <stdio.h>
#include <android/log.h>
#include <malloc.h>
#include <android/bitmap.h>
#include <math.h>
#include <stdlib.h>

#define LOG_TAG "Fuck"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
int sdl_yuvrender(SDL_Overlay* overlay,SDL_Surface* screen,char* pyuv,int ilen,int iwidth,int iheight);
int sdl_yuvrender(SDL_Overlay* overlay,SDL_Surface* screen,char* pyuv,int ilen,int iwidth,int iheight)
{
	//iwidth/iheight 视频分辨率：宽度 高度  pyuv 一帧yuv数据  ilen yuv数据的长度
	if(NULL == pyuv || ilen <= 0 || iwidth <= 0 || iheight<= 0)
	{
		return -1;
	}

 		SDL_LockSurface(screen);
        SDL_LockYUVOverlay(overlay);

        memcpy(overlay->pixels[0], pyuv, iwidth*iheight);
        memcpy(overlay->pixels[2], pyuv+iwidth*iheight, iwidth*iheight/4);
        memcpy(overlay->pixels[1], pyuv+iwidth*iheight+iwidth*iheight/4, iwidth*iheight/4);

        SDL_UnlockYUVOverlay(overlay);
        SDL_UnlockSurface(screen);

        SDL_Rect rect;
        rect.w = iwidth*2; //缩放
        rect.h = iheight*2;
        rect.x = rect.y = 0;
        SDL_DisplayYUVOverlay(overlay, &rect);
	return 0;
}
int play_SDL(int iwidth,int iheight,char* pbuf)
{
    int i = 1;
    int x, y;
    int w = 176;
    int h = 144;
    char c = 'n';

    FILE* fp;
    char filename[64];
    unsigned char* pYuv;
    char szdbg[256] = "";
    int  ireadlen = 0;
    SDL_Rect rect;

    if (SDL_Init(SDL_INIT_VIDEO) < 0)
    {

        fprintf(stderr, "can not initialize SDL:%s\n", SDL_GetError());
        exit(1);
    }
    atexit(SDL_Quit);

    SDL_Surface* screen = SDL_SetVideoMode(310, 288, 0, 0);  //缩放  352 边界会有问题 原因不明
    if (screen == NULL)
    {
        fprintf(stderr, "create surface error!\n");
        exit(1);
    }

    SDL_Overlay* overlay = SDL_CreateYUVOverlay(w, h, SDL_YV12_OVERLAY, screen);
    if (overlay == NULL)
    {
        fprintf(stderr, "create overlay error!\n");
        exit(1);
    }

    printf("w:%d, h:%d, planes:%d\n", overlay->w, overlay->h, overlay->planes);
    printf("pitches:%d, %d, %d\n", overlay->pitches[0], overlay->pitches[1], overlay->pitches[2]);

    pYuv = (unsigned char*)malloc(w*h*1.5);

    if(NULL == pYuv)
    {
        fprintf(stderr, "New memory error!\n");
        exit(2);
    }

    while (1)
    {
        if(i==96)
        {
        	i=1;
        }

        sprintf(filename, "/sdcard/carphone/carphone%03d.yuv", i);
        printf("%s\n", filename);

        fp = fopen(filename, "rb");
        if (fp == NULL)
        {
        	sprintf(szdbg, "open file(%s) error!\n",filename);
        	i ++;
        	continue;

            free(pYuv);
            exit(1);
        }else{
        	fprintf(stderr, "Open file(%s) succeed!\n",filename);
        }

        ireadlen = fread(pYuv, 1, w*h*1.5, fp);
        //传 一帧yuv数据的byte数组 pYuv
        sprintf(szdbg,"filename:%s,readlen:%d\n",filename,ireadlen);
        LOGI(szdbg);

        sdl_yuvrender(overlay,screen,pYuv, w*h*1.5,w,h);

        sprintf(szdbg,"sdl_yuvrender ok\n");
        LOGI(szdbg);


        fclose(fp);

        i += 1;

    }

    free(pYuv);
 

    while (c != 'q')
        scanf("%c", &c);

    SDL_FreeYUVOverlay(overlay);
    SDL_FreeSurface(screen);

    return 0;
}
