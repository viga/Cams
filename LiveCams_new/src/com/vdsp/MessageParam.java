package com.vdsp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class MessageParam {
	public boolean	 flag[];
	public int 	  	 type;
	public int    	 id;
	public final int CMD=0;
	public String 	 cmd;
	public final int PORT=1;
	public short  	 port;
	public final int ERRNO=2;
	public int	  	 errno;
	public final int TIMECNT=3;
	public int	  	 timecnt;
	public final int SEQNO=4;
	public int 	  	 seqno;
	public final int SRCID=5;
	public int 	  	 srcid;
	public final int RESULT=6;
	public int 	  	 result;
	public final int SRVTYPE=7;
	public int		 srvtype;
	public final int RTVLIST=8;
	public final int RTVMAXCNT=3;
	public int		 rtvlist[];
	public final int PICCNT=9;
	public final int PICMAXCNT=3;
	public int		 piccnt;
	public final int PICTYPE=10;
	public int		 pictype[];
	public final int PICLEN=11;
	public int		 piclen[];
	public final int DATETIME=12;
	public String	 datetime;
	public final int GPS=13;
	public String    gps;
	public final int BRIGHTNESS=14;
	public int		 brightness;
	public final int HUE=15;
	public int		 hue;
	public final int CONTRAST=16;
	public int		 contrast;
	public final int SATURATION=17;
	public int		 saturation;
	public final int VIDEOSIZE=18;
	public int		 videosize;
	public final int BITRATE=19;
	public int		 bitrate;
	public final int FRAMERATE=20;
	public int		 framerate;
	public final int MAXFRAMERATE=21;
	public int		 maxframerate;
	public final int INTERVAL=22;
	public int		 interval;
	public final int COUNT=23;
	public MessageParam(){
		flag=new boolean[COUNT];
		rtvlist=new int[RTVMAXCNT];
		for(int i=0;i<RTVMAXCNT;i++){
			rtvlist[i]=-1;
		}
		pictype=new int[PICMAXCNT];
		piclen=new int[PICMAXCNT];
		for(int i=0;i<PICMAXCNT;i++){
			pictype[i]=-1;
		}
	}
	public void reset(){
		for(int i=0;i<COUNT;i++){
			flag[i]=false;
		}
		for(int i=0;i<RTVMAXCNT;i++){
			rtvlist[i]=-1;
		}
		for(int i=0;i<PICMAXCNT;i++){
			pictype[i]=-1;
			piclen[i]=0;
		}
	}
	public boolean parserNotice(byte param[]){
		boolean ret=false;
		XmlPullParser pull=Xml.newPullParser();
		InputStream xml=new ByteArrayInputStream(param);
		try{
			pull.setInput(xml,"utf-8");
			int event=pull.getEventType();
			while(event!=XmlPullParser.END_DOCUMENT){
				switch(event){
				case XmlPullParser.START_DOCUMENT:
					type=0;
					break;
				case XmlPullParser.END_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					String tagName=pull.getName();
					if("notice".equals(tagName)){
						id=Integer.parseInt(pull.getAttributeValue(0).substring(2),16);
						ret=true;
					}else if("result".equals(tagName)){
						result=Integer.parseInt(pull.nextText());
						flag[RESULT]=true;
					}else if("type".equals(tagName)){
						srvtype=Integer.parseInt(pull.nextText());
						flag[SRVTYPE]=true;
					}else if("errno".equals(tagName)){
						errno=Integer.parseInt(pull.nextText());
						flag[ERRNO]=true;
					}else if("seqno".equals(tagName)){
						seqno=Integer.parseInt(pull.nextText());
						flag[SEQNO]=true;
					}else if("timecnt".equals(tagName)){
						timecnt=Integer.parseInt(pull.nextText());
						flag[TIMECNT]=true;
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				event=pull.next();
			}
		}catch(XmlPullParserException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return(ret);
	}
	public boolean parserCmdAck(byte param[]){
		boolean ret=true;
		XmlPullParser pull=Xml.newPullParser();
		InputStream xml=new ByteArrayInputStream(param);
		try{				
			pull.setInput(xml,"utf-8");
			int event=pull.getEventType();
			while(event!=XmlPullParser.END_DOCUMENT){
				switch(event){
				case XmlPullParser.START_DOCUMENT:
					type=1;
					break;
				case XmlPullParser.END_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					String tagName=pull.getName();
					if("request_recvdata".equals(tagName)){
						id=Integer.parseInt(pull.getAttributeValue(0).substring(2),16);
						seqno=Integer.parseInt(pull.getAttributeValue(1));
						flag[SEQNO]=true;
						ret=true;
					}else if("cmd_ack".equals(tagName)){
						id=Integer.parseInt(pull.getAttributeValue(0).substring(2),16);
						seqno=Integer.parseInt(pull.getAttributeValue(1));
						flag[SEQNO]=true;
						cmd=pull.getAttributeValue(2);
						flag[CMD]=true;
						ret=true;
					}else if("port".equals(tagName)){
						port=(short)Integer.parseInt(pull.nextText());
						flag[PORT]=true;
					}else if("source_id".equals(tagName)){
						srcid=Integer.parseInt(pull.nextText());
						flag[SRCID]=true;
					}else if("result".equals(tagName)){
						result=Integer.parseInt(pull.nextText());
						flag[RESULT]=true;
					}else if("pic_count".equals(tagName)){
						piccnt=Integer.parseInt(pull.nextText());
						flag[PICCNT]=true;
					}else if("date_time".equals(tagName)){
						datetime=pull.nextText();
						flag[DATETIME]=true;
					}else if("gps".equals(tagName)){
						gps=pull.nextText();
						flag[GPS]=true;
					}else if("brightness".equals(tagName)){
						brightness=Integer.parseInt(pull.nextText());
						flag[BRIGHTNESS]=true;
					}else if("hue".equals(tagName)){
						hue=Integer.parseInt(pull.nextText());
						flag[HUE]=true;
					}else if("contrast".equals(tagName)){
						contrast=Integer.parseInt(pull.nextText());
						flag[CONTRAST]=true;
					}else if("saturation".equals(tagName)){
						saturation=Integer.parseInt(pull.nextText());
						flag[SATURATION]=true;
					}else if("videosize".equals(tagName)){
						videosize=Integer.parseInt(pull.nextText());
						flag[VIDEOSIZE]=true;
					}else if("bitrate".equals(tagName)){
						bitrate=Integer.parseInt(pull.nextText());
						flag[BITRATE]=true;
					}else if("framerate".equals(tagName)){
						framerate=Integer.parseInt(pull.nextText());
						flag[FRAMERATE]=true;
					}else if("maxframerate".equals(tagName)){
						maxframerate=Integer.parseInt(pull.nextText());
						flag[MAXFRAMERATE]=true;
					}else if("interval".equals(tagName)){
						interval=Integer.parseInt(pull.nextText());
						flag[INTERVAL]=true;
					}else if(flag[CMD]){
						if(cmd.equals("request_getrtvlist")){
							for(int i=0;i<RTVMAXCNT;i++){
								if(("video"+i).equals(tagName)){
									rtvlist[i]=Integer.parseInt(pull.nextText());
									flag[RTVLIST]=true;
									break;
								}
							}
						}else if(cmd.equals("notice_uploadsnap")){
							for(int i=0;i<PICMAXCNT;i++){
								if(("pic_type"+i).equals(tagName)){
									pictype[i]=Integer.parseInt(pull.nextText());
									flag[PICTYPE]=true;
									break;
								}else if(("pic_len"+i).equals(tagName)){
									piclen[i]=Integer.parseInt(pull.nextText());
									flag[PICLEN]=true;
									break;
								}
							}
						}
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				event=pull.next();
			}
		}catch(XmlPullParserException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return(ret);
	}
}
