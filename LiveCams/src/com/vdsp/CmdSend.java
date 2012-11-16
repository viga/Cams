package com.vdsp;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

import com.vdsp.CmdRecMgr.CmdRec;

public class CmdSend {
	private final static String TAG="LIVECAMS";
	private final static int	TIMEOUT=3000;//UNIT:ms
	private static CmdRecMgr	cmdMgr=new CmdRecMgr();
	private static Vca			vca=new Vca();
    
	/*发送请求上传数据命令*/
    public static boolean requestSendData(int video,boolean audioen,boolean videoen,boolean record){
		if(SettingAndStatus.VcaStatus.LOGINED==SettingAndStatus.vcaStatus.status){
			int seqno=Vca.getSeqno();
			String cmdparam="<vms_msg>" +
								"<request_senddata id=\"0x"+Integer.toHexString(CmdId.REQUEST_SENDDATA)+"\" seqno=\""+seqno+"\">" +
									"<id>0"+video+"</id>"+
									"<audio>"+(audioen?1:0)+"</audio>"+
									"<video>"+(videoen?1:0)+"</video>"+
								"</request_senddata>"+
							"</vms_msg>\0";
			if(!Vca.sendCommand(CmdId.REQUEST_SENDDATA,cmdparam.getBytes(),
				cmdparam.length(),null,0,seqno,TIMEOUT)){
				Log.v(TAG,"请求上传数据异常！");
				DispatchHandler.submitLongNotice("警告：命令发送异常！");
			}else{
				if(record){
					CmdRec cmd=cmdMgr.new CmdRec("request_senddata",seqno,
						0==video?"手机视频":"背负设备"+(1==video?"前置视频":"后置视频"));
					CmdRecMgr.addCmdRec(cmd);
				}
				return(true);
			}
		}else{
			Log.v(TAG,"未登录服务器！");
			DispatchHandler.submitLongNotice("提示：未登录服务器，视频无法上传！");
		}
		return(false);
    }
    
    /*发送停止上传数据命令*/
    public static boolean stopSendData(boolean record){
		if(SettingAndStatus.VcaStatus.LOGINED==SettingAndStatus.vcaStatus.status){
	    	int seqno=Vca.getSeqno();
			String cmdparam="<vms_msg>" +
								"<stop_senddata id=\"0x"+Integer.toHexString(CmdId.STOP_SENDDATA)+"\" seqno=\""+seqno+"\">" +
								"</stop_senddata>" +
							"</vms_msg>\0";
			if(!Vca.sendCommand(CmdId.STOP_SENDDATA,cmdparam.getBytes(),
				cmdparam.length(),null,0,seqno,TIMEOUT)){
				Log.v(TAG,"停止上传数据异常！");
				DispatchHandler.submitLongNotice("警告：命令发送异常！");
			}else{
				if(record){
					CmdRec cmd=cmdMgr.new CmdRec("stop_senddata",seqno,null);
					CmdRecMgr.addCmdRec(cmd);
				}
				return(true);
			}
		}
		return(false);
    }
    
	/*发送请求实时播放列表命令*/
    public static boolean requestGetRtvList(boolean record){
		if(SettingAndStatus.VcaStatus.LOGINED==SettingAndStatus.vcaStatus.status){
			int seqno=Vca.getSeqno();
			String cmdparam="<vms_msg>" +
								"<request_getrtvlist id=\"0x"+Integer.toHexString(CmdId.REQUEST_GETRTVLIST)+"\" seqno=\""+seqno+"\">" +
								"</request_getrtvlist>" + 
							"</vms_msg>\0";
			if(!Vca.sendCommand(CmdId.REQUEST_GETRTVLIST,cmdparam.getBytes(),
				cmdparam.length(),null,0,seqno,TIMEOUT)){
				Log.v(TAG,"请求实时播放列表异常！");
				DispatchHandler.submitLongNotice("警告：命令发送异常！");
			}else{
				if(record){
					CmdRec cmd=cmdMgr.new CmdRec("request_getrtvlist",seqno,null);
					CmdRecMgr.addCmdRec(cmd);
				}
				return(true);
			}
		}else{
			Log.v(TAG,"未登录服务器，请先登录！");
			DispatchHandler.submitLongNotice("提示：未登录服务器，请先登录！");
		}
		return(false);
    }
    
    /*发送请求播放指定实时视频命令*/
    public static boolean requestPlayRtv(int id,String video,boolean record){
    	if(SettingAndStatus.VcaStatus.LOGINED==SettingAndStatus.vcaStatus.status){
			Vca.Arg arg=vca.new Arg(-1,-1);
			int port=Vca.control(Vca.OPID_GETRCVPORT,arg);
			if(0!=port){
				int seqno=Vca.getSeqno();
				String cmdparam="<vms_msg>" +
									"<request_playrtv id=\"0x"+Integer.toHexString(CmdId.REQUEST_PLAYRTV)+"\" seqno=\""+seqno+"\">" +
										"<video>"+id+"</video>" +
										"<port>"+port+"</port>" +
									"</request_playrtv>" +
								"</vms_msg>\0";
				if(!Vca.sendCommand(CmdId.REQUEST_PLAYRTV,cmdparam.getBytes(),
					cmdparam.length(),null,0,seqno,TIMEOUT)){
					Log.v(TAG,"播放实时视频异常！");
					DispatchHandler.submitLongNotice("警告：命令发送异常！");
				}else{
					if(record){
						CmdRec cmd=cmdMgr.new CmdRec("request_playrtv",seqno,video);
						CmdRecMgr.addCmdRec(cmd);
					}
					return(true);
				}
			}
    	}
    	return(false);
    }
    
    /*发送停止播放实时视频命令*/
    public static boolean stopPlayRtv(boolean record){
    	if(SettingAndStatus.VcaStatus.LOGINED==SettingAndStatus.vcaStatus.status){
			int seqno=Vca.getSeqno();
			String cmdparam="<vms_msg>" +
								"<stop_playrtv id=\"0x"+Integer.toHexString(CmdId.STOP_PLAYRTV)+"\" seqno=\""+seqno+"\">" +
								"</stop_playrtv>" +
							"</vms_msg>\0";
			if(!Vca.sendCommand(CmdId.STOP_PLAYRTV,cmdparam.getBytes(),
				cmdparam.length(),null,0,seqno,TIMEOUT)){
				Log.v(TAG,"停播实时视频异常！");
				DispatchHandler.submitLongNotice("警告：命令发送异常！");
			}else{
				if(record){
					CmdRec cmd=cmdMgr.new CmdRec("stop_playrtv",seqno,null);
					CmdRecMgr.addCmdRec(cmd);
				}
				return(true);
			}
    	}
    	return(false);
    }
    
    /*发送上传抓拍图片命令*/
    public static boolean uploadSnap(Date date,byte[] pic,int length,boolean record){
		if(SettingAndStatus.VcaStatus.LOGINED==SettingAndStatus.vcaStatus.status){
			int seqno=Vca.getSeqno();
			SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
			String datetime=format.format(date).toString();
			String cmdparam="<vms_msg>" +
								"<request_uploadsnap id=\"0x"+Integer.toHexString(CmdId.REQUEST_UPLOADSNAP)+"\" seqno=\""+seqno+"\">" +
									"<date_time>"+datetime+"</date_time>" +
									"<gps></gps>" +
									"<pic_count>1</pic_count>" +
									"<pic_type0>0</pic_type0>" +
									"<pic_len0>"+length+"</pic_len0>" +
								"</request_uploadsnap>" +
							"</vms_msg>\0";
			int timeout=(int)((float)(cmdparam.length()+length)*(float)(1000*8)/
				(float)SettingAndStatus.settings.bandwidth);//UNIT:ms
			timeout=timeout>TIMEOUT?timeout:TIMEOUT;
			if(!Vca.sendCommand(CmdId.REQUEST_UPLOADSNAP,cmdparam.getBytes(),
				cmdparam.length(),pic,length,seqno,timeout)){
				Log.v(TAG,"上传抓拍图片异常！");
				DispatchHandler.submitLongNotice("警告：命令发送异常！");
			}else{
				if(record){
					CmdRec cmd=cmdMgr.new CmdRec("request_uploadsnap",seqno,
						(new SimpleDateFormat("yyyyMMddHHmmssSSS")).format(date)+
						".jpg/"+length/1024+"KB");
					CmdRecMgr.addCmdRec(cmd);
				}
				return(true);
			}
		}
    	return(false);
    }
    
    /*发送通知背负设备上传抓拍命令*/
    public static boolean noticeSnap(int count,boolean record){
		if(SettingAndStatus.VcaStatus.LOGINED==SettingAndStatus.vcaStatus.status){
			int seqno=Vca.getSeqno(); 
			Date date=new Date();
			SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
			String datetime=format.format(date).toString();
			String cmdparam="<vms_msg>" +
								"<notice_uploadsnap id=\"0x"+Integer.toHexString(CmdId.NOTICE_UPLOADSNAP)+"\" seqno=\""+seqno+"\">"+
									"<date_time>"+datetime+"</date_time>"+
									"<pic_count>"+count+"</pic_count>"+
								"</notice_uploadsnap>"+
							"</vms_msg>\0";
			if(!Vca.sendCommand(CmdId.NOTICE_UPLOADSNAP,cmdparam.getBytes(),
				cmdparam.length(),null,0,seqno,TIMEOUT)){
				Log.v(TAG,"通知背负上传抓拍图片异常！");
				DispatchHandler.submitLongNotice("警告：命令发送异常！");
			}else{
				if(record){
					format=new SimpleDateFormat("yyyyMMddHHmmssSSS");
					datetime=format.format(date).toString();
					CmdRec cmd=cmdMgr.new CmdRec("notice_uploadsnap",seqno,datetime);
					CmdRecMgr.addCmdRec(cmd);
				}
				return(true);
			}
		}
    	return(false);
    }
    
    /*发送获取视频质量参数命令*/
    public static boolean getVideoQuality(boolean record){
    	if(SettingAndStatus.VcaStatus.LOGINED==SettingAndStatus.vcaStatus.status){
			int seqno=Vca.getSeqno(); 
			String cmdparam="<vms_msg>" +
								"<get_videoquality id=\"0x"+Integer.toHexString(CmdId.GET_VIDEOQUALITY)+"\" seqno=\""+seqno+"\">"+
								"</get_videoquality>"+
							"</vms_msg>\0";
			if(!Vca.sendCommand(CmdId.GET_VIDEOQUALITY,cmdparam.getBytes(),
				cmdparam.length(),null,0,seqno,TIMEOUT)){
				Log.v(TAG,"获取视频质量参数异常！");
				DispatchHandler.submitLongNotice("警告：命令发送异常！");
			}else{
				if(record){
					CmdRec cmd=cmdMgr.new CmdRec("get_videoquality",seqno,null);
					CmdRecMgr.addCmdRec(cmd);
				}
				return(true);
			}
    	}
    	return(false);
    }
    
    /*发送视频微调命令*/
    public static boolean adjustVideoQuality(int brightness,int hue,int contrast,int saturation,boolean record){
    	if(SettingAndStatus.VcaStatus.LOGINED==SettingAndStatus.vcaStatus.status){
			int seqno=Vca.getSeqno(); 
			String cmdparam="<vms_msg>" +
								"<adjust_videoquality id=\"0x"+Integer.toHexString(CmdId.ADJUST_VIDEOQUALITY)+"\" seqno=\""+seqno+"\">"+
									"<brightness>"+brightness+"</brightness>"+
									"<hue>"+hue+"</hue>"+
									"<contrast>"+contrast+"</contrast>"+
									"<saturation>"+saturation+"</saturation>"+
								"</adjust_videoquality>"+
							"</vms_msg>\0";
			if(!Vca.sendCommand(CmdId.ADJUST_VIDEOQUALITY,cmdparam.getBytes(),
				cmdparam.length(),null,0,seqno,TIMEOUT)){
				Log.v(TAG,"视频亮度色度等微调异常！");
				DispatchHandler.submitLongNotice("警告：命令发送异常！");
			}else{
				if(record){
					CmdRec cmd=cmdMgr.new CmdRec("adjust_videoquality",seqno,null);
					CmdRecMgr.addCmdRec(cmd);
				}
				return(true);
			}
    	}
    	return(false);
    }
    
    /*发送获取视频编码参数命令*/
    public static boolean getEncoderParam(boolean record){
    	if(SettingAndStatus.VcaStatus.LOGINED==SettingAndStatus.vcaStatus.status){
			int seqno=Vca.getSeqno(); 
			String cmdparam="<vms_msg>" +
								"<get_encoderparam id=\"0x"+Integer.toHexString(CmdId.GET_ENCODERPARAM)+"\" seqno=\""+seqno+"\">"+
								"</get_encoderparam>"+
							"</vms_msg>\0";
			if(!Vca.sendCommand(CmdId.GET_ENCODERPARAM,cmdparam.getBytes(),
				cmdparam.length(),null,0,seqno,TIMEOUT)){
				Log.v(TAG,"获取视频编码参数异常！");
				DispatchHandler.submitLongNotice("警告：命令发送异常！");
			}else{
				if(record){
					CmdRec cmd=cmdMgr.new CmdRec("get_encoderparam",seqno,null);
					CmdRecMgr.addCmdRec(cmd);
				}
				return(true);
			}
    	}
    	return(false);
    }

    /*发送视频编码参数调节命令*/
    public static boolean adjustEncoderParam(int size,int bitrate,int framerate,int interval,boolean record){
    	if(SettingAndStatus.VcaStatus.LOGINED==SettingAndStatus.vcaStatus.status){
			int seqno=Vca.getSeqno(); 
			String cmdparam="<vms_msg>" +
								"<adjust_encoderparam id=\"0x"+Integer.toHexString(CmdId.ADJUST_ENCODERPARAM)+"\" seqno=\""+seqno+"\">"+
									"<videosize>"+size+"</videosize>"+
									"<bitrate>"+bitrate+"</bitrate>"+
									"<framerate>"+framerate+"</framerate>"+
									"<interval>"+interval+"</interval>"+
								"</adjust_encoderparam>"+
							"</vms_msg>\0";
			if(!Vca.sendCommand(CmdId.ADJUST_ENCODERPARAM,cmdparam.getBytes(),
				cmdparam.length(),null,0,seqno,TIMEOUT)){
				Log.v(TAG,"调节视频编码器参数异常！");
				DispatchHandler.submitLongNotice("警告：命令发送异常！");
			}else{
				if(record){
					CmdRec cmd=cmdMgr.new CmdRec("adjust_encoderparam",seqno,null);
					CmdRecMgr.addCmdRec(cmd);
				}
				return(true);
			}
    	}
    	return(false);
    }

    /*发送云台控制命令控制背负设备上的云台*/
    public static boolean moveToUp(){
    	return motionControl(0,0,false);
    }
    public static boolean moveToDown(){
    	return motionControl(1,0,false);
    }
    public static boolean moveToLeft(){
    	return motionControl(2,0,false);
    }
    public static boolean moveToRight(){
    	return motionControl(3,0,false);
    }
    public static boolean focusToFar(){
    	return motionControl(4,0,false);
    }
    public static boolean focusToNear(){
    	return motionControl(4,1,false);
    }
    public static boolean moveFaster(){
    	return motionControl(5,0,false);
    }
    public static boolean moveSlower(){
    	return motionControl(5,1,false);
    }
    public static boolean brightenAperture(){
    	return motionControl(7,0,false);
    }
    public static boolean darkenAperture(){
    	return motionControl(7,1,false);
    }
    public static boolean stopMotionCtrl(){
    	return motionControl(6,0,false);
    }
    private static boolean motionControl(int param,int value,boolean record){
		if(SettingAndStatus.VcaStatus.LOGINED==SettingAndStatus.vcaStatus.status){
			int seqno=Vca.getSeqno(); 
			String cmdparam="<vms_msg>" +
								"<motion_ctrl id=\"0x"+Integer.toHexString(CmdId.MOTION_CTRL)+"\" seqno=\""+seqno+"\">"+
									"<param>"+param+"</param>"+
									"<value>"+value+"</value>"+
								"</motion_ctrl>"+
							"</vms_msg>\0";
			if(!Vca.sendCommand(CmdId.MOTION_CTRL,cmdparam.getBytes(),
				cmdparam.length(),null,0,seqno,TIMEOUT)){
				Log.v(TAG,"控制背负设备上的云台异常！");
				DispatchHandler.submitLongNotice("警告：命令发送异常！");
			}else{
				if(record){
					CmdRec cmd=cmdMgr.new CmdRec("motion_ctrl",seqno,null);
					CmdRecMgr.addCmdRec(cmd);
				}
				return(true);
			}
		}
    	return(false);
    }
}
