package com.vdsp;

public class Vca {
	/*对外接口*/
	public static native boolean open(Param param);
	public static native void close();
	public static native boolean start(int priority, short sndport, int sndbufsz, short rcvport, int rcvbufsz, int timeout);
	public static native void stop();
	public static native int control(int opid, Arg arg);
	public static native boolean putVideoData(byte data[], int len, int timeout);
	public static native int getVideoData(byte buf[], int timeout);
	public static native int getVideoData2(int buf[], int timeout);
	public static native boolean putAudioData(byte data[], int len, int timeout);
	public static native int getAudioData(byte buf[], int timeout);
	public static native boolean sendCommand(int cmdid, byte param[], int plen, byte pdata[], int dlen, int seqno, int timeout);
	public static native boolean sendAck(byte param[], int plen, byte pdata[], int dlen, int seqno);
	public static native boolean getMessage(Message msg, int timeout);
	public static native void updateVideoBitrate(int bytecnt);
	public static native void updateAudioBitrate(int bytecnt);
	public static native int getVideoBitrate();
	public static native int getAudioBitrate();
	public static native int getSeqno();

	/*控制操作的ID号*/
	public final static int OPID_ADDDST=0;
	public final static int OPID_DELDST=1;
	public final static int OPID_CLRDST=2;
	public final static int OPID_GETDST=3;
	public final static int OPID_GETSNDPORT=4;
	public final static int OPID_GETRCVPORT=5;
	public final static int OPID_ADDSRC=8;
	public final static int OPID_CLRSRC=9;
	public final static int OPID_SETSRCID=10;
	public final static int OPID_CHGDESTSIZE=15;
	
	/*VCA模块创建参数*/
	public class Param{
		public int  	deviceid;
		public int  	devicetype;
		public int  	srvipaddr;
		public short	srvport;
		public short	reserved;
		public byte		sourceid;
		public byte 	classid;
		/* FOR SPEEX MODULE DOWN */
		public byte		speexen;
		public int		encmode;
		public int		isplrt;
		public int		decmode;
		public int		osplrt;
		/* FOR SPEEX MODULE UP */
		/* FOR H264DEC MODULE DOWN */
		public byte		h264decen;
		public int		maxwidth;
		public int		maxheight;
		public int		colorspace;
		public int		dispwidth;
		public int		dispheight;
		/* FOR H264DEC MODULE UP */
		public int		hbperiod;
		public int		hbtimeout;
		public int		msglen;
		public int		msgcnt;
		public int		buflen;
		public int 		bufcnt;
		public int 		videofrmlen;
		public int 		videofrmcnt;
		public int 		audiofrmlen;
		public int 		aduiofrmcnt;
		
		/*默认参数*/
		public Param(){
			deviceid=9000;
			devicetype=43533;
			srvipaddr=(((((247<<8)+4)<<8)+168)<<8)+192;
			srvport=10014;
			sourceid=0;
			classid=0;
			/* FOR SPEEX MODULE DOWN */
			speexen=0;
			encmode=0;
			isplrt=8000;
			decmode=0;
			osplrt=8000;
			/* FOR SPEEX MODULE DOWN */
			/* FOR H264DEC MODULE DOWN */
			h264decen=0;
			maxwidth=704;
			maxheight=576;
			colorspace=1;//0-yuv420p,1-rgb565
			dispwidth=0; 
			dispheight=0;
			/* FOR H264DEC MODULE UP */
			hbperiod=10;
			hbtimeout=100;
			msglen=1024;
			msgcnt=16;
			buflen=1024;
			bufcnt=64;
			videofrmlen=1024*1024*2;//YUV420P-W*H*3/2,BGR0-W*H*4
			videofrmcnt=4;
			audiofrmlen=1024*8;
			aduiofrmcnt=8;
		}
	}

	/*VCA模块控制操作参数*/
	public class Arg{
		public int	 count;
		public int   ip[];
		public short port[];
		public int   srcid;
		public int	 width;
		public int	 height;
		public Arg(int cnt, int id){
			if(cnt>0){
				count=cnt;
				ip=new int[count];
				port=new short[count];
			}
			if(id>0){
				srcid=id;
			}
		}
		public void setWxH(int w,int h){  
			width=w;
			height=h;
		}
	}
	
	/*VCA模块输出统一消息*/
	public class Message{
		public int  type;
		public byte param[];
		public int  plen;
		public byte data[];
		public int  dlen;
		public Message(int paramlen,int datalen){
			type=-1;
			plen=paramlen;
			dlen=datalen;
			param=new byte[plen];
			data=new byte[dlen];
		}
	}
    
	static {
		System.loadLibrary("Vca");
	}
}