package com.viga.engine;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;

import com.vdsp.DispatchHandler;
import com.vdsp.Vca;
import com.viga.utils.Utils;
import com.viga.utils.Utils.SDFileWriter;
import com.viga.utils.Utils.ThreadExt;

public class DataProc {
	private final static String TAG = "LIVECAMS";
	private static Utils utils = new Utils();
	public static ThreadExt videoRecAndTx = null;
	public static ThreadExt audioRecAndTx = null;
	public static ThreadExt videoRxAndPlay = null;
	public static ThreadExt localvideoRxAndPlay = null;
	public static ThreadExt audioRxAndPlay = null;
	public static ThreadExt videoNationUpload = null;
	public static ThreadExt audioNationUpload = null;
//	private final static int VCA_COUNT = 1;

	/* 刷新接收到的音视频码率 */
	private static void refreshProgressBar(int who, int prog) {
		
		android.os.Message msg = new android.os.Message();
		msg.what = 127;
		msg.arg1 = who;
		msg.arg2 = prog;
		DispatchHandler.sendMessage(msg);
	}
	
	private static void refreshNationUpLoadPorgressBar(int size,int totalLen){
		android.os.Message msg = new android.os.Message();
		msg.what = 211;//通知 本地浏览更新进度条
		msg.arg1 = size;
		msg.arg2 = totalLen;
		DispatchHandler.sendMessage(msg);
	}
	//刷新本地播放进度条
	private static void refreshlocalUpLoadPorgressBar(int size,int totalLen){
		android.os.Message msg = new android.os.Message();
		msg.what = 212;
		msg.arg1 = size;
		msg.arg2 = totalLen;
		DispatchHandler.sendMessage(msg);
	}
	public static void startNationVideoUpload(final File file,int priority,Object userdata) {
		if (null == videoNationUpload) {
			videoNationUpload = utils.new ThreadExt(new Runnable() {
				public void run() {
					videoNaitonUpload(videoNationUpload, file,false);
				}
			});
			if (priority > 0) {
				videoNationUpload.setPriority(priority);
			}
			if (null != userdata) {
				videoNationUpload.setUserData(userdata);
			}
			videoNationUpload.start();
			System.out.println("启动本地Video上传线程");
		}
	}
	//本地视频播放上传视频
	public static void startLocalNationVideoUpload(final File file,int priority,Object userdata,final boolean b) {
		if (null == videoNationUpload) {
			videoNationUpload = utils.new ThreadExt(new Runnable() {
				public void run() {
					videoNaitonUpload(videoNationUpload, file,b);
				}
			});
			if (priority > 0) {
				videoNationUpload.setPriority(priority);
			}
			if (null != userdata) {
				videoNationUpload.setUserData(userdata);
			}
			videoNationUpload.start();
			System.out.println("启动本地Video上传线程");
		}
	}	
	public static void startNationAudioUpload(final String filename,int priority,Object userdata) {
		if (null == audioNationUpload) {
			audioNationUpload = utils.new ThreadExt(new Runnable() {
				public void run() {
					audioNaitonUpload(audioNationUpload, filename);
				}
			});
			if (priority > 0) {
				audioNationUpload.setPriority(priority);
			}
			if (null != userdata) {
				audioNationUpload.setUserData(userdata);
			}
			audioNationUpload.start();
			System.out.println("启动本地audio上传线程");
		}
	}

	
	public static void startVideoRxAndPlay(int priority, Object userdata) {
		if (null == videoRxAndPlay) {
			videoRxAndPlay = utils.new ThreadExt(new Runnable() {
				public void run() {
									
					videoRxAndPlay(videoRxAndPlay);
				
					}
				
			});
			if (priority > 0) {
				videoRxAndPlay.setPriority(priority);
			}
			if (null != userdata) {
				videoRxAndPlay.setUserData(userdata);
			}
			videoRxAndPlay.start();
		}
	}
	public static void startlocalVideoRxAndPlay(int priority, Object userdata) {
		if (null == localvideoRxAndPlay) {
			localvideoRxAndPlay = utils.new ThreadExt(new Runnable() {
				public void run() {
									
					localvideoRxAndPlay(localvideoRxAndPlay);
				
					}
				
			});
			if (priority > 0) {
				localvideoRxAndPlay.setPriority(priority);
			}
			if (null != userdata) {
				localvideoRxAndPlay.setUserData(userdata);
			}
			localvideoRxAndPlay.start();
		}
	}
	public static void startAudioRxAndPlay(int priority, Object userdata) {
		if (null == audioRxAndPlay) {
			audioRxAndPlay = utils.new ThreadExt(new Runnable() {
				public void run() {
					audioRxAndPlay(audioRxAndPlay);
				}
			});
			if (priority > 0) {
				audioRxAndPlay.setPriority(priority);
			}
			if (null != userdata) {
				audioRxAndPlay.setUserData(userdata);
			}
			audioRxAndPlay.start();
		}
	}

	public static void startVideoRecAndTx(int priority, Object userdata) {
		if (null == videoRecAndTx) {
			videoRecAndTx = utils.new ThreadExt(new Runnable() {
				public void run() {
					videoRecAndTx(videoRecAndTx);
					// videoRecTest(videoRecAndTx);
				}
			});
			if (priority > 0) {
				videoRecAndTx.setPriority(priority);
			}
			if (null != userdata) {
				videoRecAndTx.setUserData(userdata);
			}
			videoRecAndTx.start();
		}
	}

	public static void startAudioRecAndTx(int priority, Object userdata) {
		if (null == audioRecAndTx) {
			audioRecAndTx = utils.new ThreadExt(new Runnable() {
				public void run() {
					audioRecAndTx(audioRecAndTx);
				}
			});
			if (priority > 0) {
				audioRecAndTx.setPriority(priority);
			}
			if (null != userdata) {
				audioRecAndTx.setUserData(userdata);
			}
			audioRecAndTx.start();
		}
	}

	public static void stopVideoRxAndPlay() {
		if (null != videoRxAndPlay) {
			videoRxAndPlay.finish();
			videoRxAndPlay = null;
		}
	}

	public static void stopAudioRxAndPlay() {
		if (null != audioRxAndPlay) {
			audioRxAndPlay.finish();
			audioRxAndPlay = null;
		}
	}

	public static void stopVideoRecAndTx() {
		if (null != videoRecAndTx) {
			videoRecAndTx.finish();
			videoRecAndTx = null;
		}
	}

	public static void stopAudioRecAndTx() {
		if (null != audioRecAndTx) {
			audioRecAndTx.finish();
			audioRecAndTx = null;
		}
	}

	public static void stopAudioUpload() {
		if (null != audioNationUpload) {
			audioNationUpload = null;
			System.out.println("本地Audio上传完成");
		}
	}
	public static void stopVideoUpload() {
		if (null != videoNationUpload) {
			videoNationUpload = null;
			System.out.println("本地video上传完成");
		}
	}
	public static void stopUnComLoad(){
		if (null != videoNationUpload) {
			videoNationUpload.finish();
			videoNationUpload = null;
		}
		if (null != audioNationUpload) {
			audioNationUpload.finish();
			audioNationUpload = null;
		}
	}

	public static void startRxAndPlay(int vprio, int aprio, Object vud, Object aud) {
		startAudioRxAndPlay(aprio, aud);
		startVideoRxAndPlay(vprio, vud);
	}
	public static void startlocalRxAndPlay(int vprio, int aprio, Object vud, Object aud) {
		startAudioRxAndPlay(aprio, aud);
		startlocalVideoRxAndPlay(vprio, vud);
	}
	public static void startRecAndTx(int vprio, int aprio, Object vud, Object aud) {
		startAudioRecAndTx(aprio, aud);
		startVideoRecAndTx(vprio, vud);
	}

	public static void stopRxAndPlay() {
		stopAudioRxAndPlay();
		stopVideoRxAndPlay();
	}

	public static void stopRecAndTx() {
		stopAudioRecAndTx();
		stopVideoRecAndTx();
	}
	// 本地上传音频
	protected static void audioNaitonUpload(ThreadExt thread, String filename) {
		try {
			File file = new File(filename);
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream in = new BufferedInputStream(fis);
			int bufsize = AudioRecord.getMinBufferSize(SettingAndStatus.settings.isplrt,
					AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
			int headlen = (1 == SettingAndStatus.settings.speexen) ? 4 : 4 * 4;
			int minsize = (SettingAndStatus.settings.isplrt * 20 * 2) / 1000;// 20ms的音频数据量
			bufsize = ((bufsize + minsize - 1) / minsize) * minsize;
			byte[] buffer = new byte[bufsize + headlen];
			int size, offset = headlen, frameno = 0;
			/* 启动音频采集-编码-存储-发送流水线 */
			while (!thread.isExit()) {
				long timestamp = new Date().getTime();
				// System.out.println("buffeelen:"+(bufsize+headlen-offset));
				size = in.read(buffer, offset, bufsize + headlen - offset);
				if (size <= 0) {
					break;
				}
				if (size != AudioRecord.ERROR_INVALID_OPERATION) {
					offset += size;
					if (offset == (bufsize + headlen)) {
						if (1 == SettingAndStatus.settings.speexen) {
							// 前4个字节为时间戳，后面为音频PCM数据，
							// 提交给VCA，由其编码、打包和发送!!!!!
							Utils.longToByte4(timestamp, buffer, 0);
						} else {
							// 前20个字节依次为时间戳、数据类型、长度和帧序号，
							// 后面为音频PCM数据，提交给VCA，尤其打包发送!!!
							size = Utils.longToByte4(timestamp, buffer, 0);
							size += Utils.intToByte4(0x00000030, buffer, size);
							size += Utils.intToByte4(bufsize, buffer, size);
							size += Utils.intToByte4(frameno++, buffer, size);
						}						
							Vca.putAudioData(buffer, bufsize + headlen, 1000);					
						timestamp = new Date().getTime();
						offset = headlen;
					}
				}
				// Thread.sleep(500);
			}
			in.close();
			stopAudioUpload();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 本地视频上传
	protected static void videoNaitonUpload(ThreadExt thread, File file,boolean b) {
		try {
			int length = 1024 * 1024;// 和Vca.Param中的videofrmlen保持一致;
			byte buffer[] = new byte[length];
			byte head[] = { 0, 0, 0, 0, 0 };
			int size, offset, payloadlen, frameno;
			boolean breakflag = true;
		//	long begin,end;
			FileInputStream fis = new FileInputStream(file);
			int fileLength=Integer.parseInt(file.length()+"");
			BufferedInputStream in= new BufferedInputStream(fis);
			if (!thread.isExit()) {
				// 开启音频
				String filename = file.getAbsolutePath().replace(".264", ".spx");
				startNationAudioUpload(filename,8,null);
			}
			frameno = 0;
			breakflag = false;
			while (!breakflag && !thread.isExit()) {
				if ((SettingAndStatus.isHtcC510e() || SettingAndStatus.isMiOnePlus())) {
					/* 读取载荷数据的长度(4B)以及NAL头(1B) */
					offset = 0;
					size = in.read(head, 0, 5);
				//	System.out.println("headSize:"+size);
					if (size < 1) {
						breakflag = true;
						break;
					}
					if (!breakflag && !thread.isExit()) {
						payloadlen = Utils.byte4ToInt(head, 0);
						long timestamp = new Date().getTime();
						/* 检查是否是IDR，如果是则需要插入SPS+PPS */
						offset = H264Stream.getFrameHeadLength();
						boolean idr = H264Stream.isIDR(head[4]);
						if (idr) {
							offset += H264Stream.writeHead(buffer, offset, length - offset);
							if(b){
								offset += H264Stream.writeSPS(buffer, offset, length - offset,
										SettingAndStatus.settings.localvideosize);
							}
							offset += H264Stream.writeSPS(buffer, offset, length - offset,
									SettingAndStatus.settings.videosize);
							offset += H264Stream.writeHead(buffer, offset, length - offset);
							offset += H264Stream.writePPS(buffer, offset, length - offset);
						}
						offset += H264Stream.writeHead(buffer, offset, length - offset);
						payloadlen += offset;
						buffer[offset++] = head[4];// NAL头
						while (!breakflag && !thread.isExit() && offset < payloadlen) {
							try {
								size = in.read(buffer, offset, payloadlen - offset);
								if (size > 0) {
									offset += size;
						//			System.out.println("offset:"+offset+"   length:"+(payloadlen - offset));
								}else {
									breakflag = true;
									break;
								}
								if(!b){
						           refreshNationUpLoadPorgressBar((size+5),fileLength);
						           Thread.sleep(30);//控制视频上传速度
								}else{
									refreshlocalUpLoadPorgressBar((size+5),fileLength);	
								   Thread.sleep(55);//控制本地视频上传速度
								}
						
						
							} catch (SocketTimeoutException e) {
								if (!SettingAndStatus.avrecording) {
									breakflag = true;
								}
							}
						}
						/* 写文件并发送数据 */
						if (!breakflag && !thread.isExit()) {
							int frmheadlen = H264Stream.getFrameHeadLength();
							if (true) {
								H264Stream.writeFrameHead(buffer, 0, length, timestamp, 0x23, payloadlen
										- frmheadlen, idr ? 1 : 0, frameno);
								if (!Vca.putVideoData(buffer, payloadlen, 1000)) {
									Log.v(TAG, "发送视频数据异常!");
								}
							}
							frameno++;
						}
					}
				}
			}
			in.close();
			fis.close();
			stopVideoUpload();
				//stopAudioUpload();
			//	DispatchHandler.submitShortNotice("提示：录像结束!");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/* 视流分析、存储、发送线程 */
	private static void videoRecAndTx(ThreadExt thread) {
		Log.v(TAG, "视频流分析、存储、发送线程已运行!");
		ServerSocket server = null;
		Socket client = null;
		BufferedInputStream in = null;
		SDFileWriter arecorder = null;
		SDFileWriter vrecorder = null;
		InputStream socketIn = null;
		int length = 1024 * 1024;// 和Vca.Param中的videofrmlen保持一致;
		byte buffer[] = new byte[length];
		byte nationbuf[] = new byte[length];
		byte head[] = new byte[5];
		int size, offset, payloadlen, frameno;
		boolean breakflag = true;
		String afilename=null,vfilename = null,filename = null;
		long begin, end;
		
		try {
			/* 创建服务器套接字并设置超时属性 */
			server = new ServerSocket(SettingAndStatus.settings.mdaport);
			server.setSoTimeout(SettingAndStatus.settings.timeout);

			while (!thread.isExit()) {
				/* 等待客户端建立链接 */
				
				frameno = 0;
				Log.v(TAG, "等待客户端建立链接！");
				while (!thread.isExit()) {
					try {
						client = server.accept();
						Log.v(TAG, "与客户端建立了链接！");
						client.setReceiveBufferSize(1024 * 1024);
						client.setSoTimeout(SettingAndStatus.settings.timeout);
						socketIn = client.getInputStream();
						in = new BufferedInputStream(socketIn);
						break;
					} catch (SocketTimeoutException e) {
					}
				}
				/* 准备录像文件 */
				if (!thread.isExit()) {
					if (SettingAndStatus.settings.avrecord) {
						SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
						filename = format.format(new Date());
						afilename = filename + ".spx";
						vfilename = filename + ".264";
						arecorder = utils.new SDFileWriter();
						vrecorder = utils.new SDFileWriter();
						if (!arecorder.open("LiveCams/video", afilename)
								|| !vrecorder.open("LiveCams/video", vfilename)) {
							arecorder.close();
							vrecorder.close();
							arecorder = null;
							vrecorder = null;
							Log.v(TAG, "无法录像，请检查SD卡!");
							DispatchHandler.submitLongNotice("警告：无法录像，请检查SD卡!");
						} else {
							String fullname = vrecorder.getFileFullName();
							Log.v(TAG, "开始录像(" + fullname + "/.spx)!");
							DispatchHandler.submitShortNotice("提示：开始录像(" + fullname + "/.spx)!");
						}
					} else {
						Log.v(TAG, "开始录像!");
						DispatchHandler.submitShortNotice("提示：开始录像!");
					}
				}

				/* 启动音频采集编码任务 */
				if (!thread.isExit()) {
					startAudioRecAndTx(8, arecorder);
				}

				/*
				 * 丢弃前52个字节头（24+4+24）,适合的平台： HTC510E,MIONEPLUS
				 */
				breakflag = false;
				offset = 0;
				
				if ((SettingAndStatus.isHtcC510e() || SettingAndStatus.isMiOnePlus())) {
					while (!breakflag && !thread.isExit() && offset < 52) {
						try {
							size = in.read(buffer, offset, 52 - offset);

							if (size >= 0) {
								offset += size;
							} else {
								breakflag = true;
							}
						} catch (SocketTimeoutException e) {
							if (!SettingAndStatus.avrecording) {
								breakflag = true;
							}
						}
					}

				}
				
				while (!breakflag && !thread.isExit()) {
					if ((SettingAndStatus.isHtcC510e() || SettingAndStatus.isMiOnePlus())) {
						/* 读取载荷数据的长度(4B)以及NAL头(1B) */
						offset = 0;
						while (!breakflag && !thread.isExit() && offset < head.length) {
							try {
								size = in.read(head, offset, head.length - offset);
								if (size >= 0) {
									offset += size;
								} else {
									breakflag = true;
								}
								for (int i = 0; i < 4; i++) {
									nationbuf[i] = head[i];
								}
							} catch (SocketTimeoutException e) {
								if (!SettingAndStatus.avrecording) {
									breakflag = true;
								}
							}
						}

						if (!breakflag && !thread.isExit()) {
							/* 检查数据是否异常 */
							payloadlen = Utils.byte4ToInt(head, 0);
							if (0 != (head[4] & 0x80)
									|| (head[4] & 0x1F) < 1
									|| (head[4] & 0x1F) > 13
									|| payloadlen <= 0
									|| payloadlen > H264Stream
											.getMaxFrameLength(SettingAndStatus.settings.videosize)) {
								breakflag = true;
								break;
							}

							/* 检查是否需要重新分配缓冲区 */
							
							long timestamp = new Date().getTime();
							if (payloadlen > length) {
								length = ((payloadlen + 1023) / 1024 + 1) * 1024;
								buffer = new byte[length];
								Log.v(TAG, "重新分配" + length + "字节缓冲区!");
							}

							/* 检查是否是IDR，如果是则需要插入SPS+PPS */
							offset = H264Stream.getFrameHeadLength();
							boolean idr = H264Stream.isIDR(head[4]);
							if (idr) {
								offset += H264Stream.writeHead(buffer, offset, length - offset);
								offset += H264Stream.writeSPS(buffer, offset, length - offset,
										SettingAndStatus.settings.videosize);
								offset += H264Stream.writeHead(buffer, offset, length - offset);
								offset += H264Stream.writePPS(buffer, offset, length - offset);
							}
							/* 读取载荷数据并适当延时 */
							begin = (new Date()).getTime();
							offset += H264Stream.writeHead(buffer, offset, length - offset);
							payloadlen += offset;
							buffer[offset++] = head[4];// NAL头
							//int nationoffset = 0;
							int headeroffset = offset;
							while (!breakflag && !thread.isExit() && offset < payloadlen) {
								try {
									size = in.read(buffer, offset, payloadlen - offset);
								//	nationoffset += size;

									if (size >= 0) {
										offset += size;
									} else {
										breakflag = true;
										break;
									}
									if (!breakflag && !thread.isExit() && SettingAndStatus.avrecording) {
										end = (new Date()).getTime();
										if (20 > (end - begin)) {
											Thread.sleep(20 - end + begin);
										}
										begin = end;
									}
								} catch (SocketTimeoutException e) {
									if (!SettingAndStatus.avrecording) {
										breakflag = true;
									}
								}
							}

							headeroffset -= 1;
							for (int i = 0; i < (payloadlen - headeroffset); i++) {
								nationbuf[4 + i] = buffer[headeroffset + i];
							}//本地文件buf  规定只有 帧长度+帧数据
						//	System.out.println("gap:"+(b-a));
							/* 写文件并发送数据 */
							if (!breakflag && !thread.isExit()) {
								int frmheadlen = H264Stream.getFrameHeadLength();
								if (null != vrecorder) {
									vrecorder.write(nationbuf, 0, 4 + payloadlen - headeroffset);
								}
								if (SettingAndStatus.VcaStatus.LOGINED == SettingAndStatus.vcaStatus.status
										&& SettingAndStatus.phoneDataSending) {
									H264Stream.writeFrameHead(buffer, 0, length, timestamp, 0x23, payloadlen
											- frmheadlen, idr ? 1 : 0, frameno);
									if (!Vca.putVideoData(buffer, payloadlen, 1000)) {
										Log.v(TAG, "发送视频数据异常!");
									}
								}
								frameno++;
							}
						}
					} else {
						/* 不支持的视频数据不发送 */
						begin = (new Date()).getTime();
						while (!breakflag && !thread.isExit()) {
							try {
								size = in.read(buffer, 0, length);
								if (size >= 0) {
									if (null != vrecorder) {
										vrecorder.write(buffer, 0, size);
									}
								} else {
									breakflag = true;
									break;
								}
								if (!breakflag && !thread.isExit() && SettingAndStatus.avrecording) {
									end = (new Date()).getTime();
									if (20 > (end - begin)) {
										Thread.sleep(20 - end + begin);
									}
									begin = end;
								}
							} catch (SocketTimeoutException e) {
								if (!SettingAndStatus.avrecording) {
									breakflag = true;
								}
							}
						}
					}
				}

				/* 录像结束 */
				if (thread.isExit() || breakflag) {
					stopAudioRecAndTx();
					if (null != arecorder && null != vrecorder) {
						String fullname = arecorder.getFileFullName();
						long afilelength = Utils.getFileLength(fullname);
						fullname = vrecorder.getFileFullName();
						long vfilelength = Utils.getFileLength(fullname);
						Log.v(TAG, "录像结束(" + fullname + "/.spx," + vfilelength + "/" + afilelength + "字节)!");
						DispatchHandler.submitShortNotice("提示：录像结束(" + fullname + "/.spx," + vfilelength
								+ "/" + afilelength + "字节)!");
						arecorder.close();
						vrecorder.close();
						arecorder = null;
						vrecorder = null;
					} else {
						Log.v(TAG, "录像结束!");
						DispatchHandler.submitShortNotice("提示：录像结束!");
					}
					if (null != client) {
						in.close();
						client.close();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		/* 关闭服务器套接字 */
		try {
			if (null != server) {
				server.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.v(TAG, "视频流分析、存储、发送线程已结束!");
	}

	/* 音频流采集、存储、发送线程 */
	private static void audioRecAndTx(ThreadExt thread) {
		Log.v(TAG, "音频流采集、存储、发送线程已启动!");
		/* 初始化音频采集 */
		AudioRecord audioRecord;
		int bufsize = AudioRecord.getMinBufferSize(SettingAndStatus.settings.isplrt,
				AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
		if (SettingAndStatus.settings.useBlueTooth) {// 使用蓝牙作为音频采集
			audioRecord = new AudioRecord(AudioManager.STREAM_VOICE_CALL, SettingAndStatus.settings.isplrt,
					AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufsize);
		} else {
			audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SettingAndStatus.settings.isplrt,
					AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufsize);
		}
		SDFileWriter recorder = (SDFileWriter) thread.getUserData();
		int headlen = (1 == SettingAndStatus.settings.speexen) ? 4 : 4 * 4;
		int minsize = (SettingAndStatus.settings.isplrt * 20 * 2) / 1000;// 20ms的音频数据量
		bufsize = ((bufsize + minsize - 1) / minsize) * minsize;
		byte[] buffer = new byte[bufsize + headlen];
		int size, offset = headlen, frameno = 0;

		/* 启动音频采集-编码-存储-发送流水线 */
		audioRecord.startRecording();
		long timestamp = new Date().getTime();
		while (!thread.isExit()) {
			size = audioRecord.read(buffer, offset, bufsize + headlen - offset);
			if (size != AudioRecord.ERROR_INVALID_OPERATION) {
				offset += size;
				if (offset == (bufsize + headlen)) {
					if (1 == SettingAndStatus.settings.speexen) {
						// 前4个字节为时间戳，后面为音频PCM数据，
						// 提交给VCA，由其编码、打包和发送!!!!!
						Utils.longToByte4(timestamp, buffer, 0);
					} else {
						// 前20个字节依次为时间戳、数据类型、长度和帧序号，
						// 后面为音频PCM数据，提交给VCA，尤其打包发送!!!
						size = Utils.longToByte4(timestamp, buffer, 0);
						size += Utils.intToByte4(0x00000030, buffer, size);
						size += Utils.intToByte4(bufsize, buffer, size);
						size += Utils.intToByte4(frameno++, buffer, size);
					}
					if (SettingAndStatus.VcaStatus.LOGINED == SettingAndStatus.vcaStatus.status
							&& SettingAndStatus.phoneDataSending) {
						if (!Vca.putAudioData(buffer, bufsize + headlen, 1000)) {
							Log.v(TAG, "发送音频数据异常!");
						}
					}

					// 全都是音频数据，没有时间戳等头数据
					if (null != recorder) {
						recorder.write(buffer, headlen, bufsize);
					}
					timestamp = new Date().getTime();
					offset = headlen;
				}
			}
		}

		/* 结束音频采集 */
		audioRecord.stop();
		audioRecord.release();

		Log.v(TAG, "音频流采集、存储、发送线程已结束!");
	}
	/* 视频数据接收、播放线程 */
	private static void videoRxAndPlay(ThreadExt thread) {
		Log.v(TAG, "视频数据获取、播放线程已启动!");
		int bufsize = 1024 * 1024;// 和Vca.Param中的videofrmlen保持一致;
		int[] buffer = new int[bufsize];
		SurfaceHolder holder = null;
		int oldwidth = 0, oldheight = 0, framebufcnt = 0;
		/*
		 * SDFileWriter yuvrecorder=utils.new SDFileWriter();
		 * if(!yuvrecorder.open("LiveCams","dec.rgb")){ yuvrecorder=null; }
		 */
		Date begin = new Date();
		while (!thread.isExit()) {
			int size = Vca.getVideoData2(buffer, 1000);
			//Log.i(TAG, size+"");
			if (size > 0) {
				if (0 == SettingAndStatus.settings.h264decen) {
					Vca.updateVideoBitrate(size);
				
				} else {
					holder = (SurfaceHolder) thread.getUserData();
					if (null != holder) {
						synchronized (holder) {
							int width = Utils.intSwapByte(buffer[1]);
							int height = Utils.intSwapByte(buffer[2]);
							
							
							Bitmap bmp = Bitmap.createBitmap(buffer, 2, width, width, height,
									Bitmap.Config.RGB_565);
							Canvas cv = holder.lockCanvas();
							Paint pt = new Paint();
							if (oldwidth != width || oldheight != height) {
								if (0 != oldwidth && 0 != oldheight) {
									framebufcnt = 8;
								}
								oldwidth = width;
								oldheight = height;
							}
							if (framebufcnt > 0) {
								cv.drawColor(Color.BLACK);
								framebufcnt--;
							}
							
							
							Matrix mx=new Matrix();
							mx.setRotate(90);
							
							cv.drawBitmap(bmp, (SettingAndStatus.displayheight - width) / 2,
									(SettingAndStatus.displaywidth - height) / 2, pt);
							holder.unlockCanvasAndPost(cv);
						
						}
					}

					/*
					 * if(null!=yuvrecorder){
					 * yuvrecorder.write(buffer,12,size-12); }
					 */
				}
				Date end = new Date();
				long mscnt = end.getTime() - begin.getTime();
				if (mscnt >= 950) {// 约1秒刷新一次
					begin = end;
					refreshProgressBar(0, Vca.getVideoBitrate());
				}
				}
			}
		}

    //本地视频数据接收、播放线程 
	private static void localvideoRxAndPlay(ThreadExt thread) {
		Log.v(TAG, "视频数据获取、播放线程已启动!");
		int bufsize = 1024 * 1024;// 和Vca.Param中的videofrmlen保持一致;
		int[] buffer = new int[bufsize];
		SurfaceHolder holder = null;
		int oldwidth = 0, oldheight = 0, framebufcnt = 0;
		Date begin = new Date();
		while (!thread.isExit()) {
			int size = Vca.getVideoData2(buffer, 1000);
			//Log.i(TAG, size+"");
			if (size > 0) {
				if (0 == SettingAndStatus.settings.h264decen) {
					Vca.updateVideoBitrate(size);
				
				} else {
					holder = (SurfaceHolder) thread.getUserData();
					if (null != holder) {
						synchronized (holder) {
							int width = Utils.intSwapByte(buffer[1]);
							int height = Utils.intSwapByte(buffer[2]);
							
							
							Bitmap bmp = Bitmap.createBitmap(buffer, 2, width, width, height,
									Bitmap.Config.RGB_565);
							Canvas cv = holder.lockCanvas();
							Paint pt = new Paint();
							if (oldwidth != width || oldheight != height) {
								if (0 != oldwidth && 0 != oldheight) {
									framebufcnt = 8;
								}
								oldwidth = width;
								oldheight = height;
							}
							if (framebufcnt > 0) {
								cv.drawColor(Color.BLACK);
								framebufcnt--;
							}
							Matrix mx=new Matrix();
							mx.setRotate(90);
							Bitmap newbmp=Bitmap.createBitmap(bmp, 0, 0, width, height, mx, false);
							cv.drawBitmap(newbmp, (SettingAndStatus.displayheight - width) / 2+45,
									(SettingAndStatus.displaywidth - height) / 2-45, pt);
							holder.unlockCanvasAndPost(cv);
						
						}
					}

					/*
					 * if(null!=yuvrecorder){
					 * yuvrecorder.write(buffer,12,size-12); }
					 */
				}
				Date end = new Date();
				long mscnt = end.getTime() - begin.getTime();
				if (mscnt >= 950) {// 约1秒刷新一次
					begin = end;
					refreshProgressBar(0, Vca.getVideoBitrate());
				}
				}
			}
		}

	/* 音频数据接收、播放线程 */
	private static void audioRxAndPlay(ThreadExt thread) {
		Log.v(TAG, "音频数据接收、播放线程已启动!");
		/* 初始化音频播放 */
		AudioTrack audioTrack;
		int bufsize = AudioTrack.getMinBufferSize(SettingAndStatus.settings.osplrt,
				AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
		if (SettingAndStatus.settings.useBlueTooth) {
			audioTrack = new AudioTrack(
					AudioManager.STREAM_VOICE_CALL,// ********
					SettingAndStatus.settings.osplrt, AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT, bufsize, AudioTrack.MODE_STREAM);
		} else {
			audioTrack = new AudioTrack(
					AudioManager.STREAM_MUSIC,
					SettingAndStatus.settings.osplrt, AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT, bufsize, AudioTrack.MODE_STREAM);
		}
		int headlen = (1 == SettingAndStatus.settings.speexen) ? 4 : 4 * 4;
		byte[] buffer = new byte[1024 * 8];// 和Vca.Param中的audiofrmlen保持一致;
		int size, datatype, datalen;
		// audioTrack.getNativeOutputSampleRate(AudioManager.STREAM_VOICE_CALL);
		/* 启动音频播放 */
		audioTrack.play();

		Date begin = new Date();
		while (!thread.isExit()) {
			size = Vca.getAudioData(buffer, 1000);
			if (size > 0) {
				if (0 == SettingAndStatus.settings.speexen) {
					datatype = Utils.byte4ToInt(buffer, 4);
					datalen = Utils.byte4ToInt(buffer, 8);
					if ((0x30 != datatype && 0x34 != datatype && 0x38 != datatype)
							|| (8000 != SettingAndStatus.settings.osplrt && 0x30 == datatype) || /* PCM_8K */
							(16000 != SettingAndStatus.settings.osplrt && 0x34 == datatype) || /* PCM_16K */
							(32000 != SettingAndStatus.settings.osplrt && 0x38 == datatype) || /* PCM_32K */
							datalen != (size - headlen)) {
						Log.v(TAG, "无法播放的音频数据!");
						continue;
					}
				}
				audioTrack.write(buffer, headlen, size - headlen);
				if (SettingAndStatus.isplaymode) {
					Date end = new Date();
					long mscnt = end.getTime() - begin.getTime();
					if (mscnt >= 950) {// 约1秒刷新一次
						begin = end;
						refreshProgressBar(1, Vca.getAudioBitrate());
					}

				}
			}
		}

		/* 结束音频播放 */
		audioTrack.stop();
		audioTrack.release();
		Log.v(TAG, "音频数据接收、播放线程已结束!");
	}
}
