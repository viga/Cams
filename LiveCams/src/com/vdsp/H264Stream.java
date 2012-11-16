package com.vdsp;

import com.utils.Utils;

public class H264Stream {				// SIZE	    HTCC510E  MIONEPLUS
	public final static int TYPE_QCIF=0;//176x144     支持     	支持
	public final static int TYPE_QVGA=1;//320x240     支持     	支持
	public final static int TYPE_CIF=2;	//352x288     支持     	支持
	public final static int TYPE_VGA=3;	//640x480	  支持     	支持
	public final static int TYPE_D1 =4;	//704x576    不支持     不支持
	
	/*写同步头*/
	public static int writeHead(byte[] data, int offset, int len){
		final byte[] head={0,0,0,1};
		int dlen=head.length;
		if(len>=dlen){
			for(int i=0;i<dlen;i++){
				data[offset+i]=head[i];
			}
			return dlen;
		}else{
			return 0;
		}
	}
	
	/*写SPS头数据*/
	public static int writeSPS(byte[] data, int offset, int len, int type){
		final byte[] sps4qcif1={0x27,0x42,0x00,0x20,(byte)0xA6,(byte)0x82,(byte)0xC4,(byte)0xC4};	  //For HTCC510E  QCIF 320x240
		final byte[] sps4qvga1={0x27,0x42,0x00,0x20,(byte)0xA6,(byte)0x81,0x41,(byte)0xF1};      	  //For HTCC510E  QVGA 320x240
		final byte[] sps4cif1 ={0x27,0x42,0x00,0x20,(byte)0xA6,(byte)0x81,0x60,(byte)0x94,0x40}; 	  //For HTCC510E  CIF  352x288
		final byte[] sps4vga1 ={0x27,0x42,0x00,0x20,(byte)0xA6,(byte)0x80,(byte)0xA0,0x3D,0x10}; 	  //For HTCC510E  VGA  640x480
		final byte[] sps4d11  ={0x27,0x42,0x00,0x20,(byte)0xA6,(byte)0x80,(byte)0xB0,0x12,0x42}; 	  //For HTCC510E  D1   704x576
		final byte[] sps4qcif2={0x47,0x42,(byte)0xC0,0x0D,(byte)0xE9,0x05,(byte)0x89,(byte)0xC8};	  //For MIONEPLUS QCIF 320x240
		final byte[] sps4qvga2={0x47,0x42,(byte)0xC0,0x0D,(byte)0xE9,0x02,(byte)0x83,(byte)0xF2};     //For MIONEPLUS QVGA 320x240
		final byte[] sps4cif2 ={0x47,0x42,(byte)0xC0,0x0D,(byte)0xE9,0x02,(byte)0xC1,0x2C,(byte)0x80};//For MIONEPLUS CIF  352x288
		final byte[] sps4vga2 ={0x47,0x42,(byte)0xC0,0x0D,(byte)0xE9,0x01,0x40,0x7B,0x20};	  		  //For MIONEPLUS VGA  640x480
		final byte[] sps4d12  ={0x47,0x42,(byte)0xC0,0x0D,(byte)0xE9,0x01,0x40,0x24,(byte)0xC1};	  //For MIONEPLUS D1   704x576
		byte sps[];
		
		if(TYPE_QCIF==type){
			if(SettingAndStatus.isHtcC510e()){
				sps=sps4qcif1;
			}else if(SettingAndStatus.isMiOnePlus()){
				sps=sps4qcif2;
			}else{ 
				sps=sps4qcif2;
			}
		}else if(TYPE_QVGA==type){
			if(SettingAndStatus.isHtcC510e()){
				sps=sps4qvga1;
			}else if(SettingAndStatus.isMiOnePlus()){
				sps=sps4qvga2;
			}else{ 
				sps=sps4qvga2;
			}
		}else if(TYPE_CIF==type){
			if(SettingAndStatus.isHtcC510e()){
				sps=sps4cif1;
			}else if(SettingAndStatus.isMiOnePlus()){
				sps=sps4cif2;
			}else{ 
				sps=sps4cif2;
			}
		}else if(TYPE_VGA==type){
			if(SettingAndStatus.isHtcC510e()){
				sps=sps4vga1;
			}else if(SettingAndStatus.isMiOnePlus()){
				sps=sps4vga2;
			}else{ 
				sps=sps4vga2;
			}
		}else if(TYPE_D1==type){
			if(SettingAndStatus.isHtcC510e()){
				sps=sps4d11;
			}else if(SettingAndStatus.isMiOnePlus()){
				sps=sps4d12;
			}else{ 
				sps=sps4d12;
			}
		}else{
			return 0;
		}
		int dlen=sps.length;		
		
		if(len>=dlen){
			for(int i=0;i<dlen;i++){
				data[offset+i]=sps[i];
			}
			return dlen;
		}else{
			return 0;
		}
	}
	
	/*写PPS头数据*/
	public static int writePPS(byte[] data, int offset, int len){
		final byte[] pps1={0x28,(byte)0xce,0x3c,(byte)0x80};//For HTCC510E
		final byte[] pps2={0x48,(byte)0xce,0x06,(byte)0xe2};//For MIONEPLUS
		byte[] pps;
		
		if(SettingAndStatus.isHtcC510e()){
			pps=pps1;
		}else if(SettingAndStatus.isMiOnePlus()){
			pps=pps2;
		}else{ 
			pps=pps2;
		}
		int dlen=pps.length;
		
		if(len>=dlen){
			for(int i=0;i<dlen;i++){
				data[offset+i]=pps[i];
			}
			return dlen;
		}else{
			return 0;
		}
	}
	
	/*判断是否为I真*/
	public static boolean isIDR(byte nalhead){
		return 5==(nalhead&0x1F);
	}
	
	/*获取视频的宽和高*/
	public static int getVideoWidth(int videosize){
		if(TYPE_QCIF==videosize){
			return 176;
		}else if(TYPE_QVGA==videosize){
			return 320;
		}else if(TYPE_CIF==videosize){
			return 352;
		}else if(TYPE_VGA==videosize){
			return 640;
		}else if(TYPE_D1==videosize){
			return 704;
		}else{
			return 0;
		}		
	}
	public static int getVideoHeight(int videosize){
		if(TYPE_QCIF==videosize){
			return 144;
		}else if(TYPE_QVGA==videosize){
			return 240;
		}else if(TYPE_CIF==videosize){
			return 288;
		}else if(TYPE_VGA==videosize){
			return 480;
		}else if(TYPE_D1==videosize){
			return 576;
		}else{
			return 0;
		}	
	}
	
	/*将视频尺寸转换成字符串*/
	public static String stringVideoSize(int videosize){
		if(TYPE_QCIF==videosize){
			return "QCIF(176x144)";
		}else if(TYPE_QVGA==videosize){
			return "QVGA(320x240)";
		}else if(TYPE_CIF==videosize){
			return "CIF(352x288)";
		}else if(TYPE_VGA==videosize){
			return "VGA(640x480)";
		}else if(TYPE_D1==videosize){
			return "D1(704x576)";
		}else{
			return "INVALID";
		}		
	}
	
	/*获取最大码流长度*/
	public static int getMaxFrameLength(int videosize){
		if(TYPE_QCIF==videosize){
			return 176*144*3/2;
		}else if(TYPE_QVGA==videosize){
			return 320*240*3/2;
		}else if(TYPE_CIF==videosize){
			return 352*288*3/2;
		}else if(TYPE_VGA==videosize){
			return 640*480*3/2;
		}else if(TYPE_D1==videosize){
			return 704*576*3/2;
		}else{
			return 0;
		}
	}
	
	/*获取帧头长度*/
	public static int getFrameHeadLength(){
		return 4*5;
	}
	
	/*写帧头数据*/
	public static int writeFrameHead(byte[] data, int offset, int len,
		long timestamp, int datatype, int datalen, int frametype, int frameno){
		if(len>=20){
			int size=Utils.longToByte4(timestamp,data,offset);
			size+=Utils.intToByte4(datatype,data,offset+size);
			size+=Utils.intToByte4(datalen,data,offset+size);
			size+=Utils.intToByte4(frametype,data,offset+size);
			size+=Utils.intToByte4(frameno,data,offset+size);
			return size;
		}else{
			return 0;
		}
	}
}
