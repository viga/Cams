package com.vdsp;

public class CmdId {
	public static final int CMDACK=1;
	public static final int ACK=0x1000;
	public static final int REQUEST_SENDDATA=0x1006;
	public static final int MOTION_CTRL=0x1015;
	public static final int REQUEST_GETRTVLIST=0x2001;
	public static final int REQUEST_PLAYRTV=0x2002;
	public static final int REQUEST_UPLOADSNAP=0x2003;
	public static final int STOP_PLAYRTV=0x2005;
	public static final int STOP_SENDDATA=0x2006;
	public static final int NOTICE_RECVDATA=0x2007;
	public static final int NOTICE_UPLOADSNAP=0x2008;
	public static final int GET_VIDEOQUALITY=0x2100;
	public static final int ADJUST_VIDEOQUALITY=0x2101;
	public static final int GET_ENCODERPARAM=0x2102;
	public static final int ADJUST_ENCODERPARAM=0x2103;
}
