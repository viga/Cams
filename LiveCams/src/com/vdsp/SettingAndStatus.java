package com.vdsp;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.DisplayMetrics;

public class SettingAndStatus {
	public static VcaStatus 	vcaStatus;
	public static boolean		msgProcStatus;
	public static boolean 		phoneDataSending;
	public static boolean   	deviceDataSending;	
	public static boolean   	dataReceiving;
	public static boolean		avrecording;
	public static boolean   	isrecordmode;
	public static boolean		isplaymode;
	public static int			displaywidth;
	public static int			displayheight;
	public static VideoQuality  videoQuality;
	public static EncoderParam 	encoderParam;
	public static Date 			beginTime;
	public static Date 			loginTime;
	public static Settings		settings;
	private static DataBaseHelper db;
	
	/*初始化状态和设置*/
	public static void init(Context context){
		DisplayMetrics dm=new DisplayMetrics();
		dm=context.getApplicationContext().getResources().getDisplayMetrics();
		SettingAndStatus ss=new SettingAndStatus();
		vcaStatus=ss.new VcaStatus();
		videoQuality=ss.new VideoQuality();
		encoderParam=ss.new EncoderParam();
		msgProcStatus=false;
		phoneDataSending=false;
		deviceDataSending=false;
		dataReceiving=false;
		avrecording=false;
		isrecordmode=false;
		isplaymode=false;
		displaywidth=dm.widthPixels;
		displayheight=dm.heightPixels;
		beginTime=new Date();
		settings=ss.new Settings();
		db=ss.new DataBaseHelper(context);
		db.getWritableDatabase();
	}
	
	/*退出关闭数据库*/
    public static void exit(){
    	SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String begin=format.format(beginTime);
		String end=format.format(new Date());
		String sql="INSERT INTO runbook(begin,end) VALUES";
		db.getWritableDatabase().execSQL(sql+"('"+begin+"','"+end+"');");
    	db.close();
    }
    
    /*记录登陆情况*/
    public static void insertLogRecord(Date in,Date out){
    	SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String s1=format.format(in);
		String s2=format.format(out);
		String sql="INSERT INTO logbook(begin,end) VALUES";
		db.getWritableDatabase().execSQL(sql+"('"+s1+"','"+s2+"');");
    }
    
    /*加载设置*/
	public static boolean load(SQLiteDatabase db){
		if(null==db){
			db=SettingAndStatus.db.getWritableDatabase();
		}
		String sql="SELECT * FROM settings";
		Cursor cur = db.rawQuery(sql,null);
		if(null!=cur){
			while(cur.moveToNext()){
				if(cur.getString(0).equals("devid")){
					settings.devid=cur.getInt(1);
				}else if(cur.getString(0).equals("srvipaddr")){
					settings.srvipaddr=cur.getInt(1);
				}else if(cur.getString(0).equals("srvport")){
					settings.srvport=(short)cur.getInt(1);
				}else if(cur.getString(0).equals("rcvport")){
					settings.rcvport=(short)cur.getInt(1);
				}else if(cur.getString(0).equals("sndport")){
					settings.sndport=(short)cur.getInt(1);
				}else if(cur.getString(0).equals("timeout")){
					settings.timeout=cur.getInt(1);
				}else if(cur.getString(0).equals("autologin")){
					settings.autologin=1==cur.getInt(1);
				}else if(cur.getString(0).equals("logincheck")){
					settings.logincheck=1==cur.getInt(1);
				}else if(cur.getString(0).equals("logoutcheck")){
					settings.logoutcheck=1==cur.getInt(1);
				}else if(cur.getString(0).equals("avrecord")){
					settings.avrecord=1==cur.getInt(1);
				}else if(cur.getString(0).equals("videosize")){
					settings.videosize=cur.getInt(1);
				}else if(cur.getString(0).equals("vframerate")){
					settings.vframerate=cur.getInt(1);
				}else if(cur.getString(0).equals("vbitrate")){
					settings.vbitrate=cur.getInt(1);
				}else if(cur.getString(0).equals("speexen")){
					settings.speexen=(byte)cur.getInt(1);
				}else if(cur.getString(0).equals("encmode")){
					settings.encmode=cur.getInt(1);
				}else if(cur.getString(0).equals("decmode")){
					settings.decmode=cur.getInt(1);
				}else if(cur.getString(0).equals("isplrt")){
					settings.isplrt=cur.getInt(1);
				}else if(cur.getString(0).equals("osplrt")){
					settings.osplrt=cur.getInt(1);
				}else if(cur.getString(0).equals("h264decen")){
					settings.h264decen=(byte)cur.getInt(1);
				}else if(cur.getString(0).equals("fullscreen")){
					settings.fullscreen=1==cur.getInt(1);
				}else if(cur.getString(0).equals("picwidth")){
					settings.picwidth=cur.getInt(1);
				}else if(cur.getString(0).equals("picheight")){
					settings.picheight=cur.getInt(1);
				}else if(cur.getString(0).equals("picquality")){
					settings.picquality=cur.getInt(1);
				}else if(cur.getString(0).equals("picsave")){
					settings.picsave=1==cur.getInt(1);
				}else if(cur.getString(0).equals("bandwidth")){
					settings.bandwidth=cur.getInt(1);
				}else if(cur.getString(0).equals("audioduplex0")){
					settings.audioduplex0=1==cur.getInt(1);
				}else if(cur.getString(0).equals("audioduplex1")){
					settings.audioduplex1=1==cur.getInt(1);
				}else if(cur.getString(0).equals("mdaport")){
					settings.mdaport=(short)cur.getInt(1);
				}
			}
		}else{
			settings.reset();
			save(db);
		}
    	return true;
    }
	
	/*修改设置*/
	public static void modify(String name,int value){
		String sql="UPDATE settings SET value=? WHERE name=?";
        Object[] item={value,name};
        db.getWritableDatabase().execSQL(sql,item);
	}
	
	/*保存设置*/
	public static void save(SQLiteDatabase db){
		if(null==db){
			db=SettingAndStatus.db.getWritableDatabase();
		}
		String sql="INSERT INTO settings(name,value) VALUES";
		db.execSQL(sql+"('devid',"		 +settings.devid+");");
		db.execSQL(sql+"('srvipaddr'," 	 +settings.srvipaddr+");");
		db.execSQL(sql+"('srvport',"	 +settings.srvport+");");
		db.execSQL(sql+"('rcvport',"	 +settings.rcvport+");");
		db.execSQL(sql+"('sndport',"	 +settings.sndport+");");
		db.execSQL(sql+"('timeout',"	 +settings.timeout+");");
		db.execSQL(sql+"('autologin',"	 +(settings.autologin?1:0)+");");
		db.execSQL(sql+"('logincheck',"  +(settings.logincheck?1:0)+");");
		db.execSQL(sql+"('logoutcheck'," +(settings.logoutcheck?1:0)+");");
		db.execSQL(sql+"('avrecord',"	 +(settings.avrecord?1:0)+");");
		db.execSQL(sql+"('videosize',"	 +settings.videosize+");");
		db.execSQL(sql+"('vframerate',"	 +settings.vframerate+");");
		db.execSQL(sql+"('vbitrate',"	 +settings.vbitrate+");");
		db.execSQL(sql+"('speexen',"	 +settings.speexen+");");
		db.execSQL(sql+"('encmode',"	 +settings.encmode+");");
		db.execSQL(sql+"('decmode',"	 +settings.decmode+");");
		db.execSQL(sql+"('isplrt',"		 +settings.isplrt+");");
		db.execSQL(sql+"('osplrt',"		 +settings.osplrt+");");
		db.execSQL(sql+"('h264decen',"   +settings.h264decen+");");
		db.execSQL(sql+"('fullscreen',"  +(settings.fullscreen?1:0)+");");
		db.execSQL(sql+"('picwidth',"	 +settings.picwidth+");");
		db.execSQL(sql+"('picheight',"	 +settings.picheight+");");
		db.execSQL(sql+"('picquality',"	 +settings.picquality+");");
		db.execSQL(sql+"('picsave',"	 +(settings.picsave?1:0)+");");
		db.execSQL(sql+"('bandwidth',"	 +settings.bandwidth+");");
		db.execSQL(sql+"('audioduplex0',"+(settings.audioduplex0?1:0)+");");
		db.execSQL(sql+"('audioduplex1',"+(settings.audioduplex1?1:0)+");");
		db.execSQL(sql+"('mdaport',"	 +settings.mdaport+");");
	}
	
	/*检查手机型号*/
	public static boolean isMiOnePlus(){
		return "MI-ONE Plus".equals(Build.MODEL);
	}
	public static boolean isHtcC510e(){
		return "HTC C510e".equals(Build.MODEL);
	}
	
	/*VCA对象状态*/
	public class VcaStatus{
    	public static final int INIT=0;
    	public static final int OPENED=1;
    	public static final int STARTED=2;
    	public static final int CONNECTED=3;
    	public static final int LOGINED=4;
    	public static final int STOPPING=5;
    	public int status;
    	public VcaStatus(){
    		status=INIT;
    	}
    }
	
	/*视频质量参数*/
	public class VideoQuality{
		public int		brightness;
		public int 		hue;
		public int		contrast;
		public int		saturation;
	}
	
	/*视频编码参数*/
	public class EncoderParam{
		public int		videosize;
		public int		bitrate;
		public int  	framerate;
		public int		maxframerate;
		public int		interval;
	}
	
    /*应用设置集合*/
    public class Settings{
    	public int 		devid;
    	public int		bandwidth;
    	public int 		srvipaddr;
    	public short 	srvport;
    	public short	rcvport;
    	public short	sndport;
    	public int		timeout;
     	public boolean  autologin;
     	public boolean  logincheck;
    	public boolean  logoutcheck;
    	public boolean	avrecord;
    	public int		videosize;
    	public int 		vframerate;
    	public int		vbitrate;
		public byte		speexen;
		public int		encmode;
		public int		decmode;
		public int		isplrt;
		public int		osplrt;
		public byte		h264decen;
		public boolean  fullscreen;
    	public int		picwidth;
    	public int		picheight;
    	public int		picquality;
    	public boolean  picsave;
    	public boolean  audioduplex0;//上传时是否双向语音
    	public boolean  audioduplex1;//播放是是否双向语音
    	public int      mdaport;
    	
    	/*复位设置——出厂设置*/
    	public void reset(){
        	settings.devid=9000;
        	settings.srvipaddr=(((((247<<8)+4)<<8)+168)<<8)+192;
        	settings.srvport=10014;
        	settings.rcvport=10000;
        	settings.sndport=10001;
        	settings.timeout=250;
    		settings.autologin=true;
        	settings.logincheck=false;
        	settings.logoutcheck=false;
        	settings.avrecord=false;
        	settings.videosize=H264Stream.TYPE_CIF;
        	settings.vframerate=0;
        	settings.vbitrate=0;
        	settings.speexen=1;
        	settings.encmode=0;
        	settings.decmode=0;
        	settings.isplrt=8000;
        	settings.osplrt=8000;
        	settings.h264decen=1;
        	settings.fullscreen=false;
        	settings.picwidth=0;
        	settings.picheight=0;
        	settings.picquality=50;
        	settings.picsave=false;
        	settings.bandwidth=1000000;
        	settings.audioduplex0=false;
        	settings.audioduplex1=false;
        	settings.mdaport=6789;
        }    
    }
    
    /*设置数据库*/
    private class DataBaseHelper extends SQLiteOpenHelper{
    	private final static String NAME="livecams.db";
    	private final static int VERSION=20120807;
    	
    	public DataBaseHelper(Context context){     
    	    super(context,NAME,null,VERSION);     
    	}
    	
    	@Override  
    	public void onCreate(SQLiteDatabase db){ 
			String strSQL="CREATE TABLE settings(name VARCHAR(32) PRIMARY KEY NOT NULL, value INTEGER);";    
			db.execSQL(strSQL); 
			strSQL="CREATE TABLE logbook(begin DATETIME, end DATETIME);";    
			db.execSQL(strSQL); 
			strSQL="CREATE TABLE runbook(begin DATETIME, end DATETIME);";    
			db.execSQL(strSQL); 
			SettingAndStatus.settings.reset();
	    	save(db);
    	}
    	
    	@Override
    	public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
    		db.execSQL("DROP TABLE IF EXISTS settings");
            this.onCreate(db);            
    	}
    }
}
