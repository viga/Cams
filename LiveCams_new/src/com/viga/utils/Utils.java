package com.viga.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.widget.Toast;

public class Utils {
	private final static String TAG ="UTILS";

    /*将int型数据转换程四段式IP地址*/
    public static String intToIpaddr(int ip){
    	String ipstr=(ip&0xFF)+"."+
    		((ip>>>8)&0xFF)+"."+
    		((ip>>>16)&0xFF)+"."+
    		((ip>>>24)&0xFF);
    	
    	return ipstr;
    }
    
    /*将四段式IP地址转换成int型数据*/
    public static int ipaddrToInt(String ipstr){
    	int[] ip=new int[4];
    	int pos1=ipstr.indexOf(".");
    	int pos2=ipstr.indexOf(".",pos1+1);
    	int pos3=ipstr.indexOf(".",pos2+1);
    	ip[0]=Integer.parseInt(ipstr.substring(0,pos1));
    	ip[1]=Integer.parseInt(ipstr.substring(pos1+1,pos2));
    	ip[2]=Integer.parseInt(ipstr.substring(pos2+1,pos3));
    	ip[3]=Integer.parseInt(ipstr.substring(pos3+1));
    	return (((ip[0])    &0x000000FF)+
    			((ip[1]<<8) &0x0000FF00)+
    			((ip[2]<<16)&0x00FF0000)+
    			((ip[3]<<24)&0xFF000000));
    }
    
    /*将4个byte型数据拼成一个int型数据*/
    public static int byte4ToInt(byte[] b,int offset){
    	int i=0;
    	for(int j=0;j<4;j++){
    		i=(i<<8)+(b[offset+j]&0xFF);
    	}
    	return i;
    }
    
    /*将4个byte型数据拼成一个long型数据*/
    public static long byte4ToLong(byte[] b,int offset){
    	long l=0;
    	for(int j=0;j<4;j++){
    		l=(l<<8)+(b[offset+j]&0xFF);
    	}
    	return l;
    }
    public static SharedPreferences getSP(Context context,String spName){
    	SharedPreferences mysp=context.getSharedPreferences(spName, Context.MODE_PRIVATE);
    	return mysp;
    }
	/*将long型数据转换成长个字节byte型数据*/
    public static int longToByte4(long l,byte[] b,int offset){
		b[offset+0]=new Long(l    ).byteValue();
		b[offset+1]=new Long(l>> 8).byteValue();
		b[offset+2]=new Long(l>>16).byteValue();
		b[offset+3]=new Long(l>>24).byteValue();
		return 4;
    }
    
	/*将int型数据转换成长个字节byte型数据*/
    public static int intToByte4(int i,byte[] b,int offset){
		b[offset+0]=new Integer(i    ).byteValue();
		b[offset+1]=new Integer(i>> 8).byteValue();
		b[offset+2]=new Integer(i>>16).byteValue();
		b[offset+3]=new Integer(i>>24).byteValue();
		return 4;
    }
    
    /*将int型数据的4个字节颠倒下顺序*/
    public static int intSwapByte(int i){
    	byte[] b=new byte[4];
		b[0]=new Integer(i    ).byteValue();
		b[1]=new Integer(i>> 8).byteValue();
		b[2]=new Integer(i>>16).byteValue();
		b[3]=new Integer(i>>24).byteValue();
		return byte4ToInt(b,0);
    }
    
    /*将long型数据的4个字节颠倒下顺序*/
    public static long longSwapByte(long l){
    	byte[] b=new byte[4];
		b[0]=new Long(l    ).byteValue();
		b[1]=new Long(l>> 8).byteValue();
		b[2]=new Long(l>>16).byteValue();
		b[3]=new Long(l>>24).byteValue();
		return byte4ToLong(b,0);
    }
    
    /*扩展的线程类(安全退出)*/
    public class ThreadExt extends Thread{
    	private Object  data;
    	private boolean exit;
		public ThreadExt(Runnable target){
			super(target);
			exit=false;
		}
		public void finish(){
			exit=true;
			try{
				super.join();
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		public boolean isExit(){
			return exit;
		}
		public void setUserData(Object obj){
			data=obj;
		}
		public Object getUserData(){
			return data;
		}
	}
    
    /*获取指定文件大小*/
    public static long getFileLength(String filename){
    	long len=0;
    	try{
			@SuppressWarnings("resource")
			FileInputStream fs=new FileInputStream(filename);
	    	len=fs.available();
    	}catch(FileNotFoundException e){
    		e.printStackTrace();
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    	return len;
    }

    /*弹出本地短通知*/
    public static void showShortNotice(Context context,String info){
		Toast toast=Toast.makeText(context,info,Toast.LENGTH_SHORT);
		toast.show();
    }
    
    /*弹出本地长通知*/
    public static void showLongNotice(Context context,String info){
		Toast toast=Toast.makeText(context,info,Toast.LENGTH_LONG);
		toast.show();
    }
    
    /*获取SD卡空间信息*/
    public static String getSDCardInfo(){
    	File path=Environment.getExternalStorageDirectory();
        StatFs stat=new StatFs(path.getPath());
        String totalunit,availableunit;
        int blockSize=stat.getBlockSize();        
        float totalsize=(float)blockSize*(float)stat.getBlockCount();
        float availablesize=(float)blockSize*(float)stat.getAvailableBlocks();
        
        if(totalsize>=1000000000){
        	totalsize=totalsize/(float)1000000000;
        	totalunit="GB";
        }else if(totalsize>=1000000){
        	totalsize=totalsize/(float)1000000;
        	totalunit="MB";
        }else if(totalsize>=1000){
        	totalsize=totalsize/(float)1000;
        	totalunit="KB";
        }else{
        	totalunit="B";
        }
        totalsize=new BigDecimal(totalsize).setScale(2,
        	BigDecimal.ROUND_HALF_UP).floatValue(); 
        
        if(availablesize>=1000000000){
        	availablesize=availablesize/(float)1000000000;
        	availableunit="GB";
        }else if(availablesize>=1000000){
        	availablesize=availablesize/(float)1000000;
        	availableunit="MB";
        }else if(availablesize>=1000){
        	availablesize=availablesize/(float)1000;
        	availableunit="KB";
        }else{
        	availableunit="B";
        }
        availablesize=new BigDecimal(availablesize).setScale(2,
        	BigDecimal.ROUND_HALF_UP).floatValue(); 
        
        return availablesize+availableunit+"/"+totalsize+totalunit;
    }
    
    /*写SD卡文件类*/
    public class SDFileWriter{
    	private File 				 file;
    	private FileOutputStream	 output;
    	private String				 fullname;

    	public SDFileWriter(){
    		file=null;
    		output=null;
    	}
    	public String getFileFullName(){
    		return fullname;
    	}
    	public boolean open(String filepath, String filename){
			String sdpath=Environment.getExternalStorageDirectory().getPath();
			String fullpath=sdpath+"/"+filepath;
			
			file=new File(fullpath);	
    		if(!file.exists()){
    			if(!file.mkdir()){
    				Log.v(TAG,"无法创建目录："+fullpath+"!");
    				file=null;
    				return false;
    			}
    		}
    		
    		fullname=fullpath+"/"+filename;
    		file=new File(fullname);
    		if(!file.exists()){
    			try{
	    			if(!file.createNewFile()){
	    				Log.v(TAG,"无法创建文件："+fullname+"!");
	    				file=null;
	    				return false;
	    			}
    			}catch(IOException e){
    				e.printStackTrace();
    				file=null;
    				return false;
    			}
    		}
    		
    		try{
    			output=new FileOutputStream(file);
    		}catch(IOException e){
				e.printStackTrace();
				file=null;
				return false;
			}
    		
    		return true;
    	}
    	
    	public void close(){
    		if(null!=output){
    			try{
    	    		output.flush();
    	    		output.close();         				
    			}catch(IOException e){
    				e.printStackTrace();
    			}
    		}
    	}
    	
    	public void write(byte[] cbuf,int offset,int count){
    		if(null!=output){
    			try{
    				if(count>0){
	    				output.write(cbuf,offset,count);
	    				output.flush();
    				}
    			}catch(IOException e){
    				e.printStackTrace();
    				Log.v(TAG,"文件无法写入数据("+fullname+")!");
    			}
    		}
    	}
    }
    /**
     * @param filePath
     * @return 该文件缩略图
     */
   public static Bitmap getThumbnail(String filePath){
	   Bitmap bitmap;
	   if((filePath.substring(filePath.length() - ".jpg".length()).equals(".jpg"))||(filePath.substring(filePath.length() - ".png".length()).equals(".png"))){
			bitmap = BitmapFactory.decodeFile(filePath);
		    return bitmap;
	   }else{
	    bitmap=ThumbnailUtils.extractThumbnail(ThumbnailUtils.createVideoThumbnail(filePath, Thumbnails.MINI_KIND), 80, 80, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
	    return bitmap;
	   }
   }
   /**
    * 判断后缀名 Extension
    * @param file
    * @param Extension
    * @return
    */
  public static boolean guolvFiles(File file,String Extension){
	  if(Extension=="" || Extension==null){
		  return true;
	  }else if(file.getPath().substring(file.getPath().length() - Extension.length()).equals(Extension)){
		  return true;
	  }
	  return false; 
  }
  
 
  /* 判断文件MimeType的method */
  public static String getMIMEType(File f) 
  { 
    String type="";
    String fName=f.getName();
    /* 取得扩展名 */
    String end=fName.substring(fName.lastIndexOf(".")
    +1,fName.length()).toLowerCase(); 
    
    /* 依扩展名的类型决定MimeType */
    if(end.equals("m4a")||end.equals("mp3")||end.equals("mid")||
    end.equals("xmf")||end.equals("ogg")||end.equals("wav"))
    {
      type = "audio"; 
    }
    else if(end.equals("3gp")||end.equals("mp4"))
    {
      type = "video";
    }
    else if(end.equals("jpg")||end.equals("gif")||end.equals("png")||
    end.equals("jpeg")||end.equals("bmp"))
    {
      type = "image";
    }
    else if(end.equals("apk")) 
    { 
      /* android.permission.INSTALL_PACKAGES */ 
      type = "application/vnd.android.package-archive"; 
    } 
    else
    {
      type="*";
    }
    /*如果无法直接打开，就跳出软件列表给用户选择 */
    if(end.equals("apk")) 
    { 
    } 
    else 
    { 
      type += "/*";  
    } 
    return type;  
  } 
  // 转换文件大小
  public static  String FormetFileSize(long fileS)
  {
   DecimalFormat df = new DecimalFormat("#.00");
   String fileSizeString = "";
   if (fileS < 1024)
   {
    fileSizeString = df.format((double) fileS) + "B";
   }
   else if (fileS < 1048576)
    {
     fileSizeString = df.format((double) fileS / 1024) + "K";
    }
    else if (fileS < 1073741824)
    {
     fileSizeString = df.format((double) fileS / 1048576) + "M";
    }
    else
    {
     fileSizeString = df.format((double) fileS / 1073741824) + "G";
    }
   return fileSizeString;
  }
  public static byte[] readStream(InputStream inStream) throws Exception {     
      byte[] buffer = new byte[1024];     
       int len = -1;     
       ByteArrayOutputStream outStream = new ByteArrayOutputStream();     
       while ((len = inStream.read(buffer)) != -1) {     
           outStream.write(buffer, 0, len);     
       }     
       byte[] data = outStream.toByteArray();     
      outStream.close();     
       inStream.close();     
       return data;     
   }     
  
}
